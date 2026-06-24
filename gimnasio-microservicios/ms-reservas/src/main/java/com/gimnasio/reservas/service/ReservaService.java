package com.gimnasio.reservas.service;

import com.gimnasio.reservas.client.ClaseClient;
import com.gimnasio.reservas.client.MembresiaClient;
import com.gimnasio.reservas.client.SocioClient;
import com.gimnasio.reservas.dto.ClaseRespuesta;
import com.gimnasio.reservas.dto.ReservaDTO;
import com.gimnasio.reservas.dto.SocioRespuesta;
import com.gimnasio.reservas.exception.RecursoNoEncontradoException;
import com.gimnasio.reservas.exception.ReglaNegocioException;
import com.gimnasio.reservas.model.EstadoReserva;
import com.gimnasio.reservas.model.Reserva;
import com.gimnasio.reservas.repository.ReservaRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de reservas: orquesta llamadas a ms-socios, ms-membresias y ms-clases
 * para garantizar que solo socios activos con membresia vigente puedan reservar
 * clases con cupo disponible.
 */
@Service
public class ReservaService {

    private static final Logger log = LoggerFactory.getLogger(ReservaService.class);

    private final ReservaRepository repo;
    private final SocioClient socioClient;
    private final ClaseClient claseClient;
    private final MembresiaClient membresiaClient;

    public ReservaService(ReservaRepository repo,
                          SocioClient socioClient,
                          ClaseClient claseClient,
                          MembresiaClient membresiaClient) {
        this.repo = repo;
        this.socioClient = socioClient;
        this.claseClient = claseClient;
        this.membresiaClient = membresiaClient;
    }

    public List<Reserva> listar() { return repo.findAll(); }

    public List<Reserva> listarPorSocio(Long socioId) { return repo.findBySocioId(socioId); }

    public List<Reserva> listarPorClase(Long claseId) { return repo.findByClaseId(claseId); }

    public Reserva buscarPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Reserva con id " + id + " no existe"));
    }

    public Reserva reservar(ReservaDTO dto) {
        log.info("Iniciando reserva socio={} clase={}", dto.getSocioId(), dto.getClaseId());

        // 1) Validar socio (Feign -> ms-socios)
        SocioRespuesta socio = obtenerSocio(dto.getSocioId());
        if (!"ACTIVO".equals(socio.getEstado())) {
            throw new ReglaNegocioException(
                    "El socio no esta ACTIVO. Estado actual: " + socio.getEstado());
        }

        // 2) Validar membresia vigente (Feign -> ms-membresias)
        if (!consultarVigente(dto.getSocioId())) {
            throw new ReglaNegocioException(
                    "El socio no posee una membresia vigente. No puede reservar clases.");
        }

        // 3) Validar que la clase exista y tenga cupos (Feign -> ms-clases)
        ClaseRespuesta clase = obtenerClase(dto.getClaseId());
        if (clase.getFechaHora().isBefore(LocalDateTime.now())) {
            throw new ReglaNegocioException("No se puede reservar una clase que ya ocurrio");
        }
        if (clase.getCuposDisponibles() <= 0) {
            throw new ReglaNegocioException("No quedan cupos disponibles para esta clase");
        }

        // 4) Validar que el socio no haya reservado ya esta clase
        repo.findBySocioIdAndClaseId(dto.getSocioId(), dto.getClaseId())
                .ifPresent(r -> {
                    throw new ReglaNegocioException(
                            "El socio ya tiene una reserva para esta clase (id=" + r.getId() + ")");
                });

        // 5) Crear la reserva
        Reserva r = new Reserva();
        r.setSocioId(dto.getSocioId());
        r.setClaseId(dto.getClaseId());
        r.setFechaReserva(LocalDateTime.now());
        r.setEstado(EstadoReserva.CONFIRMADA);
        Reserva guardada = repo.save(r);

        // 6) Decrementar cupo en ms-clases (Feign)
        try {
            claseClient.decrementarCupo(dto.getClaseId());
            log.info("Reserva {} confirmada y cupo decrementado", guardada.getId());
        } catch (FeignException e) {
            log.error("Error decrementando cupo, rollback de reserva {}: {}", guardada.getId(), e.getMessage());
            repo.delete(guardada);
            throw new ReglaNegocioException("No se pudo confirmar el cupo. Reserva cancelada.");
        }

        return guardada;
    }

    public Reserva cancelar(Long id) {
        log.info("Cancelando reserva id={}", id);
        Reserva r = buscarPorId(id);
        if (r.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new ReglaNegocioException(
                    "Solo se pueden cancelar reservas CONFIRMADAS. Estado actual: " + r.getEstado());
        }
        r.setEstado(EstadoReserva.CANCELADA);
        Reserva guardada = repo.save(r);

        // Devolver cupo a ms-clases
        try {
            claseClient.incrementarCupo(r.getClaseId());
        } catch (FeignException e) {
            log.warn("No se pudo restaurar el cupo de la clase {}: {}", r.getClaseId(), e.getMessage());
        }
        return guardada;
    }

    public Reserva marcarAsistida(Long id) {
        Reserva r = buscarPorId(id);
        if (r.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new ReglaNegocioException("Solo se pueden marcar como asistidas reservas CONFIRMADAS");
        }
        r.setEstado(EstadoReserva.ASISTIDA);
        return repo.save(r);
    }

    public void eliminar(Long id) {
        Reserva r = buscarPorId(id);
        repo.delete(r);
    }

    private SocioRespuesta obtenerSocio(Long id) {
        try {
            return socioClient.obtenerSocio(id);
        } catch (FeignException.NotFound e) {
            throw new RecursoNoEncontradoException("Socio con id " + id + " no existe");
        } catch (FeignException e) {
            log.error("Error consultando ms-socios: {}", e.getMessage());
            throw new ReglaNegocioException("No se pudo comunicar con ms-socios");
        }
    }

    private ClaseRespuesta obtenerClase(Long id) {
        try {
            return claseClient.obtenerClase(id);
        } catch (FeignException.NotFound e) {
            throw new RecursoNoEncontradoException("Clase con id " + id + " no existe");
        } catch (FeignException e) {
            log.error("Error consultando ms-clases: {}", e.getMessage());
            throw new ReglaNegocioException("No se pudo comunicar con ms-clases");
        }
    }

    private boolean consultarVigente(Long socioId) {
        try {
            return Boolean.TRUE.equals(membresiaClient.tieneVigente(socioId).getVigente());
        } catch (FeignException e) {
            log.error("Error consultando ms-membresias: {}", e.getMessage());
            throw new ReglaNegocioException("No se pudo comunicar con ms-membresias");
        }
    }
}

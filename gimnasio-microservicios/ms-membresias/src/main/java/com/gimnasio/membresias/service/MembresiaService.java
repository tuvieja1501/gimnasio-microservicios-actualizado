package com.gimnasio.membresias.service;

import com.gimnasio.membresias.client.SocioClient;
import com.gimnasio.membresias.dto.MembresiaDTO;
import com.gimnasio.membresias.dto.SocioRespuesta;
import com.gimnasio.membresias.exception.RecursoNoEncontradoException;
import com.gimnasio.membresias.exception.ReglaNegocioException;
import com.gimnasio.membresias.model.EstadoMembresia;
import com.gimnasio.membresias.model.Membresia;
import com.gimnasio.membresias.model.PlanMembresia;
import com.gimnasio.membresias.repository.MembresiaRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Logica de membresias. Consume ms-socios via Feign para validar
 * la existencia y estado del socio antes de crear una membresia.
 */
@Service
public class MembresiaService {

    private static final Logger log = LoggerFactory.getLogger(MembresiaService.class);

    private final MembresiaRepository repo;
    private final PlanMembresiaService planService;
    private final SocioClient socioClient;

    public MembresiaService(MembresiaRepository repo,
                            PlanMembresiaService planService,
                            SocioClient socioClient) {
        this.repo = repo;
        this.planService = planService;
        this.socioClient = socioClient;
    }

    public List<Membresia> listar() {
        return repo.findAll();
    }

    public Membresia buscarPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Membresia con id " + id + " no existe"));
    }

    public List<Membresia> listarPorSocio(Long socioId) {
        log.info("Listando membresias del socio {}", socioId);
        return repo.findBySocioId(socioId);
    }

    public Membresia crear(MembresiaDTO dto) {
        log.info("Creando membresia socio={} plan={}", dto.getSocioId(), dto.getPlanId());

        // 1) Validar socio via Feign (consumo de microservicio remoto)
        SocioRespuesta socio = obtenerSocioRemoto(dto.getSocioId());
        if (!"ACTIVO".equals(socio.getEstado())) {
            throw new ReglaNegocioException(
                    "El socio " + socio.getRut() + " no esta ACTIVO. Estado actual: " + socio.getEstado());
        }

        // 2) Validar que no tenga otra membresia VIGENTE
        if (repo.existsBySocioIdAndEstado(dto.getSocioId(), EstadoMembresia.VIGENTE)) {
            throw new ReglaNegocioException(
                    "El socio ya tiene una membresia VIGENTE. Debe cancelarla antes de crear otra.");
        }

        // 3) Resolver el plan
        PlanMembresia plan = planService.buscarPorId(dto.getPlanId());
        if (!Boolean.TRUE.equals(plan.getActivo())) {
            throw new ReglaNegocioException("El plan seleccionado no esta activo");
        }

        // 4) Construir y guardar
        Membresia m = new Membresia();
        m.setSocioId(dto.getSocioId());
        m.setPlan(plan);
        m.setFechaInicio(dto.getFechaInicio());
        m.setFechaFin(dto.getFechaInicio().plusMonths(plan.getDuracionMeses()));
        m.setEstado(EstadoMembresia.VIGENTE);

        Membresia guardada = repo.save(m);
        log.info("Membresia creada id={} vigente hasta {}", guardada.getId(), guardada.getFechaFin());
        return guardada;
    }

    public Membresia cancelar(Long id) {
        log.info("Cancelando membresia id={}", id);
        Membresia m = buscarPorId(id);
        if (m.getEstado() != EstadoMembresia.VIGENTE) {
            throw new ReglaNegocioException(
                    "Solo se pueden cancelar membresias VIGENTES. Estado actual: " + m.getEstado());
        }
        m.setEstado(EstadoMembresia.CANCELADA);
        return repo.save(m);
    }

    public void eliminar(Long id) {
        log.info("Eliminando membresia id={}", id);
        Membresia m = buscarPorId(id);
        repo.delete(m);
    }

    /**
     * Endpoint interno usado por otros microservicios:
     * devuelve si el socio tiene una membresia VIGENTE.
     */
    public boolean tieneVigente(Long socioId) {
        Optional<Membresia> ultima = repo.findFirstBySocioIdAndEstadoOrderByFechaFinDesc(
                socioId, EstadoMembresia.VIGENTE);
        return ultima.isPresent() && ultima.get().getFechaFin().isAfter(LocalDate.now().minusDays(1));
    }

    private SocioRespuesta obtenerSocioRemoto(Long socioId) {
        try {
            return socioClient.obtenerSocio(socioId);
        } catch (FeignException.NotFound e) {
            log.warn("Socio remoto no encontrado: {}", socioId);
            throw new RecursoNoEncontradoException("Socio con id " + socioId + " no existe en ms-socios");
        } catch (FeignException e) {
            log.error("Error consultando ms-socios: {}", e.getMessage());
            throw new ReglaNegocioException(
                    "No se pudo comunicar con el servicio de socios. Intente nuevamente.");
        }
    }
}

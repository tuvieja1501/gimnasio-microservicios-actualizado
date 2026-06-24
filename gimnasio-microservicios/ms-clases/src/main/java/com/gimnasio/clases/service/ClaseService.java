package com.gimnasio.clases.service;

import com.gimnasio.clases.client.InstructorClient;
import com.gimnasio.clases.client.SucursalClient;
import com.gimnasio.clases.dto.ClaseDTO;
import com.gimnasio.clases.dto.InstructorRespuesta;
import com.gimnasio.clases.dto.SucursalRespuesta;
import com.gimnasio.clases.exception.RecursoNoEncontradoException;
import com.gimnasio.clases.exception.ReglaNegocioException;
import com.gimnasio.clases.model.Clase;
import com.gimnasio.clases.repository.ClaseRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClaseService {

    private static final Logger log = LoggerFactory.getLogger(ClaseService.class);

    private final ClaseRepository repo;
    private final InstructorClient instructorClient;
    private final SucursalClient sucursalClient;

    public ClaseService(ClaseRepository repo,
                        InstructorClient instructorClient,
                        SucursalClient sucursalClient) {
        this.repo = repo;
        this.instructorClient = instructorClient;
        this.sucursalClient = sucursalClient;
    }

    public List<Clase> listar() { return repo.findAll(); }

    public List<Clase> listarFuturas() {
        return repo.findByFechaHoraAfter(LocalDateTime.now());
    }

    public Clase buscarPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Clase con id " + id + " no existe"));
    }

    public Clase crear(ClaseDTO dto) {
        log.info("Creando clase '{}' instructor={} sucursal={}",
                dto.getNombre(), dto.getInstructorId(), dto.getSucursalId());

        // Validar instructor remoto via Feign
        InstructorRespuesta instr = obtenerInstructor(dto.getInstructorId());
        if (!Boolean.TRUE.equals(instr.getActivo())) {
            throw new ReglaNegocioException("El instructor no esta activo");
        }

        // Validar sucursal remota via Feign
        SucursalRespuesta suc = obtenerSucursal(dto.getSucursalId());
        if (!Boolean.TRUE.equals(suc.getActiva())) {
            throw new ReglaNegocioException("La sucursal no esta activa");
        }
        if (dto.getCupoMaximo() > suc.getCapacidad()) {
            throw new ReglaNegocioException(
                    "El cupo (" + dto.getCupoMaximo() +
                    ") supera la capacidad de la sucursal (" + suc.getCapacidad() + ")");
        }

        Clase c = new Clase();
        c.setNombre(dto.getNombre().trim());
        c.setDescripcion(dto.getDescripcion());
        c.setInstructorId(dto.getInstructorId());
        c.setSucursalId(dto.getSucursalId());
        c.setFechaHora(dto.getFechaHora());
        c.setDuracionMinutos(dto.getDuracionMinutos());
        c.setCupoMaximo(dto.getCupoMaximo());
        c.setCuposDisponibles(dto.getCupoMaximo());

        return repo.save(c);
    }

    public Clase actualizar(Long id, ClaseDTO dto) {
        Clase c = buscarPorId(id);
        c.setNombre(dto.getNombre().trim());
        c.setDescripcion(dto.getDescripcion());
        c.setFechaHora(dto.getFechaHora());
        c.setDuracionMinutos(dto.getDuracionMinutos());
        // Si suben el cupo, agregamos a disponibles; si lo bajan, no afecta reservas existentes
        int delta = dto.getCupoMaximo() - c.getCupoMaximo();
        c.setCupoMaximo(dto.getCupoMaximo());
        c.setCuposDisponibles(Math.max(0, c.getCuposDisponibles() + delta));
        return repo.save(c);
    }

    public void eliminar(Long id) {
        Clase c = buscarPorId(id);
        repo.delete(c);
    }

    /**
     * Endpoint interno: decrementa los cupos disponibles tras una reserva exitosa.
     * Llamado por ms-reservas via Feign.
     */
    public Clase decrementarCupo(Long claseId) {
        Clase c = buscarPorId(claseId);
        if (c.getCuposDisponibles() <= 0) {
            throw new ReglaNegocioException("No quedan cupos disponibles para esta clase");
        }
        c.setCuposDisponibles(c.getCuposDisponibles() - 1);
        return repo.save(c);
    }

    public Clase incrementarCupo(Long claseId) {
        Clase c = buscarPorId(claseId);
        if (c.getCuposDisponibles() < c.getCupoMaximo()) {
            c.setCuposDisponibles(c.getCuposDisponibles() + 1);
        }
        return repo.save(c);
    }

    private InstructorRespuesta obtenerInstructor(Long id) {
        try {
            return instructorClient.obtenerInstructor(id);
        } catch (FeignException.NotFound e) {
            throw new RecursoNoEncontradoException("Instructor con id " + id + " no existe");
        } catch (FeignException e) {
            log.error("Error consultando ms-instructores: {}", e.getMessage());
            throw new ReglaNegocioException("No se pudo comunicar con ms-instructores");
        }
    }

    private SucursalRespuesta obtenerSucursal(Long id) {
        try {
            return sucursalClient.obtenerSucursal(id);
        } catch (FeignException.NotFound e) {
            throw new RecursoNoEncontradoException("Sucursal con id " + id + " no existe");
        } catch (FeignException e) {
            log.error("Error consultando ms-sucursales: {}", e.getMessage());
            throw new ReglaNegocioException("No se pudo comunicar con ms-sucursales");
        }
    }
}

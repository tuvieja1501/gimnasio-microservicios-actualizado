package com.gimnasio.rutinas.service;

import com.gimnasio.rutinas.client.InstructorClient;
import com.gimnasio.rutinas.client.SocioClient;
import com.gimnasio.rutinas.dto.EjercicioDTO;
import com.gimnasio.rutinas.dto.InstructorRespuesta;
import com.gimnasio.rutinas.dto.RutinaDTO;
import com.gimnasio.rutinas.dto.SocioRespuesta;
import com.gimnasio.rutinas.exception.RecursoNoEncontradoException;
import com.gimnasio.rutinas.exception.ReglaNegocioException;
import com.gimnasio.rutinas.model.Ejercicio;
import com.gimnasio.rutinas.model.Rutina;
import com.gimnasio.rutinas.repository.RutinaRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RutinaService {

    private static final Logger log = LoggerFactory.getLogger(RutinaService.class);

    private final RutinaRepository repo;
    private final SocioClient socioClient;
    private final InstructorClient instructorClient;

    public RutinaService(RutinaRepository repo,
                         SocioClient socioClient,
                         InstructorClient instructorClient) {
        this.repo = repo;
        this.socioClient = socioClient;
        this.instructorClient = instructorClient;
    }

    public List<Rutina> listar() { return repo.findAll(); }

    public List<Rutina> listarPorSocio(Long socioId) { return repo.findBySocioId(socioId); }

    public Rutina buscarPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Rutina con id " + id + " no existe"));
    }

    public Rutina crear(RutinaDTO dto) {
        log.info("Creando rutina '{}' para socio {} por instructor {}",
                dto.getNombre(), dto.getSocioId(), dto.getInstructorId());

        // Validar socio remoto
        SocioRespuesta socio = obtenerSocio(dto.getSocioId());
        if (!"ACTIVO".equals(socio.getEstado())) {
            throw new ReglaNegocioException("Solo se asignan rutinas a socios ACTIVOS");
        }

        // Validar instructor remoto
        InstructorRespuesta instr = obtenerInstructor(dto.getInstructorId());
        if (!Boolean.TRUE.equals(instr.getActivo())) {
            throw new ReglaNegocioException("El instructor no esta activo");
        }

        Rutina r = new Rutina();
        r.setNombre(dto.getNombre().trim());
        r.setObjetivo(dto.getObjetivo());
        r.setSocioId(dto.getSocioId());
        r.setInstructorId(dto.getInstructorId());
        r.setDuracionSemanas(dto.getDuracionSemanas());
        r.setFechaCreacion(LocalDate.now());

        for (EjercicioDTO ed : dto.getEjercicios()) {
            Ejercicio e = new Ejercicio();
            e.setNombre(ed.getNombre().trim());
            e.setSeries(ed.getSeries());
            e.setRepeticiones(ed.getRepeticiones());
            e.setDescansoSegundos(ed.getDescansoSegundos());
            e.setObservaciones(ed.getObservaciones());
            e.setRutina(r);
            r.getEjercicios().add(e);
        }

        return repo.save(r);
    }

    public Rutina actualizar(Long id, RutinaDTO dto) {
        Rutina r = buscarPorId(id);
        r.setNombre(dto.getNombre().trim());
        r.setObjetivo(dto.getObjetivo());
        r.setDuracionSemanas(dto.getDuracionSemanas());

        r.getEjercicios().clear();
        for (EjercicioDTO ed : dto.getEjercicios()) {
            Ejercicio e = new Ejercicio();
            e.setNombre(ed.getNombre().trim());
            e.setSeries(ed.getSeries());
            e.setRepeticiones(ed.getRepeticiones());
            e.setDescansoSegundos(ed.getDescansoSegundos());
            e.setObservaciones(ed.getObservaciones());
            e.setRutina(r);
            r.getEjercicios().add(e);
        }
        return repo.save(r);
    }

    public void eliminar(Long id) {
        Rutina r = buscarPorId(id);
        repo.delete(r);
    }

    private SocioRespuesta obtenerSocio(Long id) {
        try {
            return socioClient.obtenerSocio(id);
        } catch (FeignException.NotFound e) {
            throw new RecursoNoEncontradoException("Socio con id " + id + " no existe");
        } catch (FeignException e) {
            throw new ReglaNegocioException("Error consultando ms-socios");
        }
    }

    private InstructorRespuesta obtenerInstructor(Long id) {
        try {
            return instructorClient.obtenerInstructor(id);
        } catch (FeignException.NotFound e) {
            throw new RecursoNoEncontradoException("Instructor con id " + id + " no existe");
        } catch (FeignException e) {
            throw new ReglaNegocioException("Error consultando ms-instructores");
        }
    }
}

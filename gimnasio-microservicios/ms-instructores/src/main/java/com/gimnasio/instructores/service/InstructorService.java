package com.gimnasio.instructores.service;

import com.gimnasio.instructores.dto.InstructorDTO;
import com.gimnasio.instructores.exception.RecursoNoEncontradoException;
import com.gimnasio.instructores.exception.ReglaNegocioException;
import com.gimnasio.instructores.model.Instructor;
import com.gimnasio.instructores.repository.InstructorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InstructorService {

    private static final Logger log = LoggerFactory.getLogger(InstructorService.class);

    private final InstructorRepository repo;

    public InstructorService(InstructorRepository repo) {
        this.repo = repo;
    }

    public List<Instructor> listar() { return repo.findAll(); }

    public List<Instructor> listarActivos() { return repo.findByActivoTrue(); }

    public List<Instructor> buscarPorEspecialidad(String esp) {
        return repo.findByEspecialidadIgnoreCase(esp);
    }

    public Instructor buscarPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Instructor con id " + id + " no existe"));
    }

    public Instructor crear(InstructorDTO dto) {
        log.info("Creando instructor rut={}", dto.getRut());
        if (repo.existsByRut(dto.getRut())) {
            throw new ReglaNegocioException("Ya existe un instructor con RUT " + dto.getRut());
        }
        if (repo.existsByEmail(dto.getEmail())) {
            throw new ReglaNegocioException("Ya existe un instructor con email " + dto.getEmail());
        }
        Instructor i = new Instructor();
        aplicar(i, dto);
        return repo.save(i);
    }

    public Instructor actualizar(Long id, InstructorDTO dto) {
        log.info("Actualizando instructor id={}", id);
        Instructor i = buscarPorId(id);
        if (!i.getRut().equals(dto.getRut()) && repo.existsByRut(dto.getRut())) {
            throw new ReglaNegocioException("Ya existe otro instructor con RUT " + dto.getRut());
        }
        aplicar(i, dto);
        return repo.save(i);
    }

    public void eliminar(Long id) {
        Instructor i = buscarPorId(id);
        repo.delete(i);
    }

    private void aplicar(Instructor i, InstructorDTO dto) {
        i.setNombre(dto.getNombre().trim());
        i.setApellido(dto.getApellido().trim());
        i.setRut(dto.getRut());
        i.setEmail(dto.getEmail().toLowerCase());
        i.setEspecialidad(dto.getEspecialidad().trim());
        i.setAniosExperiencia(dto.getAniosExperiencia());
        i.setActivo(dto.getActivo());
    }
}

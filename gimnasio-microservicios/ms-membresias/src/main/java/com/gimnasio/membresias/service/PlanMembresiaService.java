package com.gimnasio.membresias.service;

import com.gimnasio.membresias.dto.PlanMembresiaDTO;
import com.gimnasio.membresias.exception.RecursoNoEncontradoException;
import com.gimnasio.membresias.model.PlanMembresia;
import com.gimnasio.membresias.repository.PlanMembresiaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanMembresiaService {

    private static final Logger log = LoggerFactory.getLogger(PlanMembresiaService.class);

    private final PlanMembresiaRepository repo;

    public PlanMembresiaService(PlanMembresiaRepository repo) {
        this.repo = repo;
    }

    public List<PlanMembresia> listar() {
        log.info("Listando planes de membresia");
        return repo.findAll();
    }

    public List<PlanMembresia> listarActivos() {
        log.info("Listando planes activos");
        return repo.findByActivoTrue();
    }

    public PlanMembresia buscarPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Plan con id " + id + " no existe"));
    }

    public PlanMembresia crear(PlanMembresiaDTO dto) {
        log.info("Creando plan {}", dto.getNombre());
        PlanMembresia p = new PlanMembresia();
        aplicar(p, dto);
        return repo.save(p);
    }

    public PlanMembresia actualizar(Long id, PlanMembresiaDTO dto) {
        log.info("Actualizando plan id={}", id);
        PlanMembresia p = buscarPorId(id);
        aplicar(p, dto);
        return repo.save(p);
    }

    public void eliminar(Long id) {
        log.info("Eliminando plan id={}", id);
        PlanMembresia p = buscarPorId(id);
        repo.delete(p);
    }

    private void aplicar(PlanMembresia p, PlanMembresiaDTO dto) {
        p.setNombre(dto.getNombre().trim());
        p.setDescripcion(dto.getDescripcion());
        p.setDuracionMeses(dto.getDuracionMeses());
        p.setPrecio(dto.getPrecio());
        p.setActivo(dto.getActivo());
    }
}

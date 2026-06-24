package com.gimnasio.socios.service;

import com.gimnasio.socios.dto.SocioDTO;
import com.gimnasio.socios.exception.RecursoNoEncontradoException;
import com.gimnasio.socios.exception.ReglaNegocioException;
import com.gimnasio.socios.model.EstadoSocio;
import com.gimnasio.socios.model.Socio;
import com.gimnasio.socios.repository.SocioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * Logica de negocio para Socio.
 * Encapsula validaciones de dominio, transformacion DTO <-> entidad
 * y reglas de negocio (edad minima, unicidad de RUT/email, etc.).
 */
@Service
public class SocioService {

    private static final Logger log = LoggerFactory.getLogger(SocioService.class);
    private static final int EDAD_MINIMA = 14;

    private final SocioRepository repo;

    public SocioService(SocioRepository repo) {
        this.repo = repo;
    }

    public List<Socio> listar() {
        log.info("Listando todos los socios");
        return repo.findAll();
    }

    public Socio buscarPorId(Long id) {
        log.debug("Buscando socio con id={}", id);
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Socio con id " + id + " no existe"));
    }

    public Socio buscarPorRut(String rut) {
        log.debug("Buscando socio con rut={}", rut);
        return repo.findByRut(rut)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Socio con RUT " + rut + " no existe"));
    }

    public Socio crear(SocioDTO dto) {
        log.info("Creando nuevo socio rut={}", dto.getRut());
        validarReglasCreacion(dto);

        Socio s = new Socio();
        s.setNombre(dto.getNombre().trim());
        s.setApellido(dto.getApellido().trim());
        s.setRut(dto.getRut());
        s.setEmail(dto.getEmail().toLowerCase());
        s.setTelefono(dto.getTelefono());
        s.setFechaNacimiento(dto.getFechaNacimiento());
        s.setFechaRegistro(LocalDate.now());
        s.setEstado(dto.getEstado());

        Socio guardado = repo.save(s);
        log.info("Socio creado con id={}", guardado.getId());
        return guardado;
    }

    public Socio actualizar(Long id, SocioDTO dto) {
        log.info("Actualizando socio id={}", id);
        Socio existente = buscarPorId(id);

        // Validar unicidad solo si el campo cambia
        if (!existente.getRut().equals(dto.getRut()) && repo.existsByRut(dto.getRut())) {
            throw new ReglaNegocioException("Ya existe otro socio con el RUT " + dto.getRut());
        }
        if (!existente.getEmail().equalsIgnoreCase(dto.getEmail()) && repo.existsByEmail(dto.getEmail())) {
            throw new ReglaNegocioException("Ya existe otro socio con el email " + dto.getEmail());
        }

        existente.setNombre(dto.getNombre().trim());
        existente.setApellido(dto.getApellido().trim());
        existente.setRut(dto.getRut());
        existente.setEmail(dto.getEmail().toLowerCase());
        existente.setTelefono(dto.getTelefono());
        existente.setFechaNacimiento(dto.getFechaNacimiento());
        existente.setEstado(dto.getEstado());

        return repo.save(existente);
    }

    public void eliminar(Long id) {
        log.info("Eliminando socio id={}", id);
        Socio s = buscarPorId(id);
        repo.delete(s);
    }

    public Socio cambiarEstado(Long id, EstadoSocio nuevoEstado) {
        log.info("Cambiando estado socio id={} -> {}", id, nuevoEstado);
        Socio s = buscarPorId(id);
        s.setEstado(nuevoEstado);
        return repo.save(s);
    }

    private void validarReglasCreacion(SocioDTO dto) {
        if (repo.existsByRut(dto.getRut())) {
            throw new ReglaNegocioException("Ya existe un socio con el RUT " + dto.getRut());
        }
        if (repo.existsByEmail(dto.getEmail())) {
            throw new ReglaNegocioException("Ya existe un socio con el email " + dto.getEmail());
        }
        if (dto.getFechaNacimiento() != null) {
            int edad = Period.between(dto.getFechaNacimiento(), LocalDate.now()).getYears();
            if (edad < EDAD_MINIMA) {
                throw new ReglaNegocioException(
                        "El socio debe tener al menos " + EDAD_MINIMA + " anios. Edad actual: " + edad);
            }
        }
    }
}

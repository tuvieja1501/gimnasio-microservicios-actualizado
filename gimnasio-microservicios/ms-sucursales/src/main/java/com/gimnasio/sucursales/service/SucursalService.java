package com.gimnasio.sucursales.service;

import com.gimnasio.sucursales.dto.SucursalDTO;
import com.gimnasio.sucursales.exception.RecursoNoEncontradoException;
import com.gimnasio.sucursales.exception.ReglaNegocioException;
import com.gimnasio.sucursales.model.Sucursal;
import com.gimnasio.sucursales.repository.SucursalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SucursalService {

    private static final Logger log = LoggerFactory.getLogger(SucursalService.class);

    private final SucursalRepository repo;

    public SucursalService(SucursalRepository repo) {
        this.repo = repo;
    }

    public List<Sucursal> listar() {
        log.info("Listando todas las sucursales");
        return repo.findAll();
    }

    public List<Sucursal> listarActivas() { return repo.findByActivaTrue(); }

    public List<Sucursal> listarPorComuna(String comuna) { return repo.findByComunaIgnoreCase(comuna); }

    public Sucursal buscarPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Sucursal con id " + id + " no existe"));
    }

    public Sucursal crear(SucursalDTO dto) {
        log.info("Creando sucursal {}", dto.getNombre());
        if (repo.existsByNombre(dto.getNombre())) {
            throw new ReglaNegocioException("Ya existe una sucursal con el nombre " + dto.getNombre());
        }
        validarHorarios(dto);
        Sucursal s = new Sucursal();
        aplicar(s, dto);
        return repo.save(s);
    }

    public Sucursal actualizar(Long id, SucursalDTO dto) {
        log.info("Actualizando sucursal id={}", id);
        Sucursal s = buscarPorId(id);
        if (!s.getNombre().equals(dto.getNombre()) && repo.existsByNombre(dto.getNombre())) {
            throw new ReglaNegocioException("Ya existe otra sucursal con el nombre " + dto.getNombre());
        }
        validarHorarios(dto);
        aplicar(s, dto);
        return repo.save(s);
    }

    public Sucursal cambiarEstado(Long id, Boolean activa) {
        Sucursal s = buscarPorId(id);
        s.setActiva(activa);
        return repo.save(s);
    }

    public void eliminar(Long id) {
        log.info("Eliminando sucursal id={}", id);
        Sucursal s = buscarPorId(id);
        repo.delete(s);
    }

    private void validarHorarios(SucursalDTO dto) {
        if (!dto.getHoraApertura().isBefore(dto.getHoraCierre())) {
            throw new ReglaNegocioException(
                    "La hora de apertura debe ser anterior a la hora de cierre");
        }
    }

    private void aplicar(Sucursal s, SucursalDTO dto) {
        s.setNombre(dto.getNombre().trim());
        s.setDireccion(dto.getDireccion().trim());
        s.setComuna(dto.getComuna().trim());
        s.setTelefono(dto.getTelefono());
        s.setCapacidad(dto.getCapacidad());
        s.setHoraApertura(dto.getHoraApertura());
        s.setHoraCierre(dto.getHoraCierre());
        s.setActiva(dto.getActiva());
    }
}

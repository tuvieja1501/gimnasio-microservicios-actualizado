package com.gimnasio.equipos.service;

import com.gimnasio.equipos.client.SucursalClient;
import com.gimnasio.equipos.dto.EquipoDTO;
import com.gimnasio.equipos.dto.SucursalRespuesta;
import com.gimnasio.equipos.exception.RecursoNoEncontradoException;
import com.gimnasio.equipos.exception.ReglaNegocioException;
import com.gimnasio.equipos.model.Equipo;
import com.gimnasio.equipos.model.EstadoEquipo;
import com.gimnasio.equipos.repository.EquipoRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipoService {

    private static final Logger log = LoggerFactory.getLogger(EquipoService.class);

    private final EquipoRepository repo;
    private final SucursalClient sucursalClient;

    public EquipoService(EquipoRepository repo, SucursalClient sucursalClient) {
        this.repo = repo;
        this.sucursalClient = sucursalClient;
    }

    public List<Equipo> listar() { return repo.findAll(); }

    public List<Equipo> listarPorSucursal(Long sucursalId) { return repo.findBySucursalId(sucursalId); }

    public Equipo buscarPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Equipo con id " + id + " no existe"));
    }

    public Equipo crear(EquipoDTO dto) {
        log.info("Creando equipo codigo={}", dto.getCodigoInterno());

        if (repo.existsByCodigoInterno(dto.getCodigoInterno())) {
            throw new ReglaNegocioException(
                    "Ya existe un equipo con codigo interno " + dto.getCodigoInterno());
        }

        // Validar sucursal remota
        SucursalRespuesta suc = obtenerSucursal(dto.getSucursalId());
        if (!Boolean.TRUE.equals(suc.getActiva())) {
            throw new ReglaNegocioException("La sucursal no esta activa, no se pueden registrar equipos en ella");
        }

        Equipo e = new Equipo();
        aplicar(e, dto);
        return repo.save(e);
    }

    public Equipo actualizar(Long id, EquipoDTO dto) {
        Equipo e = buscarPorId(id);
        if (!e.getCodigoInterno().equals(dto.getCodigoInterno())
                && repo.existsByCodigoInterno(dto.getCodigoInterno())) {
            throw new ReglaNegocioException("Codigo interno duplicado");
        }
        aplicar(e, dto);
        return repo.save(e);
    }

    public Equipo cambiarEstado(Long id, EstadoEquipo estado) {
        Equipo e = buscarPorId(id);
        e.setEstado(estado);
        return repo.save(e);
    }

    public void eliminar(Long id) {
        Equipo e = buscarPorId(id);
        repo.delete(e);
    }

    private void aplicar(Equipo e, EquipoDTO dto) {
        e.setNombre(dto.getNombre().trim());
        e.setTipo(dto.getTipo().trim());
        e.setCodigoInterno(dto.getCodigoInterno().trim());
        e.setSucursalId(dto.getSucursalId());
        e.setFechaAdquisicion(dto.getFechaAdquisicion());
        e.setEstado(dto.getEstado());
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

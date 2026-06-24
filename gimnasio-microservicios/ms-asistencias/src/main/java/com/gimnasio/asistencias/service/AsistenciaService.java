package com.gimnasio.asistencias.service;

import com.gimnasio.asistencias.client.SocioClient;
import com.gimnasio.asistencias.client.SucursalClient;
import com.gimnasio.asistencias.dto.AsistenciaDTO;
import com.gimnasio.asistencias.dto.SocioRespuesta;
import com.gimnasio.asistencias.dto.SucursalRespuesta;
import com.gimnasio.asistencias.exception.RecursoNoEncontradoException;
import com.gimnasio.asistencias.exception.ReglaNegocioException;
import com.gimnasio.asistencias.model.Asistencia;
import com.gimnasio.asistencias.repository.AsistenciaRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AsistenciaService {

    private static final Logger log = LoggerFactory.getLogger(AsistenciaService.class);

    private final AsistenciaRepository repo;
    private final SocioClient socioClient;
    private final SucursalClient sucursalClient;

    public AsistenciaService(AsistenciaRepository repo,
                             SocioClient socioClient,
                             SucursalClient sucursalClient) {
        this.repo = repo;
        this.socioClient = socioClient;
        this.sucursalClient = sucursalClient;
    }

    public List<Asistencia> listar() { return repo.findAll(); }

    public List<Asistencia> listarPorSocio(Long socioId) { return repo.findBySocioId(socioId); }

    public Asistencia buscarPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Asistencia con id " + id + " no existe"));
    }

    public Asistencia registrarIngreso(AsistenciaDTO dto) {
        log.info("Registrando ingreso socio={} sucursal={}", dto.getSocioId(), dto.getSucursalId());

        // Validar socio
        SocioRespuesta socio = obtenerSocio(dto.getSocioId());
        if (!"ACTIVO".equals(socio.getEstado())) {
            throw new ReglaNegocioException(
                    "El socio no esta ACTIVO. Estado actual: " + socio.getEstado());
        }

        // Validar sucursal
        SucursalRespuesta suc = obtenerSucursal(dto.getSucursalId());
        if (!Boolean.TRUE.equals(suc.getActiva())) {
            throw new ReglaNegocioException("La sucursal no esta activa");
        }

        // Validar que no tenga ingreso abierto
        repo.findFirstBySocioIdAndFechaSalidaIsNullOrderByFechaIngresoDesc(dto.getSocioId())
                .ifPresent(a -> {
                    throw new ReglaNegocioException(
                            "El socio ya tiene un ingreso abierto (id=" + a.getId() +
                            "). Debe registrar salida antes.");
                });

        Asistencia a = new Asistencia();
        a.setSocioId(dto.getSocioId());
        a.setSucursalId(dto.getSucursalId());
        a.setFechaIngreso(LocalDateTime.now());
        return repo.save(a);
    }

    public Asistencia registrarSalida(Long socioId) {
        log.info("Registrando salida para socio={}", socioId);
        Asistencia abierta = repo.findFirstBySocioIdAndFechaSalidaIsNullOrderByFechaIngresoDesc(socioId)
                .orElseThrow(() -> new ReglaNegocioException(
                        "El socio no tiene un ingreso abierto"));
        abierta.setFechaSalida(LocalDateTime.now());
        return repo.save(abierta);
    }

    public void eliminar(Long id) {
        Asistencia a= buscarPorId(id);
        repo.delete(a);
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

    private SucursalRespuesta obtenerSucursal(Long id) {
        try {
            return sucursalClient.obtenerSucursal(id);
        } catch (FeignException.NotFound e) {
            throw new RecursoNoEncontradoException("Sucursal con id " + id + " no existe");
        } catch (FeignException e) {
            throw new ReglaNegocioException("Error consultando ms-sucursales");
        }
    }
}

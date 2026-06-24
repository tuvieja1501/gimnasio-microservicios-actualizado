package com.gimnasio.asistencias.service;

import com.gimnasio.asistencias.client.SocioClient;
import com.gimnasio.asistencias.client.SucursalClient;
import com.gimnasio.asistencias.dto.AsistenciaDTO;
import com.gimnasio.asistencias.dto.SocioRespuesta;
import com.gimnasio.asistencias.dto.SucursalRespuesta;
import com.gimnasio.asistencias.exception.ReglaNegocioException;
import com.gimnasio.asistencias.model.Asistencia;
import com.gimnasio.asistencias.repository.AsistenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AsistenciaService - pruebas unitarias")
class AsistenciaServiceTest {

    @Mock
    private AsistenciaRepository repo;
    @Mock
    private SocioClient socioClient;
    @Mock
    private SucursalClient sucursalClient;

    @InjectMocks
    private AsistenciaService service;

    private AsistenciaDTO dto;
    private SocioRespuesta socioActivo;
    private SucursalRespuesta sucursalActiva;

    @BeforeEach
    void setUp() {
        dto = new AsistenciaDTO();
        dto.setSocioId(10L);
        dto.setSucursalId(1L);

        socioActivo = new SocioRespuesta();
        socioActivo.setId(10L);
        socioActivo.setEstado("ACTIVO");

        sucursalActiva = new SucursalRespuesta();
        sucursalActiva.setId(1L);
        sucursalActiva.setActiva(true);
    }

    @Test
    @DisplayName("registra ingreso cuando socio activo, sucursal activa y sin ingreso abierto")
    void registraIngresoConDatosValidos() {
        when(socioClient.obtenerSocio(10L)).thenReturn(socioActivo);
        when(sucursalClient.obtenerSucursal(1L)).thenReturn(sucursalActiva);
        when(repo.findFirstBySocioIdAndFechaSalidaIsNullOrderByFechaIngresoDesc(10L))
                .thenReturn(Optional.empty());
        when(repo.save(any(Asistencia.class))).thenAnswer(inv -> {
            Asistencia a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        Asistencia resultado = service.registrarIngreso(dto);

        assertThat(resultado.getFechaIngreso()).isNotNull();
        assertThat(resultado.getFechaSalida()).isNull();
    }

    @Test
    @DisplayName("rechaza si el socio ya tiene un ingreso abierto")
    void rechazaIngresoDuplicado() {
        Asistencia abierta = new Asistencia();
        abierta.setId(5L);
        when(socioClient.obtenerSocio(10L)).thenReturn(socioActivo);
        when(sucursalClient.obtenerSucursal(1L)).thenReturn(sucursalActiva);
        when(repo.findFirstBySocioIdAndFechaSalidaIsNullOrderByFechaIngresoDesc(10L))
                .thenReturn(Optional.of(abierta));

        assertThatThrownBy(() -> service.registrarIngreso(dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("5");

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("rechaza si el socio no esta ACTIVO")
    void rechazaSocioInactivo() {
        socioActivo.setEstado("MOROSO");
        when(socioClient.obtenerSocio(10L)).thenReturn(socioActivo);

        assertThatThrownBy(() -> service.registrarIngreso(dto))
                .isInstanceOf(ReglaNegocioException.class);

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("registrarSalida() cierra el ingreso abierto mas reciente")
    void registraSalidaCorrectamente() {
        Asistencia abierta = new Asistencia();
        abierta.setId(1L);
        abierta.setSocioId(10L);
        abierta.setFechaIngreso(LocalDateTime.now().minusHours(1));
        when(repo.findFirstBySocioIdAndFechaSalidaIsNullOrderByFechaIngresoDesc(10L))
                .thenReturn(Optional.of(abierta));
        when(repo.save(any(Asistencia.class))).thenAnswer(inv -> inv.getArgument(0));

        Asistencia resultado = service.registrarSalida(10L);

        assertThat(resultado.getFechaSalida()).isNotNull();
    }

    @Test
    @DisplayName("rechaza si la sucursal no esta activa")
    void rechazaSucursalInactiva() {
        sucursalActiva.setActiva(false);
        when(socioClient.obtenerSocio(10L)).thenReturn(socioActivo);
        when(sucursalClient.obtenerSucursal(1L)).thenReturn(sucursalActiva);

        assertThatThrownBy(() -> service.registrarIngreso(dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("sucursal");

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("listar() retorna todas las asistencias")
    void listarRetornaTodas() {
        when(repo.findAll()).thenReturn(java.util.List.of(new Asistencia(), new Asistencia()));

        assertThat(service.listar()).hasSize(2);
    }

    @Test
    @DisplayName("listarPorSocio() delega en el repositorio")
    void listarPorSocioDelegaEnRepo() {
        Asistencia a = new Asistencia();
        a.setSocioId(10L);
        when(repo.findBySocioId(10L)).thenReturn(java.util.List.of(a));

        assertThat(service.listarPorSocio(10L)).hasSize(1);
        verify(repo).findBySocioId(10L);
    }

    @Test
    @DisplayName("buscarPorId() retorna la asistencia cuando existe")
    void buscarPorIdExistente() {
        Asistencia a = new Asistencia();
        a.setId(7L);
        when(repo.findById(7L)).thenReturn(Optional.of(a));

        assertThat(service.buscarPorId(7L).getId()).isEqualTo(7L);
    }

    @Test
    @DisplayName("buscarPorId() lanza excepcion si no existe")
    void buscarPorIdInexistente() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(com.gimnasio.asistencias.exception.RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("eliminar() borra la asistencia cuando existe")
    void eliminarExistente() {
        Asistencia a = new Asistencia();
        a.setId(3L);
        when(repo.findById(3L)).thenReturn(Optional.of(a));

        service.eliminar(3L);

        verify(repo).delete(a);
    }

    @Test
    @DisplayName("eliminar() lanza excepcion si la asistencia no existe")
    void eliminarInexistente() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminar(99L))
                .isInstanceOf(com.gimnasio.asistencias.exception.RecursoNoEncontradoException.class);

        verify(repo, never()).delete(any());
    }

    private feign.Response respuestaFeign(int status) {
        feign.Request request = feign.Request.create(
                feign.Request.HttpMethod.GET, "/api/recurso",
                java.util.Collections.emptyMap(), null,
                java.nio.charset.Charset.defaultCharset(), null);
        return feign.Response.builder()
                .status(status)
                .reason(status == 404 ? "Not Found" : "Internal Server Error")
                .request(request)
                .headers(java.util.Collections.emptyMap())
                .build();
    }

    @Test
    @DisplayName("traduce un 404 de ms-socios en RecursoNoEncontradoException")
    void socioNoEncontradoViaFeign() {
        feign.FeignException notFound = feign.FeignException.errorStatus(
                "SocioClient#obtenerSocio(Long)", respuestaFeign(404));
        when(socioClient.obtenerSocio(10L)).thenThrow(notFound);

        assertThatThrownBy(() -> service.registrarIngreso(dto))
                .isInstanceOf(com.gimnasio.asistencias.exception.RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("traduce un error generico de ms-socios en ReglaNegocioException")
    void errorGenericoConsultandoSocio() {
        feign.FeignException error = feign.FeignException.errorStatus(
                "SocioClient#obtenerSocio(Long)", respuestaFeign(500));
        when(socioClient.obtenerSocio(10L)).thenThrow(error);

        assertThatThrownBy(() -> service.registrarIngreso(dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ms-socios");
    }

    @Test
    @DisplayName("traduce un 404 de ms-sucursales en RecursoNoEncontradoException")
    void sucursalNoEncontradaViaFeign() {
        when(socioClient.obtenerSocio(10L)).thenReturn(socioActivo);
        feign.FeignException notFound = feign.FeignException.errorStatus(
                "SucursalClient#obtenerSucursal(Long)", respuestaFeign(404));
        when(sucursalClient.obtenerSucursal(1L)).thenThrow(notFound);

        assertThatThrownBy(() -> service.registrarIngreso(dto))
                .isInstanceOf(com.gimnasio.asistencias.exception.RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("traduce un error generico de ms-sucursales en ReglaNegocioException")
    void errorGenericoConsultandoSucursal() {
        when(socioClient.obtenerSocio(10L)).thenReturn(socioActivo);
        feign.FeignException error = feign.FeignException.errorStatus(
                "SucursalClient#obtenerSucursal(Long)", respuestaFeign(500));
        when(sucursalClient.obtenerSucursal(1L)).thenThrow(error);

        assertThatThrownBy(() -> service.registrarIngreso(dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("ms-sucursales");
    }
}

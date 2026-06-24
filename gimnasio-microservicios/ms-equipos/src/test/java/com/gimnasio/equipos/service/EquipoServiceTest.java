package com.gimnasio.equipos.service;

import com.gimnasio.equipos.client.SucursalClient;
import com.gimnasio.equipos.dto.EquipoDTO;
import com.gimnasio.equipos.dto.SucursalRespuesta;
import com.gimnasio.equipos.exception.ReglaNegocioException;
import com.gimnasio.equipos.model.Equipo;
import com.gimnasio.equipos.model.EstadoEquipo;
import com.gimnasio.equipos.repository.EquipoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EquipoService - pruebas unitarias")
class EquipoServiceTest {

    @Mock
    private EquipoRepository repo;

    @Mock
    private SucursalClient sucursalClient;

    @InjectMocks
    private EquipoService service;

    private EquipoDTO dto;
    private SucursalRespuesta sucursalActiva;

    @BeforeEach
    void setUp() {
        dto = new EquipoDTO();
        dto.setNombre("Cinta de correr");
        dto.setTipo("Cardio");
        dto.setCodigoInterno("EQ-001");
        dto.setSucursalId(1L);
        dto.setFechaAdquisicion(LocalDate.now().minusMonths(2));
        dto.setEstado(EstadoEquipo.OPERATIVO);

        sucursalActiva = new SucursalRespuesta();
        sucursalActiva.setId(1L);
        sucursalActiva.setActiva(true);
    }

    @Test
    @DisplayName("crea equipo cuando codigo es unico y sucursal esta activa")
    void creaEquipoConDatosValidos() {
        when(repo.existsByCodigoInterno(dto.getCodigoInterno())).thenReturn(false);
        when(sucursalClient.obtenerSucursal(1L)).thenReturn(sucursalActiva);
        when(repo.save(any(Equipo.class))).thenAnswer(inv -> {
            Equipo e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        Equipo resultado = service.crear(dto);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getCodigoInterno()).isEqualTo("EQ-001");
    }

    @Test
    @DisplayName("rechaza codigo interno duplicado")
    void rechazaCodigoDuplicado() {
        when(repo.existsByCodigoInterno(dto.getCodigoInterno())).thenReturn(true);

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(ReglaNegocioException.class);

        verify(repo, never()).save(any());
        verifyNoInteractions(sucursalClient);
    }

    @Test
    @DisplayName("rechaza si la sucursal remota no esta activa")
    void rechazaSucursalInactiva() {
        sucursalActiva.setActiva(false);
        when(repo.existsByCodigoInterno(dto.getCodigoInterno())).thenReturn(false);
        when(sucursalClient.obtenerSucursal(1L)).thenReturn(sucursalActiva);

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(ReglaNegocioException.class);

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("cambiarEstado() actualiza el estado del equipo")
    void cambiarEstadoActualiza() {
        Equipo existente = equipoGuardado(1L);
        when(repo.findById(1L)).thenReturn(Optional.of(existente));
        when(repo.save(any(Equipo.class))).thenAnswer(inv -> inv.getArgument(0));

        Equipo resultado = service.cambiarEstado(1L, EstadoEquipo.EN_MANTENIMIENTO);

        assertThat(resultado.getEstado()).isEqualTo(EstadoEquipo.EN_MANTENIMIENTO);
    }

    @Test
    @DisplayName("eliminar() borra el equipo existente")
    void eliminarEquipoExistente() {
        Equipo existente = equipoGuardado(1L);
        when(repo.findById(1L)).thenReturn(Optional.of(existente));

        service.eliminar(1L);

        verify(repo).delete(existente);
    }

    @Test
    @DisplayName("listar() retorna todos los equipos")
    void listarRetornaTodos() {
        when(repo.findAll()).thenReturn(java.util.List.of(equipoGuardado(1L), equipoGuardado(2L)));

        assertThat(service.listar()).hasSize(2);
    }

    @Test
    @DisplayName("listarPorSucursal() delega en el repositorio")
    void listarPorSucursalDelegaEnRepo() {
        when(repo.findBySucursalId(1L)).thenReturn(java.util.List.of(equipoGuardado(1L)));

        assertThat(service.listarPorSucursal(1L)).hasSize(1);
        verify(repo).findBySucursalId(1L);
    }

    @Test
    @DisplayName("buscarPorId() retorna el equipo cuando existe")
    void buscarPorIdExistente() {
        when(repo.findById(1L)).thenReturn(Optional.of(equipoGuardado(1L)));

        assertThat(service.buscarPorId(1L).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("buscarPorId() lanza excepcion si el equipo no existe")
    void buscarPorIdInexistente() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(com.gimnasio.equipos.exception.RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("actualizar() modifica el equipo cuando el codigo interno no cambia")
    void actualizarSinCambiarCodigo() {
        Equipo existente = equipoGuardado(1L); // codigo EQ-099
        dto.setCodigoInterno("EQ-099");
        when(repo.findById(1L)).thenReturn(Optional.of(existente));
        when(repo.save(any(Equipo.class))).thenAnswer(inv -> inv.getArgument(0));

        Equipo resultado = service.actualizar(1L, dto);

        assertThat(resultado.getNombre()).isEqualTo("Cinta de correr");
        verify(repo, never()).existsByCodigoInterno(anyString());
    }

    @Test
    @DisplayName("actualizar() permite cambiar a un codigo interno disponible")
    void actualizarConCodigoNuevoDisponible() {
        Equipo existente = equipoGuardado(1L); // codigo EQ-099
        dto.setCodigoInterno("EQ-555");
        when(repo.findById(1L)).thenReturn(Optional.of(existente));
        when(repo.existsByCodigoInterno("EQ-555")).thenReturn(false);
        when(repo.save(any(Equipo.class))).thenAnswer(inv -> inv.getArgument(0));

        Equipo resultado = service.actualizar(1L, dto);

        assertThat(resultado.getCodigoInterno()).isEqualTo("EQ-555");
    }

    @Test
    @DisplayName("actualizar() lanza excepcion si el nuevo codigo interno ya esta en uso")
    void actualizarConCodigoDuplicado() {
        Equipo existente = equipoGuardado(1L); // codigo EQ-099
        dto.setCodigoInterno("EQ-555");
        when(repo.findById(1L)).thenReturn(Optional.of(existente));
        when(repo.existsByCodigoInterno("EQ-555")).thenReturn(true);

        assertThatThrownBy(() -> service.actualizar(1L, dto))
                .isInstanceOf(ReglaNegocioException.class);

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("actualizar() lanza excepcion si el equipo no existe")
    void actualizarEquipoInexistente() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizar(99L, dto))
                .isInstanceOf(com.gimnasio.equipos.exception.RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("cambiarEstado() lanza excepcion si el equipo no existe")
    void cambiarEstadoEquipoInexistente() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cambiarEstado(99L, EstadoEquipo.DADO_DE_BAJA))
                .isInstanceOf(com.gimnasio.equipos.exception.RecursoNoEncontradoException.class);

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("eliminar() lanza excepcion si el equipo no existe")
    void eliminarEquipoInexistente() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminar(99L))
                .isInstanceOf(com.gimnasio.equipos.exception.RecursoNoEncontradoException.class);

        verify(repo, never()).delete(any());
    }

    @Test
    @DisplayName("traduce un 404 de ms-sucursales en RecursoNoEncontradoException")
    void sucursalNoEncontradaViaFeign() {
        when(repo.existsByCodigoInterno(dto.getCodigoInterno())).thenReturn(false);
        feign.Request request = feign.Request.create(
                feign.Request.HttpMethod.GET, "/api/sucursales/1",
                java.util.Collections.emptyMap(), null,
                java.nio.charset.Charset.defaultCharset(), null);
        feign.Response response = feign.Response.builder()
                .status(404).reason("Not Found").request(request)
                .headers(java.util.Collections.emptyMap()).build();
        feign.FeignException notFound = feign.FeignException.errorStatus(
                "SucursalClient#obtenerSucursal(Long)", response);
        when(sucursalClient.obtenerSucursal(1L)).thenThrow(notFound);

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(com.gimnasio.equipos.exception.RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("traduce un error generico de ms-sucursales en ReglaNegocioException")
    void errorGenericoConsultandoSucursal() {
        when(repo.existsByCodigoInterno(dto.getCodigoInterno())).thenReturn(false);
        feign.Request request = feign.Request.create(
                feign.Request.HttpMethod.GET, "/api/sucursales/1",
                java.util.Collections.emptyMap(), null,
                java.nio.charset.Charset.defaultCharset(), null);
        feign.Response response = feign.Response.builder()
                .status(500).reason("Internal Server Error").request(request)
                .headers(java.util.Collections.emptyMap()).build();
        feign.FeignException error = feign.FeignException.errorStatus(
                "SucursalClient#obtenerSucursal(Long)", response);
        when(sucursalClient.obtenerSucursal(1L)).thenThrow(error);

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(ReglaNegocioException.class);
    }

    private Equipo equipoGuardado(Long id) {
        Equipo e = new Equipo();
        e.setId(id);
        e.setNombre("Bicicleta estatica");
        e.setTipo("Cardio");
        e.setCodigoInterno("EQ-099");
        e.setSucursalId(1L);
        e.setFechaAdquisicion(LocalDate.now().minusYears(1));
        e.setEstado(EstadoEquipo.OPERATIVO);
        return e;
    }
}

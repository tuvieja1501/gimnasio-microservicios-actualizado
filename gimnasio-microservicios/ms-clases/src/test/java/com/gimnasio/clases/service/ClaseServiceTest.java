package com.gimnasio.clases.service;

import com.gimnasio.clases.client.InstructorClient;
import com.gimnasio.clases.client.SucursalClient;
import com.gimnasio.clases.dto.ClaseDTO;
import com.gimnasio.clases.dto.InstructorRespuesta;
import com.gimnasio.clases.dto.SucursalRespuesta;
import com.gimnasio.clases.exception.ReglaNegocioException;
import com.gimnasio.clases.model.Clase;
import com.gimnasio.clases.repository.ClaseRepository;
import feign.FeignException;
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
@DisplayName("ClaseService - pruebas unitarias")
class ClaseServiceTest {

    @Mock
    private ClaseRepository repo;

    @Mock
    private InstructorClient instructorClient;

    @Mock
    private SucursalClient sucursalClient;

    @InjectMocks
    private ClaseService service;

    private ClaseDTO dto;
    private InstructorRespuesta instructorActivo;
    private SucursalRespuesta sucursalActiva;

    @BeforeEach
    void setUp() {
        dto = new ClaseDTO();
        dto.setNombre("Spinning");
        dto.setInstructorId(1L);
        dto.setSucursalId(1L);
        dto.setFechaHora(LocalDateTime.now().plusDays(1));
        dto.setDuracionMinutos(45);
        dto.setCupoMaximo(20);

        instructorActivo = new InstructorRespuesta();
        instructorActivo.setId(1L);
        instructorActivo.setActivo(true);

        sucursalActiva = new SucursalRespuesta();
        sucursalActiva.setId(1L);
        sucursalActiva.setActiva(true);
        sucursalActiva.setCapacidad(30);
    }

    @Test
    @DisplayName("crea clase cuando instructor activo, sucursal activa y cupo dentro de capacidad")
    void creaClaseConDatosValidos() {
        when(instructorClient.obtenerInstructor(1L)).thenReturn(instructorActivo);
        when(sucursalClient.obtenerSucursal(1L)).thenReturn(sucursalActiva);
        when(repo.save(any(Clase.class))).thenAnswer(inv -> {
            Clase c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        Clase resultado = service.crear(dto);

        assertThat(resultado.getCuposDisponibles()).isEqualTo(20);
        assertThat(resultado.getCupoMaximo()).isEqualTo(20);
    }

    @Test
    @DisplayName("rechaza si el instructor remoto no esta activo")
    void rechazaInstructorInactivo() {
        instructorActivo.setActivo(false);
        when(instructorClient.obtenerInstructor(1L)).thenReturn(instructorActivo);

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("instructor");

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("rechaza si la sucursal remota no esta activa")
    void rechazaSucursalInactiva() {
        sucursalActiva.setActiva(false);
        when(instructorClient.obtenerInstructor(1L)).thenReturn(instructorActivo);
        when(sucursalClient.obtenerSucursal(1L)).thenReturn(sucursalActiva);

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("sucursal");

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("rechaza si el cupo solicitado supera la capacidad de la sucursal")
    void rechazaCupoSuperaCapacidad() {
        dto.setCupoMaximo(50); // > capacidad 30
        when(instructorClient.obtenerInstructor(1L)).thenReturn(instructorActivo);
        when(sucursalClient.obtenerSucursal(1L)).thenReturn(sucursalActiva);

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("capacidad");

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("traduce error de Feign al consultar instructor")
    void traduceErrorFeignInstructor() {
        when(instructorClient.obtenerInstructor(1L)).thenThrow(mock(FeignException.class));

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("decrementarCupo() reduce los cupos disponibles en 1")
    void decrementarCupoReduceDisponibles() {
        Clase c = claseGuardada(1L, 5, 20);
        when(repo.findById(1L)).thenReturn(Optional.of(c));
        when(repo.save(any(Clase.class))).thenAnswer(inv -> inv.getArgument(0));

        Clase resultado = service.decrementarCupo(1L);

        assertThat(resultado.getCuposDisponibles()).isEqualTo(4);
    }

    @Test
    @DisplayName("decrementarCupo() rechaza si no quedan cupos")
    void decrementarCupoRechazaSinCupos() {
        Clase c = claseGuardada(1L, 0, 20);
        when(repo.findById(1L)).thenReturn(Optional.of(c));

        assertThatThrownBy(() -> service.decrementarCupo(1L))
                .isInstanceOf(ReglaNegocioException.class);

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("incrementarCupo() no supera el cupo maximo")
    void incrementarCupoNoSuperaMaximo() {
        Clase c = claseGuardada(1L, 20, 20); // ya en el tope
        when(repo.findById(1L)).thenReturn(Optional.of(c));
        when(repo.save(any(Clase.class))).thenAnswer(inv -> inv.getArgument(0));

        Clase resultado = service.incrementarCupo(1L);

        assertThat(resultado.getCuposDisponibles()).isEqualTo(20);
    }

    @Test
    @DisplayName("listar() retorna todas las clases")
    void listarRetornaTodas() {
        when(repo.findAll()).thenReturn(java.util.List.of(claseGuardada(1L, 5, 20), claseGuardada(2L, 10, 20)));

        assertThat(service.listar()).hasSize(2);
    }

    @Test
    @DisplayName("listarFuturas() delega en el repositorio")
    void listarFuturasDelegaEnRepo() {
        when(repo.findByFechaHoraAfter(any(LocalDateTime.class)))
                .thenReturn(java.util.List.of(claseGuardada(1L, 5, 20)));

        assertThat(service.listarFuturas()).hasSize(1);
        verify(repo).findByFechaHoraAfter(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("buscarPorId() retorna la clase cuando existe")
    void buscarPorIdExistente() {
        when(repo.findById(1L)).thenReturn(Optional.of(claseGuardada(1L, 5, 20)));

        assertThat(service.buscarPorId(1L).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("buscarPorId() lanza excepcion si la clase no existe")
    void buscarPorIdInexistente() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(com.gimnasio.clases.exception.RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("actualizar() aumenta cuposDisponibles cuando sube el cupoMaximo")
    void actualizarAumentaCupoDisponible() {
        Clase existente = claseGuardada(1L, 5, 20);
        when(repo.findById(1L)).thenReturn(Optional.of(existente));
        when(repo.save(any(Clase.class))).thenAnswer(inv -> inv.getArgument(0));

        dto.setCupoMaximo(25); // +5 respecto al actual

        Clase resultado = service.actualizar(1L, dto);

        assertThat(resultado.getCupoMaximo()).isEqualTo(25);
        assertThat(resultado.getCuposDisponibles()).isEqualTo(10);
    }

    @Test
    @DisplayName("actualizar() no deja cuposDisponibles negativos al bajar mucho el cupoMaximo")
    void actualizarNoPermiteCuposNegativos() {
        Clase existente = claseGuardada(1L, 2, 20);
        when(repo.findById(1L)).thenReturn(Optional.of(existente));
        when(repo.save(any(Clase.class))).thenAnswer(inv -> inv.getArgument(0));

        dto.setCupoMaximo(5); // delta = 5 - 20 = -15, 2 - 15 < 0

        Clase resultado = service.actualizar(1L, dto);

        assertThat(resultado.getCuposDisponibles()).isEqualTo(0);
    }

    @Test
    @DisplayName("eliminar() borra la clase cuando existe")
    void eliminarExistente() {
        Clase existente = claseGuardada(1L, 5, 20);
        when(repo.findById(1L)).thenReturn(Optional.of(existente));

        service.eliminar(1L);

        verify(repo).delete(existente);
    }

    @Test
    @DisplayName("eliminar() lanza excepcion si la clase no existe")
    void eliminarInexistente() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminar(99L))
                .isInstanceOf(com.gimnasio.clases.exception.RecursoNoEncontradoException.class);

        verify(repo, never()).delete(any());
    }

    @Test
    @DisplayName("incrementarCupo() lanza excepcion si la clase no existe")
    void incrementarCupoClaseInexistente() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.incrementarCupo(99L))
                .isInstanceOf(com.gimnasio.clases.exception.RecursoNoEncontradoException.class);
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
    @DisplayName("traduce un 404 de ms-instructores en RecursoNoEncontradoException")
    void instructorNoEncontradoViaFeign() {
        feign.FeignException notFound = feign.FeignException.errorStatus(
                "InstructorClient#obtenerInstructor(Long)", respuestaFeign(404));
        when(instructorClient.obtenerInstructor(1L)).thenThrow(notFound);

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(com.gimnasio.clases.exception.RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("traduce un 404 de ms-sucursales en RecursoNoEncontradoException")
    void sucursalNoEncontradaViaFeign() {
        when(instructorClient.obtenerInstructor(1L)).thenReturn(instructorActivo);
        feign.FeignException notFound = feign.FeignException.errorStatus(
                "SucursalClient#obtenerSucursal(Long)", respuestaFeign(404));
        when(sucursalClient.obtenerSucursal(1L)).thenThrow(notFound);

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(com.gimnasio.clases.exception.RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("traduce un error generico de ms-sucursales en ReglaNegocioException")
    void errorGenericoConsultandoSucursal() {
        when(instructorClient.obtenerInstructor(1L)).thenReturn(instructorActivo);
        feign.FeignException error = feign.FeignException.errorStatus(
                "SucursalClient#obtenerSucursal(Long)", respuestaFeign(500));
        when(sucursalClient.obtenerSucursal(1L)).thenThrow(error);

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(ReglaNegocioException.class);
    }

    private Clase claseGuardada(Long id, int disponibles, int maximo) {
        Clase c = new Clase();
        c.setId(id);
        c.setNombre("Yoga");
        c.setInstructorId(1L);
        c.setSucursalId(1L);
        c.setFechaHora(LocalDateTime.now().plusDays(2));
        c.setDuracionMinutos(60);
        c.setCupoMaximo(maximo);
        c.setCuposDisponibles(disponibles);
        return c;
    }
}

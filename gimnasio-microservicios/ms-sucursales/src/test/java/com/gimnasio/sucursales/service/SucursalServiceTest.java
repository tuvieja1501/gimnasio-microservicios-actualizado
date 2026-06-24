package com.gimnasio.sucursales.service;

import com.gimnasio.sucursales.dto.SucursalDTO;
import com.gimnasio.sucursales.exception.RecursoNoEncontradoException;
import com.gimnasio.sucursales.exception.ReglaNegocioException;
import com.gimnasio.sucursales.model.Sucursal;
import com.gimnasio.sucursales.repository.SucursalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SucursalService - pruebas unitarias")
class SucursalServiceTest {

    @Mock
    private SucursalRepository repo;

    @InjectMocks
    private SucursalService service;

    private SucursalDTO dto;

    @BeforeEach
    void setUp() {
        dto = new SucursalDTO();
        dto.setNombre("Sucursal Centro");
        dto.setDireccion("Av. Siempre Viva 123");
        dto.setComuna("Santiago");
        dto.setTelefono("+56912345678");
        dto.setCapacidad(50);
        dto.setHoraApertura(LocalTime.of(7, 0));
        dto.setHoraCierre(LocalTime.of(22, 0));
        dto.setActiva(true);
    }

    @Nested
    @DisplayName("crear()")
    class Crear {

        @Test
        @DisplayName("crea sucursal con datos validos")
        void creaConDatosValidos() {
            when(repo.existsByNombre(dto.getNombre())).thenReturn(false);
            when(repo.save(any(Sucursal.class))).thenAnswer(inv -> {
                Sucursal s = inv.getArgument(0);
                s.setId(1L);
                return s;
            });

            Sucursal resultado = service.crear(dto);

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getNombre()).isEqualTo("Sucursal Centro");
        }

        @Test
        @DisplayName("rechaza nombre duplicado")
        void rechazaNombreDuplicado() {
            when(repo.existsByNombre(dto.getNombre())).thenReturn(true);

            assertThatThrownBy(() -> service.crear(dto))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining(dto.getNombre());

            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("rechaza horario de apertura posterior o igual al de cierre")
        void rechazaHorarioInvalido() {
            dto.setHoraApertura(LocalTime.of(23, 0));
            dto.setHoraCierre(LocalTime.of(8, 0));
            when(repo.existsByNombre(dto.getNombre())).thenReturn(false);

            assertThatThrownBy(() -> service.crear(dto))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("apertura");

            verify(repo, never()).save(any());
        }
    }

    @Test
    @DisplayName("actualizar() permite mismo nombre sin chequear unicidad")
    void actualizarSinCambiarNombre() {
        Sucursal existente = sucursalGuardada(1L);
        existente.setNombre(dto.getNombre());
        when(repo.findById(1L)).thenReturn(Optional.of(existente));
        when(repo.save(any(Sucursal.class))).thenAnswer(inv -> inv.getArgument(0));

        Sucursal resultado = service.actualizar(1L, dto);

        assertThat(resultado.getCapacidad()).isEqualTo(50);
        verify(repo, never()).existsByNombre(any());
    }

    @Test
    @DisplayName("cambiarEstado() actualiza el flag activa")
    void cambiarEstadoActualizaActiva() {
        Sucursal existente = sucursalGuardada(1L);
        when(repo.findById(1L)).thenReturn(Optional.of(existente));
        when(repo.save(any(Sucursal.class))).thenAnswer(inv -> inv.getArgument(0));

        Sucursal resultado = service.cambiarEstado(1L, false);

        assertThat(resultado.getActiva()).isFalse();
    }

    @Test
    @DisplayName("buscarPorId() lanza excepcion si no existe")
    void buscarPorIdNoExistente() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("eliminar() borra la sucursal existente")
    void eliminarSucursalExistente() {
        Sucursal existente = sucursalGuardada(1L);
        when(repo.findById(1L)).thenReturn(Optional.of(existente));

        service.eliminar(1L);

        verify(repo).delete(existente);
    }

    @Test
    @DisplayName("listar() retorna todas las sucursales")
    void listarRetornaTodas() {
        when(repo.findAll()).thenReturn(java.util.List.of(sucursalGuardada(1L), sucursalGuardada(2L)));

        assertThat(service.listar()).hasSize(2);
    }

    @Test
    @DisplayName("listarActivas() delega en el repositorio")
    void listarActivasDelegaEnRepo() {
        when(repo.findByActivaTrue()).thenReturn(java.util.List.of(sucursalGuardada(1L)));

        assertThat(service.listarActivas()).hasSize(1);
        verify(repo).findByActivaTrue();
    }

    @Test
    @DisplayName("listarPorComuna() delega en el repositorio")
    void listarPorComunaDelegaEnRepo() {
        when(repo.findByComunaIgnoreCase("Providencia")).thenReturn(java.util.List.of(sucursalGuardada(1L)));

        assertThat(service.listarPorComuna("Providencia")).hasSize(1);
        verify(repo).findByComunaIgnoreCase("Providencia");
    }

    @Test
    @DisplayName("actualizar() permite cambiar a un nombre disponible")
    void actualizarConNombreNuevoDisponible() {
        Sucursal existente = sucursalGuardada(1L); // nombre "Sucursal Norte"
        when(repo.findById(1L)).thenReturn(Optional.of(existente));
        when(repo.existsByNombre(dto.getNombre())).thenReturn(false); // dto = "Sucursal Centro"
        when(repo.save(any(Sucursal.class))).thenAnswer(inv -> inv.getArgument(0));

        Sucursal resultado = service.actualizar(1L, dto);

        assertThat(resultado.getNombre()).isEqualTo("Sucursal Centro");
    }

    @Test
    @DisplayName("actualizar() rechaza si el nuevo nombre ya esta en uso")
    void actualizarConNombreEnConflicto() {
        Sucursal existente = sucursalGuardada(1L);
        when(repo.findById(1L)).thenReturn(Optional.of(existente));
        when(repo.existsByNombre(dto.getNombre())).thenReturn(true);

        assertThatThrownBy(() -> service.actualizar(1L, dto))
                .isInstanceOf(ReglaNegocioException.class);

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("actualizar() rechaza horario invalido")
    void actualizarConHorarioInvalido() {
        Sucursal existente = sucursalGuardada(1L);
        existente.setNombre(dto.getNombre());
        dto.setHoraApertura(LocalTime.of(20, 0));
        dto.setHoraCierre(LocalTime.of(10, 0));
        when(repo.findById(1L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.actualizar(1L, dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("apertura");

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("actualizar() lanza excepcion si la sucursal no existe")
    void actualizarInexistente() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizar(99L, dto))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("cambiarEstado() lanza excepcion si la sucursal no existe")
    void cambiarEstadoInexistente() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cambiarEstado(99L, false))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("eliminar() lanza excepcion si la sucursal no existe")
    void eliminarInexistente() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminar(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(repo, never()).delete(any());
    }

    private Sucursal sucursalGuardada(Long id) {
        Sucursal s = new Sucursal();
        s.setId(id);
        s.setNombre("Sucursal Norte");
        s.setDireccion("Calle Falsa 456");
        s.setComuna("Providencia");
        s.setTelefono("+56987654321");
        s.setCapacidad(80);
        s.setHoraApertura(LocalTime.of(6, 0));
        s.setHoraCierre(LocalTime.of(23, 0));
        s.setActiva(true);
        return s;
    }
}

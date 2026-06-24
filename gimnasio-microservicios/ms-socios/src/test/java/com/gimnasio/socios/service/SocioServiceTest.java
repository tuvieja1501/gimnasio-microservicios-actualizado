package com.gimnasio.socios.service;

import com.gimnasio.socios.dto.SocioDTO;
import com.gimnasio.socios.exception.RecursoNoEncontradoException;
import com.gimnasio.socios.exception.ReglaNegocioException;
import com.gimnasio.socios.model.EstadoSocio;
import com.gimnasio.socios.model.Socio;
import com.gimnasio.socios.repository.SocioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de la capa Service (reglas de negocio) usando Mockito.
 * El repositorio se mockea por completo: aqui se valida unicamente la
 * logica de SocioService, sin tocar la base de datos real.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SocioService - pruebas unitarias")
class SocioServiceTest {

    @Mock
    private SocioRepository repo;

    private SocioService service;

    @BeforeEach
    void setUp() {
        service = new SocioService(repo);
    }

    private SocioDTO dtoValido() {
        SocioDTO dto = new SocioDTO();
        dto.setNombre(" Ana ");
        dto.setApellido(" Soto ");
        dto.setRut("11111111-1");
        dto.setEmail("Ana.Soto@MAIL.com");
        dto.setTelefono("+56911112222");
        dto.setFechaNacimiento(LocalDate.now().minusYears(25));
        dto.setEstado(EstadoSocio.ACTIVO);
        return dto;
    }

    private Socio socioExistente() {
        Socio s = new Socio();
        s.setId(1L);
        s.setNombre("Ana");
        s.setApellido("Soto");
        s.setRut("11111111-1");
        s.setEmail("ana.soto@mail.com");
        s.setTelefono("+56911112222");
        s.setFechaNacimiento(LocalDate.now().minusYears(25));
        s.setFechaRegistro(LocalDate.now());
        s.setEstado(EstadoSocio.ACTIVO);
        return s;
    }

    // ---------- listar ----------

    @Test
    @DisplayName("listar() retorna todos los socios del repositorio")
    void listarRetornaTodos() {
        when(repo.findAll()).thenReturn(Arrays.asList(socioExistente(), socioExistente()));

        List<Socio> resultado = service.listar();

        assertThat(resultado).hasSize(2);
        verify(repo, times(1)).findAll();
    }

    // ---------- buscarPorId ----------

    @Test
    @DisplayName("buscarPorId() retorna el socio cuando existe")
    void buscarPorIdExistente() {
        when(repo.findById(1L)).thenReturn(Optional.of(socioExistente()));

        Socio resultado = service.buscarPorId(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getRut()).isEqualTo("11111111-1");
    }

    @Test
    @DisplayName("buscarPorId() lanza RecursoNoEncontradoException si no existe")
    void buscarPorIdInexistente() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
    }

    // ---------- buscarPorRut ----------

    @Test
    @DisplayName("buscarPorRut() retorna el socio cuando existe")
    void buscarPorRutExistente() {
        when(repo.findByRut("11111111-1")).thenReturn(Optional.of(socioExistente()));

        Socio resultado = service.buscarPorRut("11111111-1");

        assertThat(resultado.getRut()).isEqualTo("11111111-1");
    }

    @Test
    @DisplayName("buscarPorRut() lanza RecursoNoEncontradoException si no existe")
    void buscarPorRutInexistente() {
        when(repo.findByRut("00000000-0")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorRut("00000000-0"))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("00000000-0");
    }

    // ---------- crear ----------

    @Test
    @DisplayName("crear() guarda el socio normalizando nombre/apellido/email")
    void crearSocioValido() {
        SocioDTO dto = dtoValido();
        when(repo.existsByRut(dto.getRut())).thenReturn(false);
        when(repo.existsByEmail(anyString())).thenReturn(false);
        when(repo.save(any(Socio.class))).thenAnswer(invocation -> {
            Socio arg = invocation.getArgument(0);
            arg.setId(10L);
            return arg;
        });

        Socio creado = service.crear(dto);

        ArgumentCaptor<Socio> captor = ArgumentCaptor.forClass(Socio.class);
        verify(repo).save(captor.capture());
        Socio guardado = captor.getValue();

        assertThat(creado.getId()).isEqualTo(10L);
        assertThat(guardado.getNombre()).isEqualTo("Ana");
        assertThat(guardado.getApellido()).isEqualTo("Soto");
        assertThat(guardado.getEmail()).isEqualTo("ana.soto@mail.com");
        assertThat(guardado.getFechaRegistro()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("crear() lanza ReglaNegocioException si el RUT ya existe")
    void crearConRutDuplicado() {
        SocioDTO dto = dtoValido();
        when(repo.existsByRut(dto.getRut())).thenReturn(true);

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("RUT");

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("crear() lanza ReglaNegocioException si el email ya existe")
    void crearConEmailDuplicado() {
        SocioDTO dto = dtoValido();
        when(repo.existsByRut(dto.getRut())).thenReturn(false);
        when(repo.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("email");

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("crear() lanza ReglaNegocioException si el socio es menor a la edad minima")
    void crearMenorDeEdad() {
        SocioDTO dto = dtoValido();
        dto.setFechaNacimiento(LocalDate.now().minusYears(10));
        when(repo.existsByRut(dto.getRut())).thenReturn(false);
        when(repo.existsByEmail(anyString())).thenReturn(false);

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("14 anios");

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("crear() no valida edad si la fecha de nacimiento es nula")
    void crearSinFechaNacimiento() {
        SocioDTO dto = dtoValido();
        dto.setFechaNacimiento(null);
        when(repo.existsByRut(dto.getRut())).thenReturn(false);
        when(repo.existsByEmail(anyString())).thenReturn(false);
        when(repo.save(any(Socio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Socio creado = service.crear(dto);

        assertThat(creado.getFechaNacimiento()).isNull();
        verify(repo).save(any(Socio.class));
    }

    // ---------- actualizar ----------

    @Test
    @DisplayName("actualizar() modifica los datos cuando RUT y email no cambian")
    void actualizarSinCambiarRutNiEmail() {
        Socio existente = socioExistente();
        SocioDTO dto = dtoValido();
        dto.setRut(existente.getRut());
        dto.setEmail(existente.getEmail());
        dto.setNombre("Ana Maria");

        when(repo.findById(1L)).thenReturn(Optional.of(existente));
        when(repo.save(any(Socio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Socio actualizado = service.actualizar(1L, dto);

        assertThat(actualizado.getNombre()).isEqualTo("Ana Maria");
        verify(repo, never()).existsByRut(anyString());
        verify(repo, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("actualizar() valida unicidad de RUT cuando cambia")
    void actualizarConRutNuevoDisponible() {
        Socio existente = socioExistente();
        SocioDTO dto = dtoValido();
        dto.setRut("22222222-2");
        dto.setEmail(existente.getEmail());

        when(repo.findById(1L)).thenReturn(Optional.of(existente));
        when(repo.existsByRut("22222222-2")).thenReturn(false);
        when(repo.save(any(Socio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Socio actualizado = service.actualizar(1L, dto);

        assertThat(actualizado.getRut()).isEqualTo("22222222-2");
    }

    @Test
    @DisplayName("actualizar() lanza ReglaNegocioException si el nuevo RUT ya esta en uso")
    void actualizarConRutEnConflicto() {
        Socio existente = socioExistente();
        SocioDTO dto = dtoValido();
        dto.setRut("22222222-2");
        dto.setEmail(existente.getEmail());

        when(repo.findById(1L)).thenReturn(Optional.of(existente));
        when(repo.existsByRut("22222222-2")).thenReturn(true);

        assertThatThrownBy(() -> service.actualizar(1L, dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("RUT");

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("actualizar() lanza ReglaNegocioException si el nuevo email ya esta en uso")
    void actualizarConEmailEnConflicto() {
        Socio existente = socioExistente();
        SocioDTO dto = dtoValido();
        dto.setRut(existente.getRut());
        dto.setEmail("nuevo@mail.com");

        when(repo.findById(1L)).thenReturn(Optional.of(existente));
        when(repo.existsByEmail("nuevo@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> service.actualizar(1L, dto))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("email");

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("actualizar() lanza RecursoNoEncontradoException si el socio no existe")
    void actualizarSocioInexistente() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizar(99L, dtoValido()))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(repo, never()).save(any());
    }

    // ---------- eliminar ----------

    @Test
    @DisplayName("eliminar() borra el socio cuando existe")
    void eliminarSocioExistente() {
        Socio existente = socioExistente();
        when(repo.findById(1L)).thenReturn(Optional.of(existente));

        service.eliminar(1L);

        verify(repo).delete(existente);
    }

    @Test
    @DisplayName("eliminar() lanza RecursoNoEncontradoException si el socio no existe")
    void eliminarSocioInexistente() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminar(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(repo, never()).delete(any());
    }

    // ---------- cambiarEstado ----------

    @Test
    @DisplayName("cambiarEstado() actualiza el estado y persiste el cambio")
    void cambiarEstadoCorrectamente() {
        Socio existente = socioExistente();
        when(repo.findById(1L)).thenReturn(Optional.of(existente));
        when(repo.save(any(Socio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Socio resultado = service.cambiarEstado(1L, EstadoSocio.MOROSO);

        assertThat(resultado.getEstado()).isEqualTo(EstadoSocio.MOROSO);
        verify(repo).save(existente);
    }

    @Test
    @DisplayName("cambiarEstado() lanza RecursoNoEncontradoException si el socio no existe")
    void cambiarEstadoSocioInexistente() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cambiarEstado(99L, EstadoSocio.INACTIVO))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(repo, never()).save(any());
    }
}

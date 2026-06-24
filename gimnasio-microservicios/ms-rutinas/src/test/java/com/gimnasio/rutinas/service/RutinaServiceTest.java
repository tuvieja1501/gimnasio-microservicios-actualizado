package com.gimnasio.rutinas.service;

import com.gimnasio.rutinas.client.InstructorClient;
import com.gimnasio.rutinas.client.SocioClient;
import com.gimnasio.rutinas.dto.EjercicioDTO;
import com.gimnasio.rutinas.dto.InstructorRespuesta;
import com.gimnasio.rutinas.dto.RutinaDTO;
import com.gimnasio.rutinas.dto.SocioRespuesta;
import com.gimnasio.rutinas.exception.RecursoNoEncontradoException;
import com.gimnasio.rutinas.exception.ReglaNegocioException;
import com.gimnasio.rutinas.model.Rutina;
import com.gimnasio.rutinas.repository.RutinaRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RutinaService - pruebas unitarias")
class RutinaServiceTest {

    @Mock
    private RutinaRepository repo;
    @Mock
    private SocioClient socioClient;
    @Mock
    private InstructorClient instructorClient;

    @InjectMocks
    private RutinaService service;

    private RutinaDTO dto;
    private SocioRespuesta socioActivo;
    private InstructorRespuesta instructorActivo;

    @BeforeEach
    void setUp() {
        EjercicioDTO ejercicio = new EjercicioDTO();
        ejercicio.setNombre("Sentadillas");
        ejercicio.setSeries(4);
        ejercicio.setRepeticiones(12);
        ejercicio.setDescansoSegundos(60);

        dto = new RutinaDTO();
        dto.setNombre("Fuerza nivel 1");
        dto.setObjetivo("Ganar masa muscular");
        dto.setSocioId(10L);
        dto.setInstructorId(1L);
        dto.setDuracionSemanas(8);
        dto.setEjercicios(List.of(ejercicio));

        socioActivo = new SocioRespuesta();
        socioActivo.setId(10L);
        socioActivo.setEstado("ACTIVO");

        instructorActivo = new InstructorRespuesta();
        instructorActivo.setId(1L);
        instructorActivo.setActivo(true);
    }

    @Test
    @DisplayName("crea rutina con sus ejercicios cuando socio e instructor son validos")
    void creaRutinaConDatosValidos() {
        when(socioClient.obtenerSocio(10L)).thenReturn(socioActivo);
        when(instructorClient.obtenerInstructor(1L)).thenReturn(instructorActivo);
        when(repo.save(any(Rutina.class))).thenAnswer(inv -> {
            Rutina r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        Rutina resultado = service.crear(dto);

        assertThat(resultado.getEjercicios()).hasSize(1);
        assertThat(resultado.getEjercicios().get(0).getNombre()).isEqualTo("Sentadillas");
        assertThat(resultado.getEjercicios().get(0).getRutina()).isEqualTo(resultado);
    }

    @Test
    @DisplayName("rechaza si el socio no esta ACTIVO")
    void rechazaSocioInactivo() {
        socioActivo.setEstado("MOROSO");
        when(socioClient.obtenerSocio(10L)).thenReturn(socioActivo);

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(ReglaNegocioException.class);

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("rechaza si el instructor no esta activo")
    void rechazaInstructorInactivo() {
        instructorActivo.setActivo(false);
        when(socioClient.obtenerSocio(10L)).thenReturn(socioActivo);
        when(instructorClient.obtenerInstructor(1L)).thenReturn(instructorActivo);

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(ReglaNegocioException.class);

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("traduce 404 de ms-socios a RecursoNoEncontradoException")
    void traduce404DeSocio() {
        when(socioClient.obtenerSocio(10L)).thenThrow(mock(FeignException.NotFound.class));

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("actualizar() reemplaza completamente la lista de ejercicios")
    void actualizarReemplazaEjercicios() {
        Rutina existente = rutinaGuardada(1L);
        when(repo.findById(1L)).thenReturn(java.util.Optional.of(existente));
        when(repo.save(any(Rutina.class))).thenAnswer(inv -> inv.getArgument(0));

        EjercicioDTO nuevo = new EjercicioDTO();
        nuevo.setNombre("Press banca");
        nuevo.setSeries(3);
        nuevo.setRepeticiones(10);
        dto.setEjercicios(List.of(nuevo));

        Rutina resultado = service.actualizar(1L, dto);

        assertThat(resultado.getEjercicios()).hasSize(1);
        assertThat(resultado.getEjercicios().get(0).getNombre()).isEqualTo("Press banca");
    }

    @Test
    @DisplayName("traduce error generico de ms-socios a ReglaNegocioException")
    void errorGenericoDeSocio() {
        when(socioClient.obtenerSocio(10L)).thenThrow(mock(FeignException.class));

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("traduce 404 de ms-instructores a RecursoNoEncontradoException")
    void traduce404DeInstructor() {
        when(socioClient.obtenerSocio(10L)).thenReturn(socioActivo);
        when(instructorClient.obtenerInstructor(1L)).thenThrow(mock(FeignException.NotFound.class));

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("traduce error generico de ms-instructores a ReglaNegocioException")
    void errorGenericoDeInstructor() {
        when(socioClient.obtenerSocio(10L)).thenReturn(socioActivo);
        when(instructorClient.obtenerInstructor(1L)).thenThrow(mock(FeignException.class));

        assertThatThrownBy(() -> service.crear(dto))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("listar() retorna todas las rutinas")
    void listarRetornaTodas() {
        when(repo.findAll()).thenReturn(List.of(rutinaGuardada(1L), rutinaGuardada(2L)));

        assertThat(service.listar()).hasSize(2);
    }

    @Test
    @DisplayName("listarPorSocio() delega en el repositorio")
    void listarPorSocioDelegaEnRepo() {
        when(repo.findBySocioId(10L)).thenReturn(List.of(rutinaGuardada(1L)));

        assertThat(service.listarPorSocio(10L)).hasSize(1);
        verify(repo).findBySocioId(10L);
    }

    @Test
    @DisplayName("buscarPorId() retorna la rutina cuando existe")
    void buscarPorIdExistente() {
        when(repo.findById(1L)).thenReturn(java.util.Optional.of(rutinaGuardada(1L)));

        assertThat(service.buscarPorId(1L).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("buscarPorId() lanza excepcion si no existe")
    void buscarPorIdInexistente() {
        when(repo.findById(99L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("actualizar() lanza excepcion si la rutina no existe")
    void actualizarInexistente() {
        when(repo.findById(99L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> service.actualizar(99L, dto))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(repo, never()).save(any());
    }

    @Test
    @DisplayName("eliminar() borra la rutina cuando existe")
    void eliminarExistente() {
        Rutina r = rutinaGuardada(1L);
        when(repo.findById(1L)).thenReturn(java.util.Optional.of(r));

        service.eliminar(1L);

        verify(repo).delete(r);
    }

    @Test
    @DisplayName("eliminar() lanza excepcion si la rutina no existe")
    void eliminarInexistente() {
        when(repo.findById(99L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> service.eliminar(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(repo, never()).delete(any());
    }

    private Rutina rutinaGuardada(Long id) {
        Rutina r = new Rutina();
        r.setId(id);
        r.setNombre("Rutina previa");
        r.setSocioId(10L);
        r.setInstructorId(1L);
        r.setDuracionSemanas(4);
        r.setFechaCreacion(java.time.LocalDate.now().minusWeeks(1));
        return r;
    }
}

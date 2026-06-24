package com.gimnasio.instructores.service;

import com.gimnasio.instructores.dto.InstructorDTO;
import com.gimnasio.instructores.exception.RecursoNoEncontradoException;
import com.gimnasio.instructores.exception.ReglaNegocioException;
import com.gimnasio.instructores.model.Instructor;
import com.gimnasio.instructores.repository.InstructorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InstructorService - pruebas unitarias")
class InstructorServiceTest {

    @Mock
    private InstructorRepository repo;

    @InjectMocks
    private InstructorService service;

    private InstructorDTO dto;

    @BeforeEach
    void setUp() {
        dto = new InstructorDTO();
        dto.setNombre("Pedro");
        dto.setApellido("Diaz");
        dto.setRut("11222333-4");
        dto.setEmail("Pedro.Diaz@mail.com");
        dto.setEspecialidad("Crossfit");
        dto.setAniosExperiencia(5);
        dto.setActivo(true);
    }

    @Nested
    @DisplayName("crear()")
    class Crear {

        @Test
        @DisplayName("crea instructor cuando rut y email son unicos")
        void creaConDatosValidos() {
            when(repo.existsByRut(dto.getRut())).thenReturn(false);
            when(repo.existsByEmail(dto.getEmail())).thenReturn(false);
            when(repo.save(any(Instructor.class))).thenAnswer(inv -> {
                Instructor i = inv.getArgument(0);
                i.setId(1L);
                return i;
            });

            Instructor resultado = service.crear(dto);

            assertThat(resultado.getId()).isEqualTo(1L);
            assertThat(resultado.getEspecialidad()).isEqualTo("Crossfit");
            verify(repo).save(any(Instructor.class));
        }

        @Test
        @DisplayName("rechaza RUT duplicado")
        void rechazaRutDuplicado() {
            when(repo.existsByRut(dto.getRut())).thenReturn(true);

            assertThatThrownBy(() -> service.crear(dto))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining(dto.getRut());

            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("rechaza email duplicado")
        void rechazaEmailDuplicado() {
            when(repo.existsByRut(dto.getRut())).thenReturn(false);
            when(repo.existsByEmail(dto.getEmail())).thenReturn(true);

            assertThatThrownBy(() -> service.crear(dto))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining(dto.getEmail());

            verify(repo, never()).save(any());
        }
    }

    @Nested
    @DisplayName("actualizar()")
    class Actualizar {

        @Test
        @DisplayName("permite actualizar manteniendo el mismo RUT")
        void actualizaSinCambiarRut() {
            Instructor existente = instructorGuardado(1L);
            existente.setRut(dto.getRut());
            when(repo.findById(1L)).thenReturn(Optional.of(existente));
            when(repo.save(any(Instructor.class))).thenAnswer(inv -> inv.getArgument(0));

            Instructor resultado = service.actualizar(1L, dto);

            assertThat(resultado.getNombre()).isEqualTo("Pedro");
            verify(repo, never()).existsByRut(any());
        }

        @Test
        @DisplayName("rechaza si el nuevo RUT pertenece a otro instructor")
        void rechazaRutDeOtroInstructor() {
            Instructor existente = instructorGuardado(1L); // rut distinto al de dto
            when(repo.findById(1L)).thenReturn(Optional.of(existente));
            when(repo.existsByRut(dto.getRut())).thenReturn(true);

            assertThatThrownBy(() -> service.actualizar(1L, dto))
                    .isInstanceOf(ReglaNegocioException.class);

            verify(repo, never()).save(any());
        }
    }

    @Test
    @DisplayName("buscarPorId() lanza excepcion si no existe")
    void buscarPorIdNoExistente() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("eliminar() borra el instructor existente")
    void eliminarInstructorExistente() {
        Instructor existente = instructorGuardado(1L);
        when(repo.findById(1L)).thenReturn(Optional.of(existente));

        service.eliminar(1L);

        verify(repo).delete(existente);
    }

    @Test
    @DisplayName("listarActivos() delega en el repositorio")
    void listarActivosDelegaEnRepo() {
        when(repo.findByActivoTrue()).thenReturn(List.of(instructorGuardado(1L)));

        List<Instructor> resultado = service.listarActivos();

        assertThat(resultado).hasSize(1);
        verify(repo).findByActivoTrue();
    }

    @Test
    @DisplayName("buscarPorEspecialidad() delega en el repositorio")
    void buscarPorEspecialidadDelegaEnRepo() {
        when(repo.findByEspecialidadIgnoreCase("crossfit")).thenReturn(List.of(instructorGuardado(1L)));

        List<Instructor> resultado = service.buscarPorEspecialidad("crossfit");

        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("listar() retorna todos los instructores")
    void listarRetornaTodos() {
        when(repo.findAll()).thenReturn(List.of(instructorGuardado(1L), instructorGuardado(2L)));

        assertThat(service.listar()).hasSize(2);
    }

    @Test
    @DisplayName("eliminar() lanza excepcion si el instructor no existe")
    void eliminarInstructorInexistente() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminar(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(repo, never()).delete(any());
    }

    @Test
    @DisplayName("actualizar() lanza excepcion si el instructor no existe")
    void actualizarInstructorInexistente() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizar(99L, dto))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(repo, never()).save(any());
    }

    private Instructor instructorGuardado(Long id) {
        Instructor i = new Instructor();
        i.setId(id);
        i.setNombre("Carlos");
        i.setApellido("Gomez");
        i.setRut("99888777-6");
        i.setEmail("carlos.gomez@mail.com");
        i.setEspecialidad("Yoga");
        i.setAniosExperiencia(3);
        i.setActivo(true);
        return i;
    }
}

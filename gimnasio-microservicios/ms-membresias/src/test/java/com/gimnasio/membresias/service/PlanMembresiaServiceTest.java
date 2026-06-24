package com.gimnasio.membresias.service;

import com.gimnasio.membresias.dto.PlanMembresiaDTO;
import com.gimnasio.membresias.exception.RecursoNoEncontradoException;
import com.gimnasio.membresias.model.PlanMembresia;
import com.gimnasio.membresias.repository.PlanMembresiaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlanMembresiaService - pruebas unitarias")
class PlanMembresiaServiceTest {

    @Mock
    private PlanMembresiaRepository repo;

    @InjectMocks
    private PlanMembresiaService service;

    private PlanMembresiaDTO dto;

    @BeforeEach
    void setUp() {
        dto = new PlanMembresiaDTO();
        dto.setNombre("Mensual Basico");
        dto.setDescripcion("Acceso a sala de musculacion");
        dto.setDuracionMeses(1);
        dto.setPrecio(new BigDecimal("19990"));
        dto.setActivo(true);
    }

    @Test
    @DisplayName("crear() guarda el plan correctamente")
    void creaPlan() {
        when(repo.save(any(PlanMembresia.class))).thenAnswer(inv -> {
            PlanMembresia p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        PlanMembresia resultado = service.crear(dto);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("Mensual Basico");
        assertThat(resultado.getDuracionMeses()).isEqualTo(1);
    }

    @Test
    @DisplayName("actualizar() modifica un plan existente")
    void actualizaPlanExistente() {
        PlanMembresia existente = planGuardado(1L);
        when(repo.findById(1L)).thenReturn(Optional.of(existente));
        when(repo.save(any(PlanMembresia.class))).thenAnswer(inv -> inv.getArgument(0));

        dto.setNombre("Mensual Premium");
        PlanMembresia resultado = service.actualizar(1L, dto);

        assertThat(resultado.getNombre()).isEqualTo("Mensual Premium");
    }

    @Test
    @DisplayName("buscarPorId() lanza excepcion si no existe")
    void buscarPorIdNoExistente() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("listarActivos() delega en el repositorio")
    void listarActivosDelegaEnRepo() {
        when(repo.findByActivoTrue()).thenReturn(List.of(planGuardado(1L)));

        List<PlanMembresia> resultado = service.listarActivos();

        assertThat(resultado).hasSize(1);
        verify(repo).findByActivoTrue();
    }

    @Test
    @DisplayName("eliminar() borra el plan existente")
    void eliminarPlanExistente() {
        PlanMembresia existente = planGuardado(1L);
        when(repo.findById(1L)).thenReturn(Optional.of(existente));

        service.eliminar(1L);

        verify(repo).delete(existente);
    }

    @Test
    @DisplayName("listar() retorna todos los planes")
    void listarRetornaTodos() {
        when(repo.findAll()).thenReturn(List.of(planGuardado(1L), planGuardado(2L)));

        assertThat(service.listar()).hasSize(2);
    }

    private PlanMembresia planGuardado(Long id) {
        PlanMembresia p = new PlanMembresia();
        p.setId(id);
        p.setNombre("Anual VIP");
        p.setDescripcion("Acceso total");
        p.setDuracionMeses(12);
        p.setPrecio(new BigDecimal("199990"));
        p.setActivo(true);
        return p;
    }
}

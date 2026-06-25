package com.gimnasio.membresias.model;

import org.junit.jupiter.api.Test;

import com.gimnasio.membresias.model.EstadoMembresia;
import com.gimnasio.membresias.model.Membresia;
import com.gimnasio.membresias.model.PlanMembresia;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import static org.assertj.core.api.Assertions.assertThat;

class MembresiaModelTest {
    @Test void membresiaGettersSetters() {
        PlanMembresia plan = new PlanMembresia(); plan.setId(1L); plan.setNombre("Mensual");
        plan.setDuracionMeses(1); plan.setPrecio(new BigDecimal("30000")); plan.setActivo(true);

        Membresia m = new Membresia(); LocalDate inicio = LocalDate.now();
        m.setId(1L); m.setSocioId(10L); m.setPlan(plan);
        m.setFechaInicio(inicio); m.setFechaFin(inicio.plusMonths(1)); m.setEstado(EstadoMembresia.VIGENTE);

        assertThat(m.getId()).isEqualTo(1L); assertThat(m.getSocioId()).isEqualTo(10L);
        assertThat(m.getPlan().getNombre()).isEqualTo("Mensual");
        assertThat(m.getFechaInicio()).isEqualTo(inicio); assertThat(m.getEstado()).isEqualTo(EstadoMembresia.VIGENTE);
    }

    @Test void planMembresiaGettersSetters() {
        PlanMembresia p = new PlanMembresia(); p.setId(2L); p.setNombre("Anual");
        p.setDescripcion("Desc"); p.setDuracionMeses(12); p.setPrecio(new BigDecimal("300000")); p.setActivo(true);
        p.setMembresias(new ArrayList<>());
        assertThat(p.getId()).isEqualTo(2L); assertThat(p.getDescripcion()).isEqualTo("Desc");
        assertThat(p.getDuracionMeses()).isEqualTo(12); assertThat(p.getMembresias()).isEmpty();
    }

    @Test void estadoMembresiaEnum() {
        assertThat(EstadoMembresia.values()).containsExactlyInAnyOrder(
                EstadoMembresia.VIGENTE, EstadoMembresia.VENCIDA, EstadoMembresia.CANCELADA);
    }
}

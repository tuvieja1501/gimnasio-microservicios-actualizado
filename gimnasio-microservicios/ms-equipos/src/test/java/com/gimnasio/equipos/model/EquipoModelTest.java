package com.gimnasio.equipos.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

class EquipoModelTest {
    @Test void gettersSetters() {
        Equipo e = new Equipo(); LocalDate hoy = LocalDate.now();
        e.setId(1L); e.setNombre("Cinta"); e.setTipo("Cardio"); e.setCodigoInterno("CINTA-001");
        e.setSucursalId(2L); e.setFechaAdquisicion(hoy); e.setEstado(EstadoEquipo.OPERATIVO);
        assertThat(e.getId()).isEqualTo(1L); assertThat(e.getNombre()).isEqualTo("Cinta");
        assertThat(e.getTipo()).isEqualTo("Cardio"); assertThat(e.getCodigoInterno()).isEqualTo("CINTA-001");
        assertThat(e.getSucursalId()).isEqualTo(2L); assertThat(e.getFechaAdquisicion()).isEqualTo(hoy);
        assertThat(e.getEstado()).isEqualTo(EstadoEquipo.OPERATIVO);
    }

    @Test void estadoEquipoEnum() {
        assertThat(EstadoEquipo.values()).containsExactlyInAnyOrder(
                EstadoEquipo.OPERATIVO, EstadoEquipo.EN_MANTENIMIENTO, EstadoEquipo.DADO_DE_BAJA);
    }
}

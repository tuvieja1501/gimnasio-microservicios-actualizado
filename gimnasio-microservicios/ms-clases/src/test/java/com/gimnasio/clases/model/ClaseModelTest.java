package com.gimnasio.clases.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class ClaseModelTest {
    @Test void gettersSetters() {
        Clase c = new Clase(); LocalDateTime fh = LocalDateTime.now().plusDays(1);
        c.setId(1L); c.setNombre("Pilates"); c.setDescripcion("Desc"); c.setInstructorId(2L);
        c.setSucursalId(3L); c.setFechaHora(fh); c.setDuracionMinutos(45);
        c.setCupoMaximo(15); c.setCuposDisponibles(10);
        assertThat(c.getId()).isEqualTo(1L); assertThat(c.getNombre()).isEqualTo("Pilates");
        assertThat(c.getDescripcion()).isEqualTo("Desc"); assertThat(c.getInstructorId()).isEqualTo(2L);
        assertThat(c.getSucursalId()).isEqualTo(3L); assertThat(c.getFechaHora()).isEqualTo(fh);
        assertThat(c.getDuracionMinutos()).isEqualTo(45); assertThat(c.getCupoMaximo()).isEqualTo(15);
        assertThat(c.getCuposDisponibles()).isEqualTo(10);
    }
}

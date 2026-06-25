package com.gimnasio.rutinas.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class RutinaModelTest {
    @Test void rutinaGettersSetters() {
        Rutina r = new Rutina(); LocalDate hoy = LocalDate.now();
        r.setId(1L); r.setNombre("Rutina Test"); r.setObjetivo("Ganar masa");
        r.setSocioId(10L); r.setInstructorId(2L); r.setFechaCreacion(hoy); r.setDuracionSemanas(12);
        assertThat(r.getId()).isEqualTo(1L); assertThat(r.getNombre()).isEqualTo("Rutina Test");
        assertThat(r.getObjetivo()).isEqualTo("Ganar masa"); assertThat(r.getSocioId()).isEqualTo(10L);
        assertThat(r.getInstructorId()).isEqualTo(2L); assertThat(r.getFechaCreacion()).isEqualTo(hoy);
        assertThat(r.getDuracionSemanas()).isEqualTo(12); assertThat(r.getEjercicios()).isEmpty();
    }

    @Test void ejercicioGettersSetters() {
        Rutina r = new Rutina(); r.setId(1L); r.setNombre("R");
        Ejercicio e = new Ejercicio();
        e.setId(1L); e.setNombre("Sentadilla"); e.setSeries(4); e.setRepeticiones(12);
        e.setDescansoSegundos(90); e.setObservaciones("Con barra"); e.setRutina(r);
        assertThat(e.getId()).isEqualTo(1L); assertThat(e.getNombre()).isEqualTo("Sentadilla");
        assertThat(e.getSeries()).isEqualTo(4); assertThat(e.getRepeticiones()).isEqualTo(12);
        assertThat(e.getDescansoSegundos()).isEqualTo(90); assertThat(e.getObservaciones()).isEqualTo("Con barra");
        assertThat(e.getRutina()).isNotNull();
    }
}

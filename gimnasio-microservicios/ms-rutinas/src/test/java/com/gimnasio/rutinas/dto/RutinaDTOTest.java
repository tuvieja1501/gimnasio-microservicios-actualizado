package com.gimnasio.rutinas.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class RutinaDTOTest {
    static Validator validator;
    @BeforeAll static void setup() { validator = Validation.buildDefaultValidatorFactory().getValidator(); }

    EjercicioDTO ejercicio() {
        EjercicioDTO e = new EjercicioDTO(); e.setNombre("Press Banca"); e.setSeries(3); e.setRepeticiones(10);
        return e;
    }

    RutinaDTO valido() {
        RutinaDTO d = new RutinaDTO(); d.setNombre("Rutina A"); d.setSocioId(1L); d.setInstructorId(2L);
        d.setDuracionSemanas(8); d.setEjercicios(List.of(ejercicio()));
        return d;
    }

    @Test void dtoValido() { assertThat(validator.validate(valido())).isEmpty(); }
    @Test void nombreVacio() { RutinaDTO d = valido(); d.setNombre(""); assertThat(validator.validate(d)).isNotEmpty(); }
    @Test void sinEjercicios() { RutinaDTO d = valido(); d.setEjercicios(List.of()); assertThat(validator.validate(d)).isNotEmpty(); }
    @Test void duracionExcesiva() { RutinaDTO d = valido(); d.setDuracionSemanas(53); assertThat(validator.validate(d)).isNotEmpty(); }
    @Test void gettersSetters() {
        RutinaDTO d = valido(); d.setObjetivo("Ganar masa");
        assertThat(d.getNombre()).isEqualTo("Rutina A"); assertThat(d.getSocioId()).isEqualTo(1L);
        assertThat(d.getInstructorId()).isEqualTo(2L); assertThat(d.getDuracionSemanas()).isEqualTo(8);
        assertThat(d.getObjetivo()).isEqualTo("Ganar masa"); assertThat(d.getEjercicios()).hasSize(1);
    }

    @Test void ejercicioDTOGettersSetters() {
        EjercicioDTO e = new EjercicioDTO(); e.setNombre("Curl"); e.setSeries(4); e.setRepeticiones(12);
        e.setDescansoSegundos(60); e.setObservaciones("Con barra");
        assertThat(e.getNombre()).isEqualTo("Curl"); assertThat(e.getSeries()).isEqualTo(4);
        assertThat(e.getRepeticiones()).isEqualTo(12); assertThat(e.getDescansoSegundos()).isEqualTo(60);
        assertThat(e.getObservaciones()).isEqualTo("Con barra");
    }

    @Test void ejercicioDTOValidaciones() {
        EjercicioDTO e = ejercicio(); e.setSeries(0);
        assertThat(validator.validate(e)).isNotEmpty();
    }

    @Test void respuestasGetters() {
        SocioRespuesta s = new SocioRespuesta(); s.setId(1L); s.setNombre("Ana"); s.setApellido("S"); s.setEstado("ACTIVO");
        assertThat(s.getEstado()).isEqualTo("ACTIVO");
        InstructorRespuesta ir = new InstructorRespuesta(); ir.setId(1L); ir.setNombre("C"); ir.setApellido("L");
        ir.setEspecialidad("Yoga"); ir.setActivo(true);
        assertThat(ir.getActivo()).isTrue();
    }
}

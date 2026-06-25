package com.gimnasio.instructores.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class InstructorModelTest {
    @Test void gettersSetters() {
        Instructor i = new Instructor();
        i.setId(1L); i.setNombre("Carlos"); i.setApellido("Lopez"); i.setRut("12345678-9");
        i.setEmail("carlos@mail.com"); i.setEspecialidad("Yoga"); i.setAniosExperiencia(5); i.setActivo(true);
        assertThat(i.getId()).isEqualTo(1L); assertThat(i.getNombre()).isEqualTo("Carlos");
        assertThat(i.getApellido()).isEqualTo("Lopez"); assertThat(i.getRut()).isEqualTo("12345678-9");
        assertThat(i.getEmail()).isEqualTo("carlos@mail.com"); assertThat(i.getEspecialidad()).isEqualTo("Yoga");
        assertThat(i.getAniosExperiencia()).isEqualTo(5); assertThat(i.getActivo()).isTrue();
    }
}

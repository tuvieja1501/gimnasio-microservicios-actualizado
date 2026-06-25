package com.gimnasio.instructores.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class InstructorDTOTest {
    static Validator validator;
    @BeforeAll static void setup() { validator = Validation.buildDefaultValidatorFactory().getValidator(); }

    InstructorDTO valido() {
        InstructorDTO d = new InstructorDTO(); d.setNombre("Carlos"); d.setApellido("Lopez");
        d.setRut("12345678-9"); d.setEmail("carlos@mail.com"); d.setEspecialidad("Yoga");
        d.setAniosExperiencia(5); d.setActivo(true);
        return d;
    }

    @Test void dtoValido() { assertThat(validator.validate(valido())).isEmpty(); }
    @Test void nombreVacio() { InstructorDTO d = valido(); d.setNombre(""); assertThat(validator.validate(d)).isNotEmpty(); }
    @Test void rutInvalido() { InstructorDTO d = valido(); d.setRut("abc"); assertThat(validator.validate(d)).isNotEmpty(); }
    @Test void emailInvalido() { InstructorDTO d = valido(); d.setEmail("no-email"); assertThat(validator.validate(d)).isNotEmpty(); }
    @Test void aniosNegativos() { InstructorDTO d = valido(); d.setAniosExperiencia(-1); assertThat(validator.validate(d)).isNotEmpty(); }
    @Test void gettersSetters() {
        InstructorDTO d = valido();
        assertThat(d.getNombre()).isEqualTo("Carlos"); assertThat(d.getApellido()).isEqualTo("Lopez");
        assertThat(d.getRut()).isEqualTo("12345678-9"); assertThat(d.getEmail()).isEqualTo("carlos@mail.com");
        assertThat(d.getEspecialidad()).isEqualTo("Yoga"); assertThat(d.getAniosExperiencia()).isEqualTo(5);
        assertThat(d.getActivo()).isTrue();
    }
}

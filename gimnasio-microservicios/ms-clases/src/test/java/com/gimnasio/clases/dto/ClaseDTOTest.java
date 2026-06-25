package com.gimnasio.clases.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class ClaseDTOTest {
    static Validator validator;
    @BeforeAll static void setup() { validator = Validation.buildDefaultValidatorFactory().getValidator(); }

    ClaseDTO valido() {
        ClaseDTO d = new ClaseDTO(); d.setNombre("Yoga"); d.setInstructorId(1L); d.setSucursalId(1L);
        d.setFechaHora(LocalDateTime.now().plusDays(1)); d.setDuracionMinutos(60); d.setCupoMaximo(20);
        return d;
    }

    @Test void dtoValido() { assertThat(validator.validate(valido())).isEmpty(); }
    @Test void nombreVacio() { ClaseDTO d = valido(); d.setNombre(""); assertThat(validator.validate(d)).isNotEmpty(); }
    @Test void fechaPasada() { ClaseDTO d = valido(); d.setFechaHora(LocalDateTime.now().minusDays(1)); assertThat(validator.validate(d)).isNotEmpty(); }
    @Test void duracionMinima() { ClaseDTO d = valido(); d.setDuracionMinutos(10); assertThat(validator.validate(d)).isNotEmpty(); }
    @Test void cupoMinimoUno() { ClaseDTO d = valido(); d.setCupoMaximo(0); assertThat(validator.validate(d)).isNotEmpty(); }
    @Test void gettersSetters() {
        ClaseDTO d = valido(); d.setDescripcion("Clase relajante");
        assertThat(d.getNombre()).isEqualTo("Yoga"); assertThat(d.getInstructorId()).isEqualTo(1L);
        assertThat(d.getSucursalId()).isEqualTo(1L); assertThat(d.getDuracionMinutos()).isEqualTo(60);
        assertThat(d.getCupoMaximo()).isEqualTo(20); assertThat(d.getDescripcion()).isEqualTo("Clase relajante");
    }

    @Test void respuestasDTOGetters() {
        InstructorRespuesta ir = new InstructorRespuesta(); ir.setId(1L); ir.setNombre("Juan");
        ir.setApellido("L"); ir.setEspecialidad("Yoga"); ir.setActivo(true);
        assertThat(ir.getId()).isEqualTo(1L); assertThat(ir.getActivo()).isTrue();

        SucursalRespuesta sr = new SucursalRespuesta(); sr.setId(2L); sr.setNombre("Sede A");
        sr.setDireccion("Dir 1"); sr.setCapacidad(80); sr.setActiva(true);
        assertThat(sr.getId()).isEqualTo(2L); assertThat(sr.getCapacidad()).isEqualTo(80);
    }
}

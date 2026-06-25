package com.gimnasio.sucursales.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.time.LocalTime;
import static org.assertj.core.api.Assertions.assertThat;

class SucursalDTOTest {
    static Validator validator;
    @BeforeAll static void setup() { validator = Validation.buildDefaultValidatorFactory().getValidator(); }

    SucursalDTO valido() {
        SucursalDTO d = new SucursalDTO(); d.setNombre("Sede A"); d.setDireccion("Calle 1");
        d.setComuna("Santiago"); d.setCapacidad(100); d.setHoraApertura(LocalTime.of(7,0));
        d.setHoraCierre(LocalTime.of(22,0)); d.setActiva(true);
        return d;
    }

    @Test void dtoValido() { assertThat(validator.validate(valido())).isEmpty(); }
    @Test void nombreVacio() { SucursalDTO d = valido(); d.setNombre(""); assertThat(validator.validate(d)).isNotEmpty(); }
    @Test void capacidadCero() { SucursalDTO d = valido(); d.setCapacidad(0); assertThat(validator.validate(d)).isNotEmpty(); }
    @Test void capacidadExcesiva() { SucursalDTO d = valido(); d.setCapacidad(501); assertThat(validator.validate(d)).isNotEmpty(); }
    @Test void horaAperturaNull() { SucursalDTO d = valido(); d.setHoraApertura(null); assertThat(validator.validate(d)).isNotEmpty(); }
    @Test void gettersSetters() {
        SucursalDTO d = valido(); d.setTelefono("+56222222222");
        assertThat(d.getNombre()).isEqualTo("Sede A"); assertThat(d.getComuna()).isEqualTo("Santiago");
        assertThat(d.getCapacidad()).isEqualTo(100); assertThat(d.getActiva()).isTrue();
        assertThat(d.getTelefono()).isEqualTo("+56222222222");
        assertThat(d.getDireccion()).isEqualTo("Calle 1");
        assertThat(d.getHoraApertura()).isEqualTo(LocalTime.of(7,0));
        assertThat(d.getHoraCierre()).isEqualTo(LocalTime.of(22,0));
    }
}

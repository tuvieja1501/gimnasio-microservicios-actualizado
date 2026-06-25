package com.gimnasio.equipos.dto;

import com.gimnasio.equipos.model.EstadoEquipo;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class EquipoDTOTest {
    static Validator validator;
    @BeforeAll static void setup() { validator = Validation.buildDefaultValidatorFactory().getValidator(); }

    EquipoDTO valido() {
        EquipoDTO d = new EquipoDTO(); d.setNombre("Cinta"); d.setTipo("Cardio");
        d.setCodigoInterno("ABC"); d.setSucursalId(1L); d.setEstado(EstadoEquipo.OPERATIVO);
        return d;
    }

    @Test void dtoValido() { assertThat(validator.validate(valido())).isEmpty(); }
    @Test void nombreVacio() { EquipoDTO d = valido(); d.setNombre(""); assertThat(validator.validate(d)).isNotEmpty(); }
    @Test void codigoCorto() { EquipoDTO d = valido(); d.setCodigoInterno("AB"); assertThat(validator.validate(d)).isNotEmpty(); }
    @Test void sucursalNull() { EquipoDTO d = valido(); d.setSucursalId(null); assertThat(validator.validate(d)).isNotEmpty(); }
    @Test void gettersSetters() {
        EquipoDTO d = valido();
        assertThat(d.getNombre()).isEqualTo("Cinta"); assertThat(d.getTipo()).isEqualTo("Cardio");
        assertThat(d.getCodigoInterno()).isEqualTo("ABC"); assertThat(d.getSucursalId()).isEqualTo(1L);
        assertThat(d.getEstado()).isEqualTo(EstadoEquipo.OPERATIVO);
    }

    @Test void sucursalRespuestaGetters() {
        SucursalRespuesta s = new SucursalRespuesta(); s.setId(1L); s.setNombre("Sede A"); s.setActiva(true);
        assertThat(s.getId()).isEqualTo(1L); assertThat(s.getActiva()).isTrue();
    }
}

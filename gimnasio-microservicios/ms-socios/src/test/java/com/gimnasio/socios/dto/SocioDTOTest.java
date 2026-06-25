package com.gimnasio.socios.dto;

import com.gimnasio.socios.model.EstadoSocio;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SocioDTO - validaciones Bean Validation")
class SocioDTOTest {

    static Validator validator;

    @BeforeAll static void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private SocioDTO valido() {
        SocioDTO d = new SocioDTO();
        d.setNombre("Ana"); d.setApellido("Soto"); d.setRut("11111111-1");
        d.setEmail("ana@mail.com"); d.setFechaNacimiento(LocalDate.now().minusYears(20));
        d.setEstado(EstadoSocio.ACTIVO);
        return d;
    }

    @Test @DisplayName("DTO valido no produce errores")
    void dtoValido() {
        assertThat(validator.validate(valido())).isEmpty();
    }

    @Test @DisplayName("nombre en blanco -> error")
    void nombreBlanco() {
        SocioDTO d = valido(); d.setNombre("");
        assertThat(validator.validate(d)).isNotEmpty();
    }

    @Test @DisplayName("RUT invalido -> error")
    void rutInvalido() {
        SocioDTO d = valido(); d.setRut("abc");
        assertThat(validator.validate(d)).isNotEmpty();
    }

    @Test @DisplayName("email invalido -> error")
    void emailInvalido() {
        SocioDTO d = valido(); d.setEmail("no-es-email");
        assertThat(validator.validate(d)).isNotEmpty();
    }

    @Test @DisplayName("fechaNacimiento futura -> error")
    void fechaNacimientoFutura() {
        SocioDTO d = valido(); d.setFechaNacimiento(LocalDate.now().plusDays(1));
        assertThat(validator.validate(d)).isNotEmpty();
    }

    @Test @DisplayName("estado null -> error")
    void estadoNull() {
        SocioDTO d = valido(); d.setEstado(null);
        assertThat(validator.validate(d)).isNotEmpty();
    }

    @Test @DisplayName("getters y setters funcionan")
    void gettersSetters() {
        SocioDTO d = valido();
        assertThat(d.getNombre()).isEqualTo("Ana");
        assertThat(d.getApellido()).isEqualTo("Soto");
        assertThat(d.getRut()).isEqualTo("11111111-1");
        assertThat(d.getEmail()).isEqualTo("ana@mail.com");
        assertThat(d.getEstado()).isEqualTo(EstadoSocio.ACTIVO);
        d.setTelefono("+56911112222");
        assertThat(d.getTelefono()).isEqualTo("+56911112222");
    }
}

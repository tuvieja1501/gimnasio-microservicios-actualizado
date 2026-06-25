package com.gimnasio.pagos.dto;

import com.gimnasio.pagos.model.MetodoPago;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

class PagoDTOTest {
    static Validator validator;
    @BeforeAll static void setup() { validator = Validation.buildDefaultValidatorFactory().getValidator(); }

    PagoDTO valido() {
        PagoDTO d = new PagoDTO(); d.setSocioId(1L); d.setMembresiaId(2L);
        d.setMonto(new BigDecimal("50000")); d.setMetodoPago(MetodoPago.EFECTIVO);
        return d;
    }

    @Test void dtoValido() { assertThat(validator.validate(valido())).isEmpty(); }
    @Test void socioIdNull() { PagoDTO d = valido(); d.setSocioId(null); assertThat(validator.validate(d)).isNotEmpty(); }
    @Test void montoNegativo() { PagoDTO d = valido(); d.setMonto(BigDecimal.ZERO); assertThat(validator.validate(d)).isNotEmpty(); }
    @Test void metodoPagoNull() { PagoDTO d = valido(); d.setMetodoPago(null); assertThat(validator.validate(d)).isNotEmpty(); }
    @Test void gettersSetters() {
        PagoDTO d = valido(); d.setReferencia("REF-001");
        assertThat(d.getSocioId()).isEqualTo(1L); assertThat(d.getMembresiaId()).isEqualTo(2L);
        assertThat(d.getMonto()).isEqualByComparingTo("50000");
        assertThat(d.getMetodoPago()).isEqualTo(MetodoPago.EFECTIVO);
        assertThat(d.getReferencia()).isEqualTo("REF-001");
    }

    @Test void socioRespuestaGetters() {
        SocioRespuesta s = new SocioRespuesta(); s.setId(1L); s.setNombre("Ana"); s.setApellido("Soto");
        s.setRut("11111111-1"); s.setEstado("ACTIVO");
        assertThat(s.getId()).isEqualTo(1L); assertThat(s.getRut()).isEqualTo("11111111-1");
    }
}

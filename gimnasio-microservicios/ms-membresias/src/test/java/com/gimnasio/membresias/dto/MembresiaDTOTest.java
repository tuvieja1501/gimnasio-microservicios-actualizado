package com.gimnasio.membresias.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

class MembresiaDTOTest {
    static Validator validator;
    @BeforeAll static void setup() { validator = Validation.buildDefaultValidatorFactory().getValidator(); }

    @Test void membresiaValido() {
        MembresiaDTO d = new MembresiaDTO(); d.setSocioId(1L); d.setPlanId(2L); d.setFechaInicio(LocalDate.now());
        assertThat(validator.validate(d)).isEmpty();
    }

    @Test void socioIdNull() {
        MembresiaDTO d = new MembresiaDTO(); d.setPlanId(2L); d.setFechaInicio(LocalDate.now());
        assertThat(validator.validate(d)).isNotEmpty();
    }

    @Test void membresiaGettersSetters() {
        MembresiaDTO d = new MembresiaDTO(); d.setSocioId(5L); d.setPlanId(3L); d.setFechaInicio(LocalDate.now());
        assertThat(d.getSocioId()).isEqualTo(5L); assertThat(d.getPlanId()).isEqualTo(3L);
        assertThat(d.getFechaInicio()).isEqualTo(LocalDate.now());
    }

    @Test void planValido() {
        PlanMembresiaDTO p = new PlanMembresiaDTO(); p.setNombre("Premium"); p.setDuracionMeses(3);
        p.setPrecio(new BigDecimal("80000")); p.setActivo(true);
        assertThat(validator.validate(p)).isEmpty();
    }

    @Test void planDuracionInvalida() {
        PlanMembresiaDTO p = new PlanMembresiaDTO(); p.setNombre("X"); p.setDuracionMeses(0);
        p.setPrecio(new BigDecimal("1000")); p.setActivo(true);
        assertThat(validator.validate(p)).isNotEmpty();
    }

    @Test void planPrecioNegativo() {
        PlanMembresiaDTO p = new PlanMembresiaDTO(); p.setNombre("X"); p.setDuracionMeses(1);
        p.setPrecio(BigDecimal.ZERO); p.setActivo(true);
        assertThat(validator.validate(p)).isNotEmpty();
    }

    @Test void planGettersSetters() {
        PlanMembresiaDTO p = new PlanMembresiaDTO(); p.setNombre("VIP"); p.setDescripcion("Plan top");
        p.setDuracionMeses(12); p.setPrecio(new BigDecimal("200000")); p.setActivo(false);
        assertThat(p.getNombre()).isEqualTo("VIP"); assertThat(p.getDescripcion()).isEqualTo("Plan top");
        assertThat(p.getDuracionMeses()).isEqualTo(12); assertThat(p.getActivo()).isFalse();
    }

    @Test void socioRespuestaGetters() {
        SocioRespuesta s = new SocioRespuesta(); s.setId(1L); s.setNombre("A"); s.setApellido("B");
        s.setRut("11111111-1"); s.setEmail("a@b.com"); s.setEstado("ACTIVO");
        assertThat(s.getRut()).isEqualTo("11111111-1"); assertThat(s.getEmail()).isEqualTo("a@b.com");
    }
}

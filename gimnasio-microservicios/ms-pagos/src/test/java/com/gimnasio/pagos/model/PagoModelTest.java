package com.gimnasio.pagos.model;

import org.junit.jupiter.api.Test;

import com.gimnasio.pagos.model.EstadoPago;
import com.gimnasio.pagos.model.MetodoPago;
import com.gimnasio.pagos.model.Pago;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class PagoModelTest {
    @Test void gettersSetters() {
        Pago p = new Pago(); LocalDateTime ahora = LocalDateTime.now();
        p.setId(1L); p.setSocioId(10L); p.setMembresiaId(5L);
        p.setMonto(new BigDecimal("50000")); p.setMetodoPago(MetodoPago.TRANSFERENCIA);
        p.setFechaPago(ahora); p.setEstado(EstadoPago.PAGADO); p.setReferencia("REF-001");
        assertThat(p.getId()).isEqualTo(1L); assertThat(p.getSocioId()).isEqualTo(10L);
        assertThat(p.getMembresiaId()).isEqualTo(5L);
        assertThat(p.getMonto()).isEqualByComparingTo("50000");
        assertThat(p.getMetodoPago()).isEqualTo(MetodoPago.TRANSFERENCIA);
        assertThat(p.getFechaPago()).isEqualTo(ahora);
        assertThat(p.getEstado()).isEqualTo(EstadoPago.PAGADO);
        assertThat(p.getReferencia()).isEqualTo("REF-001");
    }

    @Test void estadoPagoEnum() {
        assertThat(EstadoPago.values()).containsExactlyInAnyOrder(EstadoPago.PAGADO, EstadoPago.PENDIENTE, EstadoPago.ANULADO);
    }

    @Test void metodoPagoEnum() {
        assertThat(MetodoPago.values()).containsExactlyInAnyOrder(
                MetodoPago.EFECTIVO, MetodoPago.TARJETA_CREDITO, MetodoPago.TARJETA_DEBITO, MetodoPago.TRANSFERENCIA);
    }
}

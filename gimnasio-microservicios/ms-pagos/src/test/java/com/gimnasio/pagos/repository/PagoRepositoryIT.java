package com.gimnasio.pagos.repository;

import com.gimnasio.pagos.model.EstadoPago;
import com.gimnasio.pagos.model.MetodoPago;
import com.gimnasio.pagos.model.Pago;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("PagoRepository - pruebas de integracion (H2)")
class PagoRepositoryIT {

    @Autowired
    private PagoRepository repo;

    private Pago crear(Long socioId, Long membresiaId) {
        Pago p = new Pago();
        p.setSocioId(socioId);
        p.setMembresiaId(membresiaId);
        p.setMonto(new BigDecimal("19990"));
        p.setMetodoPago(MetodoPago.EFECTIVO);
        p.setFechaPago(LocalDateTime.now());
        p.setEstado(EstadoPago.PAGADO);
        return p;
    }

    @Test
    @DisplayName("findBySocioId() filtra correctamente")
    void findBySocioIdFiltraCorrectamente() {
        repo.save(crear(10L, 1L));
        repo.save(crear(10L, 2L));
        repo.save(crear(20L, 1L));

        List<Pago> resultado = repo.findBySocioId(10L);

        assertThat(resultado).hasSize(2);
    }

    @Test
    @DisplayName("findByMembresiaId() filtra correctamente")
    void findByMembresiaIdFiltraCorrectamente() {
        repo.save(crear(10L, 1L));
        repo.save(crear(20L, 1L));
        repo.save(crear(20L, 2L));

        List<Pago> resultado = repo.findByMembresiaId(1L);

        assertThat(resultado).hasSize(2);
    }
}

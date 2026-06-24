package com.gimnasio.reservas.repository;

import com.gimnasio.reservas.model.EstadoReserva;
import com.gimnasio.reservas.model.Reserva;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@DisplayName("ReservaRepository - pruebas de integracion (H2)")
class ReservaRepositoryIT {

    @Autowired
    private ReservaRepository repo;

    private Reserva crear(Long socioId, Long claseId) {
        Reserva r = new Reserva();
        r.setSocioId(socioId);
        r.setClaseId(claseId);
        r.setFechaReserva(LocalDateTime.now());
        r.setEstado(EstadoReserva.CONFIRMADA);
        return r;
    }

    @Test
    @DisplayName("findBySocioIdAndClaseId() encuentra la reserva exacta")
    void findBySocioIdAndClaseIdEncuentraReserva() {
        repo.save(crear(10L, 20L));

        Optional<Reserva> resultado = repo.findBySocioIdAndClaseId(10L, 20L);

        assertThat(resultado).isPresent();
    }

    @Test
    @DisplayName("findBySocioId() y findByClaseId() filtran correctamente")
    void findBySocioIdYClaseIdFiltranCorrectamente() {
        repo.save(crear(10L, 20L));
        repo.save(crear(10L, 21L));
        repo.save(crear(11L, 20L));

        assertThat(repo.findBySocioId(10L)).hasSize(2);
        assertThat(repo.findByClaseId(20L)).hasSize(2);
    }

    @Test
    @DisplayName("la restriccion unica (socio_id, clase_id) impide reservas duplicadas a nivel de BD")
    void restriccionUnicaImpideDuplicados() {
        repo.save(crear(10L, 20L));
        repo.flush();

        assertThatThrownBy(() -> {
            repo.save(crear(10L, 20L));
            repo.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}

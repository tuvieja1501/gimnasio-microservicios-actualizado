package com.gimnasio.asistencias.repository;

import com.gimnasio.asistencias.model.Asistencia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("AsistenciaRepository - pruebas de integracion (H2)")
class AsistenciaRepositoryIT {

    @Autowired
    private AsistenciaRepository repo;

    private Asistencia crear(Long socioId, LocalDateTime ingreso, LocalDateTime salida) {
        Asistencia a = new Asistencia();
        a.setSocioId(socioId);
        a.setSucursalId(1L);
        a.setFechaIngreso(ingreso);
        a.setFechaSalida(salida);
        return a;
    }

    @Test
    @DisplayName("findFirstBySocioIdAndFechaSalidaIsNullOrderByFechaIngresoDesc() encuentra el ingreso abierto mas reciente")
    void encuentraIngresoAbiertoMasReciente() {
        repo.save(crear(10L, LocalDateTime.now().minusHours(5), LocalDateTime.now().minusHours(4)));
        repo.save(crear(10L, LocalDateTime.now().minusHours(2), null)); // abierto, mas reciente

        Optional<Asistencia> resultado = repo
                .findFirstBySocioIdAndFechaSalidaIsNullOrderByFechaIngresoDesc(10L);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getFechaSalida()).isNull();
    }

    @Test
    @DisplayName("retorna vacio si todos los ingresos del socio ya tienen salida registrada")
    void retornaVacioSiNoHayIngresoAbierto() {
        repo.save(crear(10L, LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(2)));

        Optional<Asistencia> resultado = repo
                .findFirstBySocioIdAndFechaSalidaIsNullOrderByFechaIngresoDesc(10L);

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("findBySocioId() retorna todo el historial del socio")
    void findBySocioIdRetornaHistorial() {
        repo.save(crear(10L, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(2).plusHours(1)));
        repo.save(crear(10L, LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1).plusHours(1)));
        repo.save(crear(20L, LocalDateTime.now(), null));

        List<Asistencia> resultado = repo.findBySocioId(10L);

        assertThat(resultado).hasSize(2);
    }
}

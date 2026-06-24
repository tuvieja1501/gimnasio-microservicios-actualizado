package com.gimnasio.clases.repository;

import com.gimnasio.clases.model.Clase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("ClaseRepository - pruebas de integracion (H2)")
class ClaseRepositoryIT {

    @Autowired
    private ClaseRepository repo;

    private Clase crear(String nombre, Long instructorId, Long sucursalId, LocalDateTime fechaHora) {
        Clase c = new Clase();
        c.setNombre(nombre);
        c.setInstructorId(instructorId);
        c.setSucursalId(sucursalId);
        c.setFechaHora(fechaHora);
        c.setDuracionMinutos(45);
        c.setCupoMaximo(20);
        c.setCuposDisponibles(20);
        return c;
    }

    @Test
    @DisplayName("findByInstructorId() filtra correctamente")
    void findByInstructorIdFiltraCorrectamente() {
        repo.save(crear("Spinning", 1L, 1L, LocalDateTime.now().plusDays(1)));
        repo.save(crear("Yoga", 2L, 1L, LocalDateTime.now().plusDays(1)));

        List<Clase> resultado = repo.findByInstructorId(1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Spinning");
    }

    @Test
    @DisplayName("findByFechaHoraAfter() solo retorna clases futuras")
    void findByFechaHoraAfterFiltraFuturas() {
        repo.save(crear("Clase pasada", 1L, 1L, LocalDateTime.now().minusDays(1)));
        repo.save(crear("Clase futura", 1L, 1L, LocalDateTime.now().plusDays(1)));

        List<Clase> futuras = repo.findByFechaHoraAfter(LocalDateTime.now());

        assertThat(futuras).hasSize(1);
        assertThat(futuras.get(0).getNombre()).isEqualTo("Clase futura");
    }

    @Test
    @DisplayName("findBySucursalId() filtra correctamente")
    void findBySucursalIdFiltraCorrectamente() {
        repo.save(crear("Pilates", 1L, 5L, LocalDateTime.now().plusDays(2)));

        List<Clase> resultado = repo.findBySucursalId(5L);

        assertThat(resultado).hasSize(1);
    }
}

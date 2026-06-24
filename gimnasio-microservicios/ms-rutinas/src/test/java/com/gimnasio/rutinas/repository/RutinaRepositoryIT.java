package com.gimnasio.rutinas.repository;

import com.gimnasio.rutinas.model.Ejercicio;
import com.gimnasio.rutinas.model.Rutina;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("RutinaRepository - pruebas de integracion (H2)")
class RutinaRepositoryIT {

    @Autowired
    private RutinaRepository repo;

    private Rutina crearConEjercicio(Long socioId, Long instructorId) {
        Rutina r = new Rutina();
        r.setNombre("Fuerza nivel 1");
        r.setObjetivo("Ganar masa muscular");
        r.setSocioId(socioId);
        r.setInstructorId(instructorId);
        r.setDuracionSemanas(8);
        r.setFechaCreacion(LocalDate.now());

        Ejercicio e = new Ejercicio();
        e.setNombre("Sentadillas");
        e.setSeries(4);
        e.setRepeticiones(12);
        e.setDescansoSegundos(60);
        e.setRutina(r);
        r.getEjercicios().add(e);

        return r;
    }

    @Test
    @DisplayName("guarda la rutina junto con sus ejercicios por cascada (cascade=ALL)")
    void guardaRutinaConEjerciciosEnCascada() {
        Rutina guardada = repo.saveAndFlush(crearConEjercicio(10L, 1L));

        Optional<Rutina> recuperada = repo.findById(guardada.getId());

        assertThat(recuperada).isPresent();
        assertThat(recuperada.get().getEjercicios()).hasSize(1);
        assertThat(recuperada.get().getEjercicios().get(0).getNombre()).isEqualTo("Sentadillas");
    }

    @Test
    @DisplayName("findBySocioId() filtra correctamente")
    void findBySocioIdFiltraCorrectamente() {
        repo.save(crearConEjercicio(10L, 1L));
        repo.save(crearConEjercicio(20L, 1L));

        List<Rutina> resultado = repo.findBySocioId(10L);

        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("findByInstructorId() filtra correctamente")
    void findByInstructorIdFiltraCorrectamente() {
        repo.save(crearConEjercicio(10L, 1L));
        repo.save(crearConEjercicio(11L, 1L));
        repo.save(crearConEjercicio(12L, 2L));

        List<Rutina> resultado = repo.findByInstructorId(1L);

        assertThat(resultado).hasSize(2);
    }
}

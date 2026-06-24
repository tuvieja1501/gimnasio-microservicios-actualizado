package com.gimnasio.instructores.repository;

import com.gimnasio.instructores.model.Instructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("InstructorRepository - pruebas de integracion (H2)")
class InstructorRepositoryIT {

    @Autowired
    private InstructorRepository repo;

    private Instructor crear(String rut, String email, String especialidad, boolean activo) {
        Instructor i = new Instructor();
        i.setNombre("Carlos");
        i.setApellido("Gomez");
        i.setRut(rut);
        i.setEmail(email);
        i.setEspecialidad(especialidad);
        i.setAniosExperiencia(5);
        i.setActivo(activo);
        return i;
    }

    @Test
    @DisplayName("existsByRut() y existsByEmail() reflejan el estado real de la base")
    void existsByRutYEmail() {
        repo.save(crear("11111111-1", "carlos@mail.com", "Yoga", true));

        assertThat(repo.existsByRut("11111111-1")).isTrue();
        assertThat(repo.existsByEmail("carlos@mail.com")).isTrue();
        assertThat(repo.existsByRut("00000000-0")).isFalse();
    }

    @Test
    @DisplayName("findByActivoTrue() solo retorna instructores activos")
    void findByActivoTrueFiltraCorrectamente() {
        repo.save(crear("22222222-2", "activo@mail.com", "Spinning", true));
        repo.save(crear("33333333-3", "inactivo@mail.com", "Spinning", false));

        List<Instructor> activos = repo.findByActivoTrue();

        assertThat(activos).hasSize(1);
        assertThat(activos.get(0).getEmail()).isEqualTo("activo@mail.com");
    }

    @Test
    @DisplayName("findByEspecialidadIgnoreCase() ignora mayusculas/minusculas")
    void findByEspecialidadIgnoraMayusculas() {
        repo.save(crear("44444444-4", "cross@mail.com", "Crossfit", true));

        List<Instructor> resultado = repo.findByEspecialidadIgnoreCase("CROSSFIT");

        assertThat(resultado).hasSize(1);
    }
}

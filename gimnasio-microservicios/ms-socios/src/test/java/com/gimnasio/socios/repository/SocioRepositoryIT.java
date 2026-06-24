package com.gimnasio.socios.repository;

import com.gimnasio.socios.model.EstadoSocio;
import com.gimnasio.socios.model.Socio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de integracion de la capa de persistencia: levanta un contexto Spring
 * acotado a JPA con una base de datos H2 en memoria (no se mockea el
 * repositorio, se valida la query real generada por Spring Data contra una
 * base de datos relacional real).
 */
@DataJpaTest
@DisplayName("SocioRepository - pruebas de integracion (H2)")
class SocioRepositoryIT {

    @Autowired
    private SocioRepository repo;

    private Socio crearSocio(String rut, String email) {
        Socio s = new Socio();
        s.setNombre("Ana");
        s.setApellido("Soto");
        s.setRut(rut);
        s.setEmail(email);
        s.setTelefono("+56911112222");
        s.setFechaNacimiento(LocalDate.now().minusYears(25));
        s.setFechaRegistro(LocalDate.now());
        s.setEstado(EstadoSocio.ACTIVO);
        return s;
    }

    @Test
    @DisplayName("guarda y recupera un socio por id")
    void guardaYRecuperaPorId() {
        Socio guardado = repo.save(crearSocio("11111111-1", "ana.soto@mail.com"));

        Optional<Socio> recuperado = repo.findById(guardado.getId());

        assertThat(recuperado).isPresent();
        assertThat(recuperado.get().getRut()).isEqualTo("11111111-1");
    }

    @Test
    @DisplayName("existsByRut() retorna true solo si el RUT esta persistido")
    void existsByRutFuncionaCorrectamente() {
        repo.save(crearSocio("22222222-2", "otro@mail.com"));

        assertThat(repo.existsByRut("22222222-2")).isTrue();
        assertThat(repo.existsByRut("99999999-9")).isFalse();
    }

    @Test
    @DisplayName("existsByEmail() respeta la restriccion de unicidad de email")
    void existsByEmailFuncionaCorrectamente() {
        repo.save(crearSocio("33333333-3", "unico@mail.com"));

        assertThat(repo.existsByEmail("unico@mail.com")).isTrue();
        assertThat(repo.existsByEmail("noexiste@mail.com")).isFalse();
    }

    @Test
    @DisplayName("findByRut() devuelve el socio correspondiente")
    void findByRutDevuelveSocioCorrecto() {
        repo.save(crearSocio("44444444-4", "cuarto@mail.com"));

        Optional<Socio> resultado = repo.findByRut("44444444-4");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getEmail()).isEqualTo("cuarto@mail.com");
    }

    @Test
    @DisplayName("findByRut() retorna vacio si el RUT no existe")
    void findByRutVacioSiNoExiste() {
        assertThat(repo.findByRut("00000000-0")).isEmpty();
    }
}

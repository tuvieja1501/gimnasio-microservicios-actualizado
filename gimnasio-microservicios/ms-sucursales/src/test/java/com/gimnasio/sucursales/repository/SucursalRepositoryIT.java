package com.gimnasio.sucursales.repository;

import com.gimnasio.sucursales.model.Sucursal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("SucursalRepository - pruebas de integracion (H2)")
class SucursalRepositoryIT {

    @Autowired
    private SucursalRepository repo;

    private Sucursal crear(String nombre, String comuna, boolean activa) {
        Sucursal s = new Sucursal();
        s.setNombre(nombre);
        s.setDireccion("Calle Falsa 123");
        s.setComuna(comuna);
        s.setTelefono("+56911112222");
        s.setCapacidad(50);
        s.setHoraApertura(LocalTime.of(7, 0));
        s.setHoraCierre(LocalTime.of(22, 0));
        s.setActiva(activa);
        return s;
    }

    @Test
    @DisplayName("existsByNombre() respeta la restriccion de unicidad")
    void existsByNombreFuncionaCorrectamente() {
        repo.save(crear("Sucursal Centro", "Santiago", true));

        assertThat(repo.existsByNombre("Sucursal Centro")).isTrue();
        assertThat(repo.existsByNombre("Sucursal Inexistente")).isFalse();
    }

    @Test
    @DisplayName("findByActivaTrue() solo retorna sucursales activas")
    void findByActivaTrueFiltraCorrectamente() {
        repo.save(crear("Sucursal Norte", "Providencia", true));
        repo.save(crear("Sucursal Cerrada", "Maipu", false));

        List<Sucursal> activas = repo.findByActivaTrue();

        assertThat(activas).hasSize(1);
        assertThat(activas.get(0).getNombre()).isEqualTo("Sucursal Norte");
    }

    @Test
    @DisplayName("findByComunaIgnoreCase() ignora mayusculas/minusculas")
    void findByComunaIgnoraMayusculas() {
        repo.save(crear("Sucursal Las Condes", "Las Condes", true));

        List<Sucursal> resultado = repo.findByComunaIgnoreCase("las condes");

        assertThat(resultado).hasSize(1);
    }
}

package com.gimnasio.equipos.repository;

import com.gimnasio.equipos.model.Equipo;
import com.gimnasio.equipos.model.EstadoEquipo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("EquipoRepository - pruebas de integracion (H2)")
class EquipoRepositoryIT {

    @Autowired
    private EquipoRepository repo;

    private Equipo crear(String codigo, Long sucursalId) {
        Equipo e = new Equipo();
        e.setNombre("Cinta de correr");
        e.setTipo("Cardio");
        e.setCodigoInterno(codigo);
        e.setSucursalId(sucursalId);
        e.setEstado(EstadoEquipo.OPERATIVO);
        return e;
    }

    @Test
    @DisplayName("existsByCodigoInterno() respeta la restriccion de unicidad")
    void existsByCodigoInternoFuncionaCorrectamente() {
        repo.save(crear("EQ-001", 1L));

        assertThat(repo.existsByCodigoInterno("EQ-001")).isTrue();
        assertThat(repo.existsByCodigoInterno("EQ-999")).isFalse();
    }

    @Test
    @DisplayName("findBySucursalId() filtra correctamente")
    void findBySucursalIdFiltraCorrectamente() {
        repo.save(crear("EQ-100", 1L));
        repo.save(crear("EQ-101", 2L));

        List<Equipo> resultado = repo.findBySucursalId(1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getCodigoInterno()).isEqualTo("EQ-100");
    }
}

package com.gimnasio.membresias.repository;

import com.gimnasio.membresias.model.EstadoMembresia;
import com.gimnasio.membresias.model.Membresia;
import com.gimnasio.membresias.model.PlanMembresia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("MembresiaRepository / PlanMembresiaRepository - pruebas de integracion (H2)")
class MembresiaRepositoryIT {

    @Autowired
    private MembresiaRepository membresiaRepo;

    @Autowired
    private PlanMembresiaRepository planRepo;

    private PlanMembresia plan;

    @BeforeEach
    void setUp() {
        PlanMembresia p = new PlanMembresia();
        p.setNombre("Mensual Basico");
        p.setDuracionMeses(1);
        p.setPrecio(new BigDecimal("19990"));
        p.setActivo(true);
        plan = planRepo.save(p);
    }

    private Membresia crearMembresia(Long socioId, EstadoMembresia estado, LocalDate fechaFin) {
        Membresia m = new Membresia();
        m.setSocioId(socioId);
        m.setPlan(plan);
        m.setFechaInicio(LocalDate.now().minusDays(10));
        m.setFechaFin(fechaFin);
        m.setEstado(estado);
        return membresiaRepo.save(m);
    }

    @Test
    @DisplayName("findByActivoTrue() en planes solo retorna los activos")
    void findPlanesActivos() {
        PlanMembresia inactivo = new PlanMembresia();
        inactivo.setNombre("Plan descontinuado");
        inactivo.setDuracionMeses(6);
        inactivo.setPrecio(new BigDecimal("50000"));
        inactivo.setActivo(false);
        planRepo.save(inactivo);

        List<PlanMembresia> activos = planRepo.findByActivoTrue();

        assertThat(activos).extracting(PlanMembresia::getNombre).containsExactly("Mensual Basico");
    }

    @Test
    @DisplayName("findBySocioId() retorna todas las membresias de un socio")
    void findBySocioIdRetornaTodas() {
        crearMembresia(10L, EstadoMembresia.VENCIDA, LocalDate.now().minusMonths(1));
        crearMembresia(10L, EstadoMembresia.VIGENTE, LocalDate.now().plusMonths(1));
        crearMembresia(20L, EstadoMembresia.VIGENTE, LocalDate.now().plusMonths(1));

        List<Membresia> resultado = membresiaRepo.findBySocioId(10L);

        assertThat(resultado).hasSize(2);
    }

    @Test
    @DisplayName("existsBySocioIdAndEstado() detecta una membresia VIGENTE existente")
    void existsBySocioIdAndEstadoDetectaVigente() {
        crearMembresia(10L, EstadoMembresia.VIGENTE, LocalDate.now().plusMonths(1));

        assertThat(membresiaRepo.existsBySocioIdAndEstado(10L, EstadoMembresia.VIGENTE)).isTrue();
        assertThat(membresiaRepo.existsBySocioIdAndEstado(10L, EstadoMembresia.CANCELADA)).isFalse();
        assertThat(membresiaRepo.existsBySocioIdAndEstado(99L, EstadoMembresia.VIGENTE)).isFalse();
    }

    @Test
    @DisplayName("findFirstBySocioIdAndEstadoOrderByFechaFinDesc() retorna la mas reciente")
    void findFirstOrdenaPorFechaFinDesc() {
        crearMembresia(10L, EstadoMembresia.VIGENTE, LocalDate.now().plusMonths(1));
        crearMembresia(10L, EstadoMembresia.VIGENTE, LocalDate.now().plusMonths(3));

        Optional<Membresia> resultado = membresiaRepo
                .findFirstBySocioIdAndEstadoOrderByFechaFinDesc(10L, EstadoMembresia.VIGENTE);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getFechaFin()).isEqualTo(LocalDate.now().plusMonths(3));
    }
}

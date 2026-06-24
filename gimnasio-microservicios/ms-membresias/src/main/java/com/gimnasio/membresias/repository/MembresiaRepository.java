package com.gimnasio.membresias.repository;

import com.gimnasio.membresias.model.EstadoMembresia;
import com.gimnasio.membresias.model.Membresia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembresiaRepository extends JpaRepository<Membresia, Long> {

    List<Membresia> findBySocioId(Long socioId);

    Optional<Membresia> findFirstBySocioIdAndEstadoOrderByFechaFinDesc(
            Long socioId, EstadoMembresia estado);

    boolean existsBySocioIdAndEstado(Long socioId, EstadoMembresia estado);
}

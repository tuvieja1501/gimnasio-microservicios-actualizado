package com.gimnasio.reservas.repository;

import com.gimnasio.reservas.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findBySocioId(Long socioId);

    List<Reserva> findByClaseId(Long claseId);

    Optional<Reserva> findBySocioIdAndClaseId(Long socioId, Long claseId);
}

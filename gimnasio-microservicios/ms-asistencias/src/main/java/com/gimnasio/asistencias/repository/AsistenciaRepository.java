package com.gimnasio.asistencias.repository;

import com.gimnasio.asistencias.model.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    List<Asistencia> findBySocioId(Long socioId);

    Optional<Asistencia> findFirstBySocioIdAndFechaSalidaIsNullOrderByFechaIngresoDesc(Long socioId);
}

package com.gimnasio.rutinas.repository;

import com.gimnasio.rutinas.model.Rutina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RutinaRepository extends JpaRepository<Rutina, Long> {
    List<Rutina> findBySocioId(Long socioId);
    List<Rutina> findByInstructorId(Long instructorId);
}

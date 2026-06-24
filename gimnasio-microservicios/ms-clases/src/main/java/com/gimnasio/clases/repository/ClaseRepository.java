package com.gimnasio.clases.repository;

import com.gimnasio.clases.model.Clase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClaseRepository extends JpaRepository<Clase, Long> {
    List<Clase> findByInstructorId(Long instructorId);
    List<Clase> findBySucursalId(Long sucursalId);
    List<Clase> findByFechaHoraAfter(LocalDateTime fecha);
}

package com.gimnasio.instructores.repository;

import com.gimnasio.instructores.model.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Long> {
    boolean existsByRut(String rut);
    boolean existsByEmail(String email);
    List<Instructor> findByActivoTrue();
    List<Instructor> findByEspecialidadIgnoreCase(String especialidad);
}

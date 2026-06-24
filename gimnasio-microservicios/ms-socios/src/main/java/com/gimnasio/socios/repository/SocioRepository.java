package com.gimnasio.socios.repository;

import com.gimnasio.socios.model.Socio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad Socio.
 * Provee operaciones CRUD reales sobre PostgreSQL.
 */
@Repository
public interface SocioRepository extends JpaRepository<Socio, Long> {

    Optional<Socio> findByRut(String rut);

    Optional<Socio> findByEmail(String email);

    boolean existsByRut(String rut);

    boolean existsByEmail(String email);
}

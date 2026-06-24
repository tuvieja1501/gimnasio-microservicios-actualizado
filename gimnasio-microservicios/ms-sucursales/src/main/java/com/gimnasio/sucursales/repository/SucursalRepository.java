package com.gimnasio.sucursales.repository;

import com.gimnasio.sucursales.model.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SucursalRepository extends JpaRepository<Sucursal, Long> {
    boolean existsByNombre(String nombre);
    List<Sucursal> findByActivaTrue();
    List<Sucursal> findByComunaIgnoreCase(String comuna);
}

package com.gimnasio.asistencias.client;

import com.gimnasio.asistencias.dto.SucursalRespuesta;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-sucursales", url = "${ms-sucursales.url:http://localhost:8090}")
public interface SucursalClient {

    @GetMapping("/api/sucursales/{id}")
    SucursalRespuesta obtenerSucursal(@PathVariable("id") Long id);
}

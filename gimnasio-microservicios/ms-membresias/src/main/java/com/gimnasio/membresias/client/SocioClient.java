package com.gimnasio.membresias.client;

import com.gimnasio.membresias.dto.SocioRespuesta;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign Client para comunicarse con ms-socios.
 * Permite validar que el socio exista antes de crear una membresia.
 */
@FeignClient(name = "ms-socios", url = "${ms-socios.url:http://localhost:8081}")
public interface SocioClient {

    @GetMapping("/api/socios/{id}")
    SocioRespuesta obtenerSocio(@PathVariable("id") Long id);
}

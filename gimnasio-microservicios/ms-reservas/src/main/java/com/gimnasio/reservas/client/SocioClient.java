package com.gimnasio.reservas.client;

import com.gimnasio.reservas.dto.SocioRespuesta;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-socios", url = "${ms-socios.url:http://localhost:8081}")
public interface SocioClient {

    @GetMapping("/api/socios/{id}")
    SocioRespuesta obtenerSocio(@PathVariable("id") Long id);
}

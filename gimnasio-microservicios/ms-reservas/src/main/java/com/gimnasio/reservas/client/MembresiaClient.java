package com.gimnasio.reservas.client;

import com.gimnasio.reservas.dto.MembresiaVigenteRespuesta;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-membresias", url = "${ms-membresias.url:http://localhost:8082}")
public interface MembresiaClient {

    @GetMapping("/api/membresias/socio/{socioId}/vigente")
    MembresiaVigenteRespuesta tieneVigente(@PathVariable("socioId") Long socioId);
}

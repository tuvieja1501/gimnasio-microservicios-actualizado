package com.gimnasio.reservas.client;

import com.gimnasio.reservas.dto.ClaseRespuesta;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-clases", url = "${ms-clases.url:http://localhost:8084}")
public interface ClaseClient {

    @GetMapping("/api/clases/{id}")
    ClaseRespuesta obtenerClase(@PathVariable("id") Long id);

    @PatchMapping("/api/clases/{id}/decrementar-cupo")
    ClaseRespuesta decrementarCupo(@PathVariable("id") Long id);

    @PatchMapping("/api/clases/{id}/incrementar-cupo")
    ClaseRespuesta incrementarCupo(@PathVariable("id") Long id);
}

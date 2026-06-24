package com.gimnasio.clases.client;

import com.gimnasio.clases.dto.InstructorRespuesta;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-instructores", url = "${ms-instructores.url:http://localhost:8083}")
public interface InstructorClient {

    @GetMapping("/api/instructores/{id}")
    InstructorRespuesta obtenerInstructor(@PathVariable("id") Long id);
}

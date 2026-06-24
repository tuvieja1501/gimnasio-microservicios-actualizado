package com.gimnasio.reservas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.cloud.openfeign.EnableFeignClients;

@OpenAPIDefinition(info = @Info(title = "ms-reservas API", version = "1.0",
        description = "Reservas de cupo en clases; orquesta ms-socios, ms-membresias y ms-clases via Feign con compensacion/rollback."))
@SpringBootApplication
@EnableFeignClients
public class ReservasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservasApplication.class, args);
    }
}

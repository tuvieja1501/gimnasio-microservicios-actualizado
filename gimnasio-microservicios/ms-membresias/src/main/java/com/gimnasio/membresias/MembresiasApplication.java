package com.gimnasio.membresias;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.cloud.openfeign.EnableFeignClients;

@OpenAPIDefinition(info = @Info(title = "ms-membresias API", version = "1.0",
        description = "Planes y membresias de socios; valida estado del socio remoto en ms-socios via Feign."))
@SpringBootApplication
@EnableFeignClients
public class MembresiasApplication {

    public static void main(String[] args) {
        SpringApplication.run(MembresiasApplication.class, args);
    }
}

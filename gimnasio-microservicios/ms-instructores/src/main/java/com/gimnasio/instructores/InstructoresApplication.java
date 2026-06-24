package com.gimnasio.instructores;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.cloud.openfeign.EnableFeignClients;

@OpenAPIDefinition(info = @Info(title = "ms-instructores API", version = "1.0",
        description = "Gestion de instructores del gimnasio y sus especialidades."))
@SpringBootApplication
@EnableFeignClients
public class InstructoresApplication {

    public static void main(String[] args) {
        SpringApplication.run(InstructoresApplication.class, args);
    }
}

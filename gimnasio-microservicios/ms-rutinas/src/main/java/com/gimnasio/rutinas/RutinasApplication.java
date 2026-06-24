package com.gimnasio.rutinas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.cloud.openfeign.EnableFeignClients;

@OpenAPIDefinition(info = @Info(title = "ms-rutinas API", version = "1.0",
        description = "Rutinas de entrenamiento con ejercicios anidados; valida socio e instructor remotos via Feign."))
@SpringBootApplication
@EnableFeignClients
public class RutinasApplication {

    public static void main(String[] args) {
        SpringApplication.run(RutinasApplication.class, args);
    }
}

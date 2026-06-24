package com.gimnasio.asistencias;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.cloud.openfeign.EnableFeignClients;

@OpenAPIDefinition(info = @Info(title = "ms-asistencias API", version = "1.0",
        description = "Control de ingreso/salida de socios; valida socio y sucursal remotos via Feign."))
@SpringBootApplication
@EnableFeignClients
public class AsistenciasApplication {

    public static void main(String[] args) {
        SpringApplication.run(AsistenciasApplication.class, args);
    }
}

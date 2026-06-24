package com.gimnasio.sucursales;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.cloud.openfeign.EnableFeignClients;

@OpenAPIDefinition(info = @Info(title = "ms-sucursales API", version = "1.0",
        description = "Sedes fisicas del gimnasio; consumido por ms-clases y ms-equipos via Feign."))
@SpringBootApplication
@EnableFeignClients
public class SucursalesApplication {

    public static void main(String[] args) {
        SpringApplication.run(SucursalesApplication.class, args);
    }
}

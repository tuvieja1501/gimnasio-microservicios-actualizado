package com.gimnasio.equipos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.cloud.openfeign.EnableFeignClients;

@OpenAPIDefinition(info = @Info(title = "ms-equipos API", version = "1.0",
        description = "Inventario de equipos por sucursal; valida sucursal remota via Feign."))
@SpringBootApplication
@EnableFeignClients
public class EquiposApplication {

    public static void main(String[] args) {
        SpringApplication.run(EquiposApplication.class, args);
    }
}

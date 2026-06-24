package com.gimnasio.clases;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.cloud.openfeign.EnableFeignClients;

@OpenAPIDefinition(info = @Info(title = "ms-clases API", version = "1.0",
        description = "Clases agendadas; valida instructor y sucursal remotos via Feign, gestiona cupos."))
@SpringBootApplication
@EnableFeignClients
public class ClasesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClasesApplication.class, args);
    }
}

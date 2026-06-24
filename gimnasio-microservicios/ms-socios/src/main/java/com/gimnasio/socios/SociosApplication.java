package com.gimnasio.socios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.cloud.openfeign.EnableFeignClients;

@OpenAPIDefinition(info = @Info(title = "ms-socios API", version = "1.0",
        description = "Gestion de socios del gimnasio: alta, busqueda, estados y reglas de unicidad (RUT/email)."))
@SpringBootApplication
@EnableFeignClients
public class SociosApplication {

    public static void main(String[] args) {
        SpringApplication.run(SociosApplication.class, args);
    }
}

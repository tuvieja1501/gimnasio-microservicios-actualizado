package com.gimnasio.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del API Gateway.
 *
 * Este microservicio NO contiene logica de negocio: su unica responsabilidad
 * es recibir todas las solicitudes externas en el puerto 8080 y enrutarlas
 * (proxy reverso) hacia el microservicio interno correspondiente, segun las
 * rutas definidas en application.yml (ver spring.cloud.gateway.routes).
 *
 * Beneficios de centralizar el enrutamiento aqui:
 *  - El cliente (frontend, Postman, app movil) solo necesita conocer UNA URL
 *    base (http://localhost:8080) en vez de los 10 puertos individuales.
 *  - Permite aplicar filtros transversales (logging, headers, rate-limit,
 *    autenticacion futura) en un solo lugar sin tocar cada microservicio.
 *  - Si un microservicio cambia de puerto o se mueve a otro host (ej. al
 *    desplegar en Docker/Railway), solo se actualiza la ruta en el Gateway.
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}

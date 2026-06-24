package com.gimnasio.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de integracion minima: levanta el contexto completo de Spring
 * (incluyendo la configuracion de Spring Cloud Gateway leida desde
 * application.yml) y verifica que las 10 rutas declaradas se hayan
 * registrado correctamente. Si hubiera un error de sintaxis en el YAML
 * o una ruta mal definida, esta prueba fallaria al arrancar el contexto.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@DisplayName("GatewayApplication - carga de contexto y rutas")
class GatewayApplicationTests {

    @org.springframework.beans.factory.annotation.Autowired
    private RouteLocator routeLocator;

    @Test
    @DisplayName("el contexto levanta y registra las rutas hacia los 10 microservicios")
    void contextoLevantaConRutasRegistradas() {
        var rutas = routeLocator.getRoutes().collectList().block();

        assertThat(rutas).isNotNull();
        assertThat(rutas).extracting(r -> r.getId())
                .contains("ms-socios", "ms-membresias", "ms-planes-membresia",
                        "ms-instructores", "ms-clases", "ms-reservas", "ms-pagos",
                        "ms-equipos", "ms-rutinas", "ms-asistencias", "ms-sucursales");
    }
}

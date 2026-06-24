package com.gimnasio.gateway.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Prueba unitaria del filtro global. No se levanta un microservicio real:
 * se simula el ServerWebExchange (request/response reactivos) y se verifica
 * que el filtro agregue el header de trazabilidad antes de continuar la
 * cadena de filtros (GatewayFilterChain).
 */
@DisplayName("LoggingGlobalFilter - pruebas unitarias")
class LoggingGlobalFilterTest {

    private final LoggingGlobalFilter filtro = new LoggingGlobalFilter();

    @Test
    @DisplayName("agrega el header X-Gateway-Trace-Id a la solicitud antes de continuar la cadena")
    void agregaHeaderDeTrazabilidad() {
        ServerHttpRequest request = MockServerHttpRequest.get("/api/socios/1").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        Mono<Void> resultado = filtro.filter(exchange, chain);

        StepVerifier.create(resultado).verifyComplete();
        verify(chain).filter(argThat(mutado ->
                mutado.getRequest().getHeaders().containsKey("X-Gateway-Trace-Id")));
    }

    @Test
    @DisplayName("tiene orden -1 para ejecutarse antes que los filtros propios de cada ruta")
    void tieneOrdenCorrecto() {
        assertThat(filtro.getOrder()).isEqualTo(-1);
    }

    @Test
    @DisplayName("propaga el error y registra el tiempo transcurrido si la cadena falla")
    void propagaErrorDeLaCadena() {
        ServerHttpRequest request = MockServerHttpRequest.post("/api/pagos").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        RuntimeException fallo = new RuntimeException("servicio destino no disponible");
        when(chain.filter(any())).thenReturn(Mono.error(fallo));

        Mono<Void> resultado = filtro.filter(exchange, chain);

        StepVerifier.create(resultado).expectErrorMatches(e -> e == fallo).verify();
    }

    @Test
    @DisplayName("conserva el metodo y la URI originales de la solicitud al mutarla")
    void conservaMetodoYUriOriginales() {
        ServerHttpRequest request = MockServerHttpRequest.get("/api/clases/5").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filtro.filter(exchange, chain)).verifyComplete();

        verify(chain).filter(argThat(mutado ->
                mutado.getRequest().getMethod() == request.getMethod()
                        && mutado.getRequest().getURI().equals(request.getURI())));
    }
}

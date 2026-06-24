package com.gimnasio.gateway.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("LoggingGlobalFilter - pruebas unitarias")
class LoggingGlobalFilterTest {

    private final LoggingGlobalFilter filtro = new LoggingGlobalFilter();

    @Test
    @DisplayName("agrega el header X-Gateway-Trace-Id a la solicitud antes de continuar la cadena")
    void agregaHeaderDeTrazabilidad() {
        // Usar MockServerHttpRequest directamente (no la interfaz)
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/socios/1").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

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
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/pagos").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        RuntimeException fallo = new RuntimeException("servicio destino no disponible");
        when(chain.filter(any())).thenReturn(Mono.error(fallo));

        Mono<Void> resultado = filtro.filter(exchange, chain);

        StepVerifier.create(resultado).expectErrorMatches(e -> e == fallo).verify();
    }

    @Test
    @DisplayName("conserva el metodo y la URI originales de la solicitud al mutarla")
    void conservaMetodoYUriOriginales() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/clases/5").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filtro.filter(exchange, chain)).verifyComplete();

        verify(chain).filter(argThat(mutado ->
                mutado.getRequest().getMethod() == request.getMethod()
                        && mutado.getRequest().getURI().equals(request.getURI())));
    }

    @Test
    @DisplayName("el traceId agregado al header no es nulo ni vacio")
    void traceIdNoEsVacio() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/membresias").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filtro.filter(exchange, chain)).verifyComplete();

        verify(chain).filter(argThat(mutado -> {
            String traceId = mutado.getRequest().getHeaders().getFirst("X-Gateway-Trace-Id");
            return traceId != null && !traceId.isBlank();
        }));
    }

    @Test
    @DisplayName("funciona correctamente con metodo DELETE")
    void funcionaConMetodoDelete() {
        MockServerHttpRequest request = MockServerHttpRequest.delete("/api/socios/99").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filtro.filter(exchange, chain)).verifyComplete();

        verify(chain).filter(argThat(mutado ->
                mutado.getRequest().getMethod() == HttpMethod.DELETE));
    }
}
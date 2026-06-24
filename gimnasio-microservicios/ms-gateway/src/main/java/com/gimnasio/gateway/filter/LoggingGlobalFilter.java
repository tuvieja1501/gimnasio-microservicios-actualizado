package com.gimnasio.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Filtro global: se ejecuta para CUALQUIER ruta que pase por el Gateway
 * (a diferencia de un filtro por-ruta, que se configura solo en una entrada
 * especifica de application.yml).
 *
 * Hace dos cosas:
 *  1) Agrega un header "X-Gateway-Trace-Id" a la solicitud antes de
 *     reenviarla al microservicio destino, util para correlacionar logs
 *     entre el Gateway y el microservicio en un escenario distribuido.
 *  2) Registra en el log de cuanto demoro la solicitud completa
 *     (tiempo en el Gateway + tiempo del microservicio destino).
 */
@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingGlobalFilter.class);
    private static final String TRACE_HEADER = "X-Gateway-Trace-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Instant inicio = Instant.now();
        String traceId = UUID.randomUUID().toString().substring(0, 8);

        ServerHttpRequest mutada = exchange.getRequest().mutate()
                .header(TRACE_HEADER, traceId)
                .build();

        log.info("[{}] -> {} {}", traceId,
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI());

        return chain.filter(exchange.mutate().request(mutada).build())
                .doFinally(signal -> {
                    long ms = java.time.Duration.between(inicio, Instant.now()).toMillis();
                    log.info("[{}] <- {} ({} ms)", traceId,
                            exchange.getResponse().getStatusCode(), ms);
                });
    }

    @Override
    public int getOrder() {
        return -1; // se ejecuta antes que los filtros propios de cada ruta
    }
}

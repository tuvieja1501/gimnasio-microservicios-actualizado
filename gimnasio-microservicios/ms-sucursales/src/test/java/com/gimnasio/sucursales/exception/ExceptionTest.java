package com.gimnasio.sucursales.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ExceptionTest {
    @Test void recursoNoEncontrado() {
        RecursoNoEncontradoException ex = new RecursoNoEncontradoException("no existe");
        assertThat(ex.getMessage()).isEqualTo("no existe");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }
    @Test void reglaNegocio() {
        ReglaNegocioException ex = new ReglaNegocioException("horario invalido");
        assertThat(ex.getMessage()).isEqualTo("horario invalido");
    }
    @Test void handlerNotFound() {
        GlobalExceptionHandler h = new GlobalExceptionHandler();
        assertThat(h.handleNoEncontrado(new RecursoNoEncontradoException("x")).getStatusCodeValue()).isEqualTo(404);
    }
    @Test void handlerConflict() {
        GlobalExceptionHandler h = new GlobalExceptionHandler();
        assertThat(h.handleReglaNegocio(new ReglaNegocioException("x")).getStatusCodeValue()).isEqualTo(409);
    }
    @Test void handlerGeneral() {
        GlobalExceptionHandler h = new GlobalExceptionHandler();
        assertThat(h.handleGeneral(new RuntimeException("x")).getStatusCodeValue()).isEqualTo(500);
    }
}

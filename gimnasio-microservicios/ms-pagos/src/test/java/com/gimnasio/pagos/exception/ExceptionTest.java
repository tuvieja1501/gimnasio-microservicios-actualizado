package com.gimnasio.pagos.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ExceptionTest {
    @Test void recursoNoEncontrado() { assertThat(new RecursoNoEncontradoException("x").getMessage()).isEqualTo("x"); }
    @Test void reglaNegocio() { assertThat(new ReglaNegocioException("y").getMessage()).isEqualTo("y"); }
    @Test void handler404() { assertThat(new GlobalExceptionHandler().handleNoEncontrado(new RecursoNoEncontradoException("x")).getStatusCodeValue()).isEqualTo(404); }
    @Test void handler409() { assertThat(new GlobalExceptionHandler().handleReglaNegocio(new ReglaNegocioException("x")).getStatusCodeValue()).isEqualTo(409); }
    @Test void handler500() { assertThat(new GlobalExceptionHandler().handleGeneral(new RuntimeException("x")).getStatusCodeValue()).isEqualTo(500); }
}

package com.gimnasio.socios.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Exceptions y GlobalExceptionHandler")
class ExceptionTest {

    @Test @DisplayName("RecursoNoEncontradoException guarda mensaje")
    void recursoNoEncontrado() {
        RecursoNoEncontradoException ex = new RecursoNoEncontradoException("no existe id 99");
        assertThat(ex.getMessage()).contains("99");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test @DisplayName("ReglaNegocioException guarda mensaje")
    void reglaNegocio() {
        ReglaNegocioException ex = new ReglaNegocioException("RUT duplicado");
        assertThat(ex.getMessage()).contains("RUT");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test @DisplayName("GlobalExceptionHandler - handleNoEncontrado retorna 404")
    void handlerNotFound() {
        GlobalExceptionHandler h = new GlobalExceptionHandler();
        var resp = h.handleNoEncontrado(new RecursoNoEncontradoException("not found"));
        assertThat(resp.getStatusCodeValue()).isEqualTo(404);
        assertThat(resp.getBody()).containsKey("mensaje");
    }

    @Test @DisplayName("GlobalExceptionHandler - handleReglaNegocio retorna 409")
    void handlerConflict() {
        GlobalExceptionHandler h = new GlobalExceptionHandler();
        var resp = h.handleReglaNegocio(new ReglaNegocioException("conflict"));
        assertThat(resp.getStatusCodeValue()).isEqualTo(409);
    }

    @Test @DisplayName("GlobalExceptionHandler - handleGeneral retorna 500")
    void handlerGeneral() {
        GlobalExceptionHandler h = new GlobalExceptionHandler();
        var resp = h.handleGeneral(new RuntimeException("error inesperado"));
        assertThat(resp.getStatusCodeValue()).isEqualTo(500);
    }
}

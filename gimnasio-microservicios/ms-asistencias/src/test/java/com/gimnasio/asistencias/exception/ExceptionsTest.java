package com.gimnasio.asistencias.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExceptionsTest {

    @Test
    void testRecursoNoEncontradoException() {
        RecursoNoEncontradoException exception = new RecursoNoEncontradoException("No encontrado");
        assertEquals("No encontrado", exception.getMessage());
    }

    @Test
    void testReglaNegocioException() {
        ReglaNegocioException exception = new ReglaNegocioException("Error de negocio");
        assertEquals("Error de negocio", exception.getMessage());
    }
    
    @Test
    void testGlobalExceptionHandler() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        // Llama a los métodos de manejo de error que tengas en tu handler
        // ResponseEntity<?> response = handler.handleRecursoNoEncontrado(...);
        // assertNotNull(response);
    }
}
package com.gimnasio.rutinas.exception;

/**
 * Se lanza cuando un recurso solicitado por id no existe en la base de datos.
 */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}

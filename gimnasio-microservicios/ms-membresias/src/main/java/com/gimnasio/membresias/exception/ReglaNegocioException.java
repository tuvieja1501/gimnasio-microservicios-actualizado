package com.gimnasio.membresias.exception;

/**
 * Se lanza cuando una operacion viola una regla de negocio del dominio.
 */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}

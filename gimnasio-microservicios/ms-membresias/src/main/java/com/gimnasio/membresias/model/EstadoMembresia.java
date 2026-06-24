package com.gimnasio.membresias.model;

/**
 * VIGENTE  : esta dentro del periodo de validez.
 * VENCIDA  : ya paso la fecha fin.
 * CANCELADA: fue dada de baja antes de su vencimiento.
 */
public enum EstadoMembresia {
    VIGENTE,
    VENCIDA,
    CANCELADA
}

package com.gimnasio.socios.model;

/**
 * Estados posibles de un socio.
 * ACTIVO   : puede acceder al gimnasio y reservar clases.
 * INACTIVO : registrado pero suspendido temporalmente.
 * MOROSO   : con deudas pendientes, acceso restringido.
 */
public enum EstadoSocio {
    ACTIVO,
    INACTIVO,
    MOROSO
}

package com.logisticaandina.scgl.excepciones;

/**
 * Excepcion base de las validaciones de asignacion.
 * Al lanzarse, el motor NO persiste ninguna operacion
 * (flujo alternativo del CU-02: rechazo -> sin persistencia).
 */
public class AsignacionInvalidaException extends Exception {
    public AsignacionInvalidaException(String mensaje) { super(mensaje); }
}

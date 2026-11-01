package com.logisticaandina.scgl.excepciones;

/**
 * Rechazo de una operacion del modulo de mantenimiento por regla de negocio
 * (p. ej. abrir una segunda orden sobre la misma unidad o cerrar una orden ya cerrada).
 * No se persiste nada cuando se lanza.
 */
public class MantenimientoInvalidoException extends Exception {
    public MantenimientoInvalidoException(String mensaje) { super(mensaje); }
}

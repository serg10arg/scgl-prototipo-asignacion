package com.logisticaandina.scgl.excepciones;

/**
 * Error de la capa de persistencia. Ante un fallo dentro de una
 * transaccion, se ejecuta rollback y se propaga esta excepcion (RNF1).
 */
public class PersistenciaException extends Exception {
    public PersistenciaException(String mensaje, Throwable causa) { super(mensaje, causa); }
    public PersistenciaException(String mensaje) { super(mensaje); }
}

package com.logisticaandina.scgl.excepciones;

/**
 * Error de la capa de persistencia. Ante un fallo dentro de una
 * transaccion, se ejecuta rollback y se propaga esta excepcion (RNF1).
 * Envuelve la SQLException original como causa para no perder el detalle tecnico.
 */
public class PersistenciaException extends Exception {
    public PersistenciaException(String mensaje, Throwable causa) { super(mensaje, causa); }
    public PersistenciaException(String mensaje) { super(mensaje); }

    /**
     * Mensaje para la interfaz: el texto de negocio mas la causa tecnica original
     * (p. ej. el mensaje de MySQL), para diagnosticar sin ocultar el error real.
     */
    public String getDetalle() {
        Throwable causa = getCause();
        return (causa == null || causa.getMessage() == null)
                ? getMessage()
                : getMessage() + " (causa: " + causa.getMessage() + ")";
    }
}

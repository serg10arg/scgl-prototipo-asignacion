package com.logisticaandina.scgl.excepciones;

/** El vehiculo no cumple RN1 (estado) o RN4 (mantenimiento pendiente). */
public class VehiculoNoDisponibleException extends AsignacionInvalidaException {
    public VehiculoNoDisponibleException(String mensaje) { super(mensaje); }
}

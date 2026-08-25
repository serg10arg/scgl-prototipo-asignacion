package com.logisticaandina.scgl.excepciones;

/** El conductor no cumple RN2 (horas) o RN3 (licencia). */
public class ConductorNoDisponibleException extends AsignacionInvalidaException {
    public ConductorNoDisponibleException(String mensaje) { super(mensaje); }
}

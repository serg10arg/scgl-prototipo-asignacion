package com.logisticaandina.scgl.excepciones;

/** El vehiculo no cumple RN5 (capacidad insuficiente para la carga). */
public class CapacidadInsuficienteException extends AsignacionInvalidaException {
    public CapacidadInsuficienteException(String mensaje) { super(mensaje); }
}

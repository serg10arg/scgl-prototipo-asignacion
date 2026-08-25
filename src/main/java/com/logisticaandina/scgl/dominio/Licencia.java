package com.logisticaandina.scgl.dominio;

import java.time.LocalDate;

/** Licencia habilitante del conductor (regla RN3). */
public class Licencia {

    private final String numero;
    private final String tipo;
    private final LocalDate fechaVencimiento;

    public Licencia(String numero, String tipo, LocalDate fechaVencimiento) {
        this.numero = numero;
        this.tipo = tipo;
        this.fechaVencimiento = fechaVencimiento;
    }

    /** RN3: la licencia esta vigente a la fecha indicada. */
    public boolean estaVigente(LocalDate fecha) {
        return !fechaVencimiento.isBefore(fecha);
    }

    public String getNumero() { return numero; }
    public String getTipo() { return tipo; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
}

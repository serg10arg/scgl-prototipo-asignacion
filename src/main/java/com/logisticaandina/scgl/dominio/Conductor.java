package com.logisticaandina.scgl.dominio;

import java.time.LocalDate;

/** Conductor (RF2). Encapsula las reglas de disponibilidad RN2 y RN3. */
public class Conductor {

    private int id;
    private String nombre;
    private Licencia licencia;
    private double horasAcumuladas;

    public Conductor(int id, String nombre, Licencia licencia, double horasAcumuladas) {
        this.id = id;
        this.nombre = nombre;
        this.licencia = licencia;
        this.horasAcumuladas = horasAcumuladas;
    }

    /** RN3: posee licencia habilitante vigente. */
    public boolean tieneLicenciaVigente(LocalDate fecha) {
        return licencia.estaVigente(fecha);
    }

    /** RN2: no excede el limite legal de horas al sumar el viaje. */
    public boolean tieneHorasDisponibles(double horasViaje, double limiteLegalHoras) {
        return (horasAcumuladas + horasViaje) <= limiteLegalHoras;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public Licencia getLicencia() { return licencia; }
    public double getHorasAcumuladas() { return horasAcumuladas; }
}

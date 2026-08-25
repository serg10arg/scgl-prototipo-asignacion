package com.logisticaandina.scgl.dominio;

/**
 * Clase abstracta base de la jerarquia de vehiculos.
 * Encapsula el estado y las reglas de negocio comunes (RN1, RN4, RN5).
 * Las subclasses concretas definen el comportamiento diferenciado
 * mediante polimorfismo (costoPorKilometro, getTipo).
 */
public abstract class Vehiculo {

    protected int id;
    protected String patente;
    protected double capacidadKg;
    protected int kmActual;
    protected int kmUltimoMantenimiento;
    protected int umbralMantenimientoKm;
    protected EstadoVehiculo estado;

    protected Vehiculo(int id, String patente, double capacidadKg, int kmActual,
                       int kmUltimoMantenimiento, int umbralMantenimientoKm,
                       EstadoVehiculo estado) {
        this.id = id;
        this.patente = patente;
        this.capacidadKg = capacidadKg;
        this.kmActual = kmActual;
        this.kmUltimoMantenimiento = kmUltimoMantenimiento;
        this.umbralMantenimientoKm = umbralMantenimientoKm;
        this.estado = estado;
    }

    /** Polimorfismo: cada tipo define su costo por km (base para indicadores RF11). */
    public abstract double costoPorKilometro();

    /** Identifica el tipo concreto (usado tambien como discriminador de persistencia). */
    public abstract TipoVehiculo getTipo();

    /** Regla RN4: requiere mantenimiento preventivo si supero el umbral desde el ultimo service. */
    public boolean requiereMantenimiento() {
        return (kmActual - kmUltimoMantenimiento) >= umbralMantenimientoKm;
    }

    /** Regla RN1 (estado): la unidad esta habilitada operativamente. */
    public boolean estaOperativo() {
        return estado == EstadoVehiculo.OPERATIVO;
    }

    /** Regla RN5: compatibilidad de capacidad con el peso de la carga. */
    public boolean puedeTransportar(double pesoKg) {
        return this.capacidadKg >= pesoKg;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPatente() { return patente; }
    public double getCapacidadKg() { return capacidadKg; }
    public int getKmActual() { return kmActual; }
    public EstadoVehiculo getEstado() { return estado; }
}

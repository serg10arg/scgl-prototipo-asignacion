package com.logisticaandina.scgl.dominio;

import java.time.LocalDate;

/**
 * Orden de mantenimiento de una unidad (RF-09 / CU-06).
 * Encapsula su ciclo de vida: solo una orden ABIERTA puede modificarse o cerrarse.
 * El cierre es la baja logica de la orden: no se borra, para conservar el historial (RF-10).
 *
 * Guarda el id y la patente del vehiculo (obtenida por JOIN) en lugar del objeto Vehiculo
 * completo: alcanza para operar y presentar la orden sin una consulta extra por fila.
 */
public class OrdenMantenimiento {

    private int id;
    private final int idVehiculo;
    private final String patenteVehiculo;
    private TipoOrden tipo;
    private EstadoOrden estado;
    private final LocalDate fecha;

    /** Constructor de alta (CU-06, paso 5): la orden nace ABIERTA. */
    public OrdenMantenimiento(int idVehiculo, String patenteVehiculo, TipoOrden tipo, LocalDate fecha) {
        this(0, idVehiculo, patenteVehiculo, tipo, EstadoOrden.ABIERTA, fecha);
    }

    /** Constructor de reconstruccion desde la base de datos. */
    public OrdenMantenimiento(int id, int idVehiculo, String patenteVehiculo, TipoOrden tipo,
                              EstadoOrden estado, LocalDate fecha) {
        this.id = id;
        this.idVehiculo = idVehiculo;
        this.patenteVehiculo = patenteVehiculo;
        this.tipo = tipo;
        this.estado = estado;
        this.fecha = fecha;
    }

    public boolean estaAbierta() {
        return estado == EstadoOrden.ABIERTA;
    }

    /** Modificacion: corrige el tipo de una orden mientras sigue abierta. */
    public void cambiarTipo(TipoOrden nuevoTipo) {
        if (!estaAbierta()) {
            throw new IllegalStateException("La orden #" + id + " esta cerrada y no puede modificarse.");
        }
        this.tipo = nuevoTipo;
    }

    /** Baja logica (CU-06, E1): la orden pasa a CERRADA. */
    public void cerrar() {
        if (!estaAbierta()) {
            throw new IllegalStateException("La orden #" + id + " ya estaba cerrada.");
        }
        this.estado = EstadoOrden.CERRADA;
    }

    /**
     * CU-06, E1.2 «si corresponde»: solo el preventivo reinicia el kilometraje de
     * referencia del ultimo mantenimiento (base de RN4); un correctivo no lo hace.
     */
    public boolean actualizaKmDeReferencia() {
        return tipo == TipoOrden.PREVENTIVO;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdVehiculo() { return idVehiculo; }
    public String getPatenteVehiculo() { return patenteVehiculo; }
    public TipoOrden getTipo() { return tipo; }
    public EstadoOrden getEstado() { return estado; }
    public LocalDate getFecha() { return fecha; }
}

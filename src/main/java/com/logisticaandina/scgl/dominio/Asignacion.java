package com.logisticaandina.scgl.dominio;

import java.time.LocalDateTime;

/** Asignacion viaje-vehiculo-conductor confirmada (RF4). */
public class Asignacion {

    private int id;
    private final SolicitudEnvio solicitud;
    private final Vehiculo vehiculo;
    private final Conductor conductor;
    private final LocalDateTime fechaHora;
    private final double horasEstimadas;
    private EstadoAsignacion estado;

    public Asignacion(int id, SolicitudEnvio solicitud, Vehiculo vehiculo,
                      Conductor conductor, LocalDateTime fechaHora,
                      double horasEstimadas, EstadoAsignacion estado) {
        this.id = id;
        this.solicitud = solicitud;
        this.vehiculo = vehiculo;
        this.conductor = conductor;
        this.fechaHora = fechaHora;
        this.horasEstimadas = horasEstimadas;
        this.estado = estado;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public SolicitudEnvio getSolicitud() { return solicitud; }
    public Vehiculo getVehiculo() { return vehiculo; }
    public Conductor getConductor() { return conductor; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public double getHorasEstimadas() { return horasEstimadas; }
    public EstadoAsignacion getEstado() { return estado; }
}

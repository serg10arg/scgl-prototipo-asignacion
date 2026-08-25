package com.logisticaandina.scgl.dominio;

import java.time.LocalDateTime;

/** Solicitud de envio de un cliente (RF3). */
public class SolicitudEnvio {

    private int id;
    private String origen;
    private String destino;
    private double pesoKg;
    private LocalDateTime ventanaInicio;
    private LocalDateTime ventanaFin;
    private EstadoSolicitud estado;

    public SolicitudEnvio(int id, String origen, String destino, double pesoKg,
                          LocalDateTime ventanaInicio, LocalDateTime ventanaFin,
                          EstadoSolicitud estado) {
        this.id = id;
        this.origen = origen;
        this.destino = destino;
        this.pesoKg = pesoKg;
        this.ventanaInicio = ventanaInicio;
        this.ventanaFin = ventanaFin;
        this.estado = estado;
    }

    public int getId() { return id; }
    public String getOrigen() { return origen; }
    public String getDestino() { return destino; }
    public double getPesoKg() { return pesoKg; }
    public LocalDateTime getVentanaInicio() { return ventanaInicio; }
    public LocalDateTime getVentanaFin() { return ventanaFin; }
    public EstadoSolicitud getEstado() { return estado; }
    public void setEstado(EstadoSolicitud estado) { this.estado = estado; }
}

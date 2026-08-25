package com.logisticaandina.scgl.servicio;

import com.logisticaandina.scgl.dominio.*;
import com.logisticaandina.scgl.excepciones.*;
import com.logisticaandina.scgl.persistencia.AsignacionDAO;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Nucleo del prototipo (CU-02 "Asignar viaje"). Ejecuta las validaciones
 * de las reglas de negocio (RF4/RF5/RF6) y, solo si todas se cumplen,
 * persiste la asignacion en una transaccion atomica (RNF1).
 *
 * Si alguna validacion falla se lanza una AsignacionInvalidaException y
 * NO se persiste nada (flujo alternativo del CU-02, elimina el Problema 1).
 */
public class MotorAsignacion {

    private final AsignacionDAO asignacionDAO;

    public MotorAsignacion(AsignacionDAO asignacionDAO) {
        this.asignacionDAO = asignacionDAO;
    }

    public Asignacion procesarAsignacion(SolicitudEnvio solicitud, Vehiculo vehiculo,
                                         Conductor conductor, double horasEstimadas,
                                         double limiteLegalHoras, LocalDate hoy)
            throws AsignacionInvalidaException, PersistenciaException {

        validar(solicitud, vehiculo, conductor, horasEstimadas, limiteLegalHoras, hoy);

        Asignacion asignacion = new Asignacion(
                0, solicitud, vehiculo, conductor,
                LocalDateTime.now(), horasEstimadas, EstadoAsignacion.CONFIRMADA);

        asignacionDAO.guardarEnTransaccion(asignacion);   // RNF1
        return asignacion;
    }

    /** Aplica RN1, RN4, RN5, RN3 y RN2 en ese orden. */
    private void validar(SolicitudEnvio solicitud, Vehiculo vehiculo, Conductor conductor,
                         double horasEstimadas, double limiteLegalHoras, LocalDate hoy)
            throws AsignacionInvalidaException {

        // RF6 / RN1: estado operativo
        if (!vehiculo.estaOperativo()) {
            throw new VehiculoNoDisponibleException(
                "Vehiculo " + vehiculo.getPatente() + " no operativo (estado: "
                + vehiculo.getEstado() + ").");
        }
        // RF6 / RN4: mantenimiento preventivo pendiente
        if (vehiculo.requiereMantenimiento()) {
            throw new VehiculoNoDisponibleException(
                "Vehiculo " + vehiculo.getPatente()
                + " requiere mantenimiento preventivo (umbral de km superado).");
        }
        // RN5: capacidad
        if (!vehiculo.puedeTransportar(solicitud.getPesoKg())) {
            throw new CapacidadInsuficienteException(
                "Vehiculo " + vehiculo.getPatente() + " sin capacidad para "
                + solicitud.getPesoKg() + " kg.");
        }
        // RF5 / RN3: licencia vigente
        if (!conductor.tieneLicenciaVigente(hoy)) {
            throw new ConductorNoDisponibleException(
                "Conductor " + conductor.getNombre() + " con licencia vencida.");
        }
        // RF5 / RN2: horas legales de conduccion
        if (!conductor.tieneHorasDisponibles(horasEstimadas, limiteLegalHoras)) {
            throw new ConductorNoDisponibleException(
                "Conductor " + conductor.getNombre() + " excede el limite legal de horas ("
                + conductor.getHorasAcumuladas() + " + " + horasEstimadas + " > "
                + limiteLegalHoras + ").");
        }
    }
}

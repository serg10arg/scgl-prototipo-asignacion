package com.logisticaandina.scgl;

import com.logisticaandina.scgl.dominio.*;
import com.logisticaandina.scgl.excepciones.*;
import com.logisticaandina.scgl.persistencia.*;
import com.logisticaandina.scgl.servicio.MotorAsignacion;

import java.time.LocalDate;

/**
 * Demostracion operacional del modulo de asignacion.
 * Ejercita tres escenarios contra la base de datos MySQL (db/schema.sql):
 *   A) Asignacion valida  -> se persiste (transaccion OK).
 *   B) Rechazo por vehiculo (mantenimiento pendiente) -> no persiste.
 *   C) Rechazo por conductor (excede horas legales)   -> no persiste.
 *
 * [SUPUESTO] Limite legal de conduccion parametrizado (RNF4).
 */
public class App {

    // Limite de jornada semanal para transporte de cargas: 44 h
    // (CCT de Transporte de Cargas, art. 30.1; marco Ley 11.544). Parametrizable (RNF4).
    private static final double LIMITE_LEGAL_HORAS = 44.0;

    public static void main(String[] args) {
        VehiculoDAO vehiculoDAO = new VehiculoDAO();
        ConductorDAO conductorDAO = new ConductorDAO();
        SolicitudDAO solicitudDAO = new SolicitudDAO();
        MotorAsignacion motor = new MotorAsignacion(new AsignacionDAO());
        LocalDate hoy = LocalDate.now();

        System.out.println("=== SCGL - Prototipo del modulo de asignacion ===\n");

        // Escenario A: asignacion valida (solicitud 1, vehiculo 1, conductor 1)
        intentar(motor, solicitudDAO, vehiculoDAO, conductorDAO,
                 1, 1, 1, 6.0, hoy, "A) Asignacion valida");

        // Escenario B: vehiculo 2 requiere mantenimiento (RN4) -> rechazo
        intentar(motor, solicitudDAO, vehiculoDAO, conductorDAO,
                 2, 2, 1, 6.0, hoy, "B) Rechazo por vehiculo");

        // Escenario C: conductor 2 (40 h) + 6 h > 44 h (RN2) -> rechazo
        intentar(motor, solicitudDAO, vehiculoDAO, conductorDAO,
                 2, 1, 2, 6.0, hoy, "C) Rechazo por conductor");
    }

    private static void intentar(MotorAsignacion motor, SolicitudDAO sDao, VehiculoDAO vDao,
                                 ConductorDAO cDao, int idSol, int idVeh, int idCond,
                                 double horas, LocalDate hoy, String titulo) {
        System.out.println("--- " + titulo + " ---");
        try {
            SolicitudEnvio s = sDao.buscarPorId(idSol);
            Vehiculo v = vDao.buscarPorId(idVeh);
            Conductor c = cDao.buscarPorId(idCond);

            Asignacion a = motor.procesarAsignacion(s, v, c, horas, LIMITE_LEGAL_HORAS, hoy);
            System.out.println("  OK -> asignacion #" + a.getId()
                + " persistida (vehiculo " + v.getPatente()
                + ", conductor " + c.getNombre() + ").\n");
        } catch (AsignacionInvalidaException e) {
            System.out.println("  RECHAZADA (no se persiste): " + e.getMessage() + "\n");
        } catch (PersistenciaException e) {
            System.out.println("  ERROR de persistencia: " + e.getMessage() + "\n");
        }
    }
}

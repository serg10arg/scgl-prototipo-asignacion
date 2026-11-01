package com.logisticaandina.scgl.mantenimiento;

import com.logisticaandina.scgl.dominio.OrdenMantenimiento;
import com.logisticaandina.scgl.excepciones.MantenimientoInvalidoException;
import com.logisticaandina.scgl.excepciones.PersistenciaException;

import java.io.IOException;

/**
 * CONTROLADOR del patron MVC (modulo de mantenimiento). Recibe la opcion elegida en
 * la Vista, invoca la operacion del Modelo y devuelve el resultado a la Vista.
 * Concentra el tratamiento de excepciones: rechazos de negocio, errores de MySQL y de
 * archivo se informan sin interrumpir el programa.
 */
public class ControladorMantenimiento {

    private final ModeloMantenimiento modelo;
    private final VistaMantenimiento vista;

    public ControladorMantenimiento(ModeloMantenimiento modelo, VistaMantenimiento vista) {
        this.modelo = modelo;
        this.vista = vista;
    }

    /** Bucle del submenu; vuelve al menu principal con la opcion 0. */
    public void ejecutar() {
        boolean volver = false;
        while (!volver) {
            vista.mostrarMenu();
            int opcion = vista.solicitarOpcion();
            try {
                switch (opcion) {
                    case 1 -> vista.mostrarOrdenes("Ordenes de mantenimiento", modelo.listarOrdenes());
                    case 2 -> vista.mostrarOrdenes("Ordenes abiertas", modelo.listarOrdenesAbiertas());
                    case 3 -> abrirOrden();
                    case 4 -> modificarTipo();
                    case 5 -> cerrarOrden();
                    case 6 -> vista.mostrarResumen(modelo.resumenFlota());
                    case 7 -> exportar();
                    case 8 -> vista.mostrarRegistrosArchivo(modelo.getRutaArchivo(),
                            modelo.getEncabezadoArchivo(), modelo.recuperarOrdenesExportadas());
                    case 0 -> volver = true;
                    default -> vista.mostrarError("Opcion invalida.");
                }
            } catch (MantenimientoInvalidoException e) {
                vista.mostrarError("RECHAZADA (no se persiste): " + e.getMessage());
            } catch (PersistenciaException e) {
                vista.mostrarError("de persistencia: " + e.getDetalle());
            } catch (IOException e) {
                vista.mostrarError("de archivo: " + e.getMessage());
            }
        }
    }

    private void abrirOrden() throws MantenimientoInvalidoException, PersistenciaException {
        vista.mostrarVehiculos(modelo.listarVehiculos());
        int idVehiculo = vista.solicitarIdVehiculo();
        OrdenMantenimiento o = modelo.abrirOrden(idVehiculo, vista.solicitarTipo());
        vista.mostrarMensaje("OK -> orden #" + o.getId() + " (" + o.getTipo() + ") abierta; el vehiculo "
                + o.getPatenteVehiculo() + " queda EN_TALLER y fuera de asignacion (RN1).");
    }

    private void modificarTipo() throws MantenimientoInvalidoException, PersistenciaException {
        vista.mostrarOrdenes("Ordenes abiertas", modelo.listarOrdenesAbiertas());
        int idOrden = vista.solicitarIdOrden();
        OrdenMantenimiento o = modelo.modificarTipo(idOrden, vista.solicitarTipo());
        vista.mostrarMensaje("OK -> orden #" + o.getId() + " actualizada a " + o.getTipo() + ".");
    }

    private void cerrarOrden() throws MantenimientoInvalidoException, PersistenciaException {
        vista.mostrarOrdenes("Ordenes abiertas", modelo.listarOrdenesAbiertas());
        int idOrden = vista.solicitarIdOrden();
        OrdenMantenimiento o = modelo.cerrarOrden(idOrden);
        vista.mostrarMensaje("OK -> orden #" + o.getId() + " cerrada; el vehiculo " + o.getPatenteVehiculo()
                + " vuelve a OPERATIVO" + (o.actualizaKmDeReferencia()
                ? " y reinicia el km de referencia del mantenimiento (RN4)." : "."));
    }

    private void exportar() throws PersistenciaException, IOException {
        int filas = modelo.exportarOrdenes();
        vista.mostrarMensaje("OK -> " + filas + " ordenes exportadas a " + modelo.getRutaArchivo() + ".");
    }
}

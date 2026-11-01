package com.logisticaandina.scgl.mantenimiento;

import com.logisticaandina.scgl.dominio.EstadoVehiculo;
import com.logisticaandina.scgl.dominio.OrdenMantenimiento;
import com.logisticaandina.scgl.dominio.TipoOrden;
import com.logisticaandina.scgl.dominio.Vehiculo;
import com.logisticaandina.scgl.excepciones.MantenimientoInvalidoException;
import com.logisticaandina.scgl.excepciones.PersistenciaException;
import com.logisticaandina.scgl.persistencia.ArchivoOrdenes;
import com.logisticaandina.scgl.persistencia.ConsultaDAO;
import com.logisticaandina.scgl.persistencia.OrdenMantenimientoDAO;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * MODELO del patron MVC (modulo de mantenimiento, CU-06 / RF-09).
 * Gestiona los datos y las reglas del modulo: valida cada operacion y la persiste a
 * traves de los DAO (MySQL) o del archivo CSV. No lee ni escribe en la consola.
 */
public class ModeloMantenimiento {

    private final OrdenMantenimientoDAO ordenDAO;
    private final ConsultaDAO<Vehiculo> vehiculoDAO;   // depende de la interfaz, no de la clase JDBC
    private final ArchivoOrdenes archivo;

    public ModeloMantenimiento(OrdenMantenimientoDAO ordenDAO, ConsultaDAO<Vehiculo> vehiculoDAO,
                               ArchivoOrdenes archivo) {
        this.ordenDAO = ordenDAO;
        this.vehiculoDAO = vehiculoDAO;
        this.archivo = archivo;
    }

    // ----------------------------------------------------------------- consulta

    public List<OrdenMantenimiento> listarOrdenes() throws PersistenciaException {
        return ordenDAO.listarTodos();
    }

    public List<OrdenMantenimiento> listarOrdenesAbiertas() throws PersistenciaException {
        return ordenDAO.listarAbiertas();
    }

    public List<Vehiculo> listarVehiculos() throws PersistenciaException {
        return vehiculoDAO.listarTodos();
    }

    // --------------------------------------------------------------------- alta

    /** Alta (CU-06): abre una orden y deja la unidad EN_TALLER. Devuelve la orden creada. */
    public OrdenMantenimiento abrirOrden(int idVehiculo, TipoOrden tipo)
            throws MantenimientoInvalidoException, PersistenciaException {
        Vehiculo v = vehiculoDAO.buscarPorId(idVehiculo);
        if (v == null) {
            throw new MantenimientoInvalidoException("El vehiculo id=" + idVehiculo + " no existe.");
        }
        for (OrdenMantenimiento o : ordenDAO.listarAbiertas()) {
            if (o.getIdVehiculo() == idVehiculo) {
                throw new MantenimientoInvalidoException("El vehiculo " + v.getPatente()
                        + " ya tiene la orden #" + o.getId() + " abierta.");
            }
        }
        OrdenMantenimiento orden = new OrdenMantenimiento(idVehiculo, v.getPatente(), tipo, LocalDate.now());
        ordenDAO.insertar(orden);
        return orden;
    }

    // ------------------------------------------------------------- modificacion

    /** Modificacion: corrige el tipo de una orden abierta. */
    public OrdenMantenimiento modificarTipo(int idOrden, TipoOrden nuevoTipo)
            throws MantenimientoInvalidoException, PersistenciaException {
        OrdenMantenimiento orden = buscarAbierta(idOrden);
        orden.cambiarTipo(nuevoTipo);
        ordenDAO.actualizar(orden);
        return orden;
    }

    // ------------------------------------------------------------- baja logica

    /** Baja logica (CU-06, E1): cierra la orden y la unidad vuelve a OPERATIVO. */
    public OrdenMantenimiento cerrarOrden(int idOrden)
            throws MantenimientoInvalidoException, PersistenciaException {
        OrdenMantenimiento orden = buscarAbierta(idOrden);
        orden.cerrar();
        ordenDAO.cerrarEnTransaccion(orden);
        return orden;
    }

    private OrdenMantenimiento buscarAbierta(int idOrden)
            throws MantenimientoInvalidoException, PersistenciaException {
        OrdenMantenimiento orden = ordenDAO.buscarPorId(idOrden);
        if (orden == null) {
            throw new MantenimientoInvalidoException("La orden #" + idOrden + " no existe.");
        }
        if (!orden.estaAbierta()) {
            throw new MantenimientoInvalidoException("La orden #" + idOrden + " ya esta cerrada.");
        }
        return orden;
    }

    // ------------------------------------------------- resumen (arreglo + ArrayList)

    /**
     * Resumen de la flota por estado. Los vehiculos llegan en un ArrayList (cantidad
     * variable) y se acumulan en un arreglo de tamano fijo, una posicion por cada valor
     * de EstadoVehiculo (indice = ordinal). La ultima posicion cuenta las unidades que
     * superaron el umbral de km (RN4).
     */
    public int[] resumenFlota() throws PersistenciaException {
        EstadoVehiculo[] estados = EstadoVehiculo.values();
        int[] conteo = new int[estados.length + 1];
        for (Vehiculo v : vehiculoDAO.listarTodos()) {
            conteo[v.getEstado().ordinal()]++;
            if (v.requiereMantenimiento()) {
                conteo[estados.length]++;
            }
        }
        return conteo;
    }

    // ------------------------------------------------------------------ archivo

    public int exportarOrdenes() throws PersistenciaException, IOException {
        return archivo.exportar(ordenDAO.listarTodos());
    }

    public List<String[]> recuperarOrdenesExportadas() throws IOException {
        return archivo.recuperar();
    }

    public String[] getEncabezadoArchivo() {
        return archivo.getEncabezado();
    }

    public String getRutaArchivo() {
        return archivo.getRuta();
    }
}

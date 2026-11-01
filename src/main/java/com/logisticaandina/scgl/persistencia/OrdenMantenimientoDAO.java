package com.logisticaandina.scgl.persistencia;

import com.logisticaandina.scgl.dominio.EstadoOrden;
import com.logisticaandina.scgl.dominio.OrdenMantenimiento;
import com.logisticaandina.scgl.dominio.TipoOrden;
import com.logisticaandina.scgl.excepciones.PersistenciaException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de OrdenMantenimiento (RF-09 / CU-06). Implementa el CRUD del TP4:
 * consulta (buscarPorId, listarTodos, listarAbiertas), alta (insertar), modificacion
 * (actualizar) y baja logica (cerrarEnTransaccion). Alta y cierre tocan dos tablas
 * (orden y vehiculo), por eso se ejecutan como transaccion atomica (RNF-01).
 */
public class OrdenMantenimientoDAO implements CrudDAO<OrdenMantenimiento> {

    private static final String SELECT_BASE =
            "SELECT o.id_orden, o.id_vehiculo, v.patente, o.tipo, o.estado, o.fecha "
          + "FROM orden_mantenimiento o JOIN vehiculo v ON v.id_vehiculo = o.id_vehiculo";

    // ----------------------------------------------------------------- consulta

    @Override
    public OrdenMantenimiento buscarPorId(int id) throws PersistenciaException {
        String sql = SELECT_BASE + " WHERE o.id_orden = ?";
        try (Connection c = ConexionMySQL.obtener();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar la orden de mantenimiento id=" + id, e);
        }
    }

    @Override
    public List<OrdenMantenimiento> listarTodos() throws PersistenciaException {
        return listar(SELECT_BASE + " ORDER BY o.id_orden", "Error al listar ordenes de mantenimiento");
    }

    /** Ordenes ABIERTAS: las que hoy mantienen una unidad fuera de servicio (RN1). */
    public List<OrdenMantenimiento> listarAbiertas() throws PersistenciaException {
        return listar(SELECT_BASE + " WHERE o.estado = 'ABIERTA' ORDER BY o.id_orden",
                "Error al listar ordenes de mantenimiento abiertas");
    }

    private List<OrdenMantenimiento> listar(String sql, String mensajeError) throws PersistenciaException {
        List<OrdenMantenimiento> lista = new ArrayList<>();
        try (Connection c = ConexionMySQL.obtener();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new PersistenciaException(mensajeError, e);
        }
    }

    // --------------------------------------------------------------------- alta

    /**
     * Alta (CU-06, paso 5): crea la orden ABIERTA y pasa el vehiculo a EN_TALLER,
     * dentro de una misma transaccion. Devuelve el id generado.
     */
    @Override
    public int insertar(OrdenMantenimiento o) throws PersistenciaException {
        String insOrden = "INSERT INTO orden_mantenimiento (id_vehiculo, tipo, estado, fecha) "
                        + "VALUES (?, ?, ?, ?)";
        String updVeh = "UPDATE vehiculo SET estado = 'EN_TALLER' WHERE id_vehiculo = ?";

        Connection c = null;
        try {
            c = ConexionMySQL.obtener();
            c.setAutoCommit(false);                     // inicio de la transaccion (RNF-01)

            try (PreparedStatement ps = c.prepareStatement(insOrden, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, o.getIdVehiculo());
                ps.setString(2, o.getTipo().name());
                ps.setString(3, o.getEstado().name());
                ps.setObject(4, o.getFecha());          // LocalDate -> DATE, sin conversion de zona
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) o.setId(keys.getInt(1));
                }
            }
            try (PreparedStatement ps = c.prepareStatement(updVeh)) {
                ps.setInt(1, o.getIdVehiculo());
                ps.executeUpdate();
            }

            c.commit();
            return o.getId();
        } catch (SQLException e) {
            deshacer(c);
            throw new PersistenciaException(
                "Error al registrar la orden de mantenimiento; se revirtio la transaccion (rollback)", e);
        } finally {
            cerrar(c);
        }
    }

    // ------------------------------------------------------------- modificacion

    /** Modificacion: actualiza el tipo de una orden que sigue ABIERTA. */
    @Override
    public void actualizar(OrdenMantenimiento o) throws PersistenciaException {
        String sql = "UPDATE orden_mantenimiento SET tipo = ? WHERE id_orden = ? AND estado = 'ABIERTA'";
        try (Connection c = ConexionMySQL.obtener();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, o.getTipo().name());
            ps.setInt(2, o.getId());
            if (ps.executeUpdate() == 0) {
                throw new PersistenciaException("La orden #" + o.getId() + " no existe o ya no esta abierta.");
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al actualizar la orden de mantenimiento id=" + o.getId(), e);
        }
    }

    // ------------------------------------------------------------- baja logica

    /**
     * Baja logica (CU-06, E1): la orden pasa a CERRADA y el vehiculo vuelve a OPERATIVO.
     * Si la orden es preventiva, ademas reinicia el km de referencia del ultimo
     * mantenimiento al km actual (RN4). Todo en una transaccion.
     */
    public void cerrarEnTransaccion(OrdenMantenimiento o) throws PersistenciaException {
        String updOrden = "UPDATE orden_mantenimiento SET estado = 'CERRADA' WHERE id_orden = ?";
        String updVeh = o.actualizaKmDeReferencia()
                ? "UPDATE vehiculo SET estado = 'OPERATIVO', km_ultimo_mantenimiento = km_actual "
                  + "WHERE id_vehiculo = ?"
                : "UPDATE vehiculo SET estado = 'OPERATIVO' WHERE id_vehiculo = ?";

        Connection c = null;
        try {
            c = ConexionMySQL.obtener();
            c.setAutoCommit(false);

            try (PreparedStatement ps = c.prepareStatement(updOrden)) {
                ps.setInt(1, o.getId());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(updVeh)) {
                ps.setInt(1, o.getIdVehiculo());
                ps.executeUpdate();
            }

            c.commit();
        } catch (SQLException e) {
            deshacer(c);
            throw new PersistenciaException(
                "Error al cerrar la orden de mantenimiento; se revirtio la transaccion (rollback)", e);
        } finally {
            cerrar(c);
        }
    }

    // ---------------------------------------------------------------- soporte

    private OrdenMantenimiento mapear(ResultSet rs) throws SQLException {
        return new OrdenMantenimiento(
                rs.getInt("id_orden"),
                rs.getInt("id_vehiculo"),
                rs.getString("patente"),
                TipoOrden.valueOf(rs.getString("tipo")),
                EstadoOrden.valueOf(rs.getString("estado")),
                rs.getObject("fecha", LocalDate.class));
    }

    private void deshacer(Connection c) {
        if (c != null) {
            try { c.rollback(); } catch (SQLException ex) { /* la excepcion original es la relevante */ }
        }
    }

    private void cerrar(Connection c) {
        if (c != null) {
            try { c.setAutoCommit(true); c.close(); } catch (SQLException ex) { /* sin accion */ }
        }
    }
}

package com.logisticaandina.scgl.persistencia;

import com.logisticaandina.scgl.dominio.Asignacion;
import com.logisticaandina.scgl.excepciones.PersistenciaException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Persiste la asignacion como una unica transaccion atomica (RNF1). */
public class AsignacionDAO {

    public void guardarEnTransaccion(Asignacion a) throws PersistenciaException {
        String insAsig =
            "INSERT INTO asignacion (id_solicitud, id_vehiculo, id_conductor, " +
            "fecha_hora, horas_estimadas, estado) VALUES (?, ?, ?, ?, ?, ?)";
        String updSol  = "UPDATE solicitud_envio SET estado = 'ASIGNADA' WHERE id_solicitud = ?";
        String updCond = "UPDATE conductor SET horas_acumuladas = horas_acumuladas + ? " +
                         "WHERE id_conductor = ?";

        Connection c = null;
        try {
            c = ConexionMySQL.obtener();
            c.setAutoCommit(false);                 // inicio de la transaccion (RNF1)

            try (PreparedStatement ps = c.prepareStatement(insAsig, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, a.getSolicitud().getId());
                ps.setInt(2, a.getVehiculo().getId());
                ps.setInt(3, a.getConductor().getId());
                ps.setObject(4, a.getFechaHora());      // LocalDateTime -> DATETIME, sin conversion de zona
                ps.setDouble(5, a.getHorasEstimadas());
                ps.setString(6, a.getEstado().name());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) a.setId(keys.getInt(1));
                }
            }
            try (PreparedStatement ps = c.prepareStatement(updSol)) {
                ps.setInt(1, a.getSolicitud().getId());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(updCond)) {
                ps.setDouble(1, a.getHorasEstimadas());
                ps.setInt(2, a.getConductor().getId());
                ps.executeUpdate();
            }

            c.commit();                             // confirmacion atomica
        } catch (SQLException e) {
            if (c != null) {
                try { c.rollback(); } catch (SQLException ex) { /* log */ }
            }
            throw new PersistenciaException(
                "Error al persistir la asignacion; se revirtio la transaccion (rollback)", e);
        } finally {
            if (c != null) {
                try { c.setAutoCommit(true); c.close(); } catch (SQLException ex) { /* log */ }
            }
        }
    }
}

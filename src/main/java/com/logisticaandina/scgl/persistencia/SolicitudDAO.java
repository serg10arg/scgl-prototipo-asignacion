package com.logisticaandina.scgl.persistencia;

import com.logisticaandina.scgl.dominio.EstadoSolicitud;
import com.logisticaandina.scgl.dominio.SolicitudEnvio;
import com.logisticaandina.scgl.excepciones.PersistenciaException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Acceso a datos de SolicitudEnvio. */
public class SolicitudDAO {

    public SolicitudEnvio buscarPorId(int id) throws PersistenciaException {
        String sql = "SELECT * FROM solicitud_envio WHERE id_solicitud = ?";
        try (Connection c = ConexionMySQL.obtener();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar solicitud id=" + id, e);
        }
    }

    /** Lista las solicitudes en estado PENDIENTE (solo lectura; menu interactivo del TP3). */
    public List<SolicitudEnvio> listarPendientes() throws PersistenciaException {
        String sql = "SELECT * FROM solicitud_envio WHERE estado = 'PENDIENTE' ORDER BY id_solicitud";
        List<SolicitudEnvio> lista = new ArrayList<>();
        try (Connection c = ConexionMySQL.obtener();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar solicitudes pendientes", e);
        }
    }

    private SolicitudEnvio mapear(ResultSet rs) throws SQLException {
        return new SolicitudEnvio(
                rs.getInt("id_solicitud"),
                rs.getString("origen"),
                rs.getString("destino"),
                rs.getDouble("peso_kg"),
                rs.getTimestamp("ventana_inicio").toLocalDateTime(),
                rs.getTimestamp("ventana_fin").toLocalDateTime(),
                EstadoSolicitud.valueOf(rs.getString("estado")));
    }
}

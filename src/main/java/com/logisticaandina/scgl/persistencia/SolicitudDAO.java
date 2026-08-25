package com.logisticaandina.scgl.persistencia;

import com.logisticaandina.scgl.dominio.EstadoSolicitud;
import com.logisticaandina.scgl.dominio.SolicitudEnvio;
import com.logisticaandina.scgl.excepciones.PersistenciaException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Acceso a datos de SolicitudEnvio. */
public class SolicitudDAO {

    public SolicitudEnvio buscarPorId(int id) throws PersistenciaException {
        String sql = "SELECT * FROM solicitud_envio WHERE id_solicitud = ?";
        try (Connection c = ConexionMySQL.obtener();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new SolicitudEnvio(
                        rs.getInt("id_solicitud"),
                        rs.getString("origen"),
                        rs.getString("destino"),
                        rs.getDouble("peso_kg"),
                        rs.getTimestamp("ventana_inicio").toLocalDateTime(),
                        rs.getTimestamp("ventana_fin").toLocalDateTime(),
                        EstadoSolicitud.valueOf(rs.getString("estado")));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar solicitud id=" + id, e);
        }
    }
}

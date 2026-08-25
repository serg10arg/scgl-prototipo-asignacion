package com.logisticaandina.scgl.persistencia;

import com.logisticaandina.scgl.dominio.Conductor;
import com.logisticaandina.scgl.dominio.Licencia;
import com.logisticaandina.scgl.excepciones.PersistenciaException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Acceso a datos de Conductor. */
public class ConductorDAO {

    public Conductor buscarPorId(int id) throws PersistenciaException {
        String sql = "SELECT * FROM conductor WHERE id_conductor = ?";
        try (Connection c = ConexionMySQL.obtener();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Licencia lic = new Licencia(
                        rs.getString("num_licencia"),
                        rs.getString("tipo_licencia"),
                        rs.getDate("fecha_venc_licencia").toLocalDate());
                return new Conductor(
                        rs.getInt("id_conductor"),
                        rs.getString("nombre"),
                        lic,
                        rs.getDouble("horas_acumuladas"));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar conductor id=" + id, e);
        }
    }
}

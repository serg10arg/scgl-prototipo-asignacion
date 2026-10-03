package com.logisticaandina.scgl.persistencia;

import com.logisticaandina.scgl.dominio.Conductor;
import com.logisticaandina.scgl.dominio.Licencia;
import com.logisticaandina.scgl.excepciones.PersistenciaException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Acceso a datos de Conductor. */
public class ConductorDAO {

    public Conductor buscarPorId(int id) throws PersistenciaException {
        String sql = "SELECT * FROM conductor WHERE id_conductor = ?";
        try (Connection c = ConexionMySQL.obtener();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar conductor id=" + id, e);
        }
    }

    /** Lista todos los conductores (solo lectura; menu interactivo del TP3). */
    public List<Conductor> listarTodos() throws PersistenciaException {
        String sql = "SELECT * FROM conductor ORDER BY id_conductor";
        List<Conductor> lista = new ArrayList<>();
        try (Connection c = ConexionMySQL.obtener();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar conductores", e);
        }
    }

    private Conductor mapear(ResultSet rs) throws SQLException {
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
}

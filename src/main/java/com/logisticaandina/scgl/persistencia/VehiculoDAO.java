package com.logisticaandina.scgl.persistencia;

import com.logisticaandina.scgl.dominio.*;
import com.logisticaandina.scgl.excepciones.PersistenciaException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Acceso a datos de Vehiculo. Reconstruye la subclass segun el discriminador 'tipo'. */
public class VehiculoDAO {

    public Vehiculo buscarPorId(int id) throws PersistenciaException {
        String sql = "SELECT * FROM vehiculo WHERE id_vehiculo = ?";
        try (Connection c = ConexionMySQL.obtener();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar vehiculo id=" + id, e);
        }
    }

    /** Lista todos los vehiculos (solo lectura; alimenta el menu interactivo del TP3). */
    public List<Vehiculo> listarTodos() throws PersistenciaException {
        String sql = "SELECT * FROM vehiculo ORDER BY id_vehiculo";
        List<Vehiculo> lista = new ArrayList<>();
        try (Connection c = ConexionMySQL.obtener();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar vehiculos", e);
        }
    }

    /** Mapeo tabla-objeto: el ENUM 'tipo' selecciona la subclass concreta. */
    private Vehiculo mapear(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_vehiculo");
        String patente = rs.getString("patente");
        TipoVehiculo tipo = TipoVehiculo.valueOf(rs.getString("tipo"));
        double capacidad = rs.getDouble("capacidad_kg");
        int kmActual = rs.getInt("km_actual");
        int kmUlt = rs.getInt("km_ultimo_mantenimiento");
        int umbral = rs.getInt("umbral_mantenimiento_km");
        EstadoVehiculo estado = EstadoVehiculo.valueOf(rs.getString("estado"));

        return (tipo == TipoVehiculo.PESADO)
            ? new VehiculoPesado(id, patente, capacidad, kmActual, kmUlt, umbral, estado)
            : new VehiculoLigero(id, patente, capacidad, kmActual, kmUlt, umbral, estado);
    }
}

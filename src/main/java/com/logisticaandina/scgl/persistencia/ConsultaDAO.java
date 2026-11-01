package com.logisticaandina.scgl.persistencia;

import com.logisticaandina.scgl.excepciones.PersistenciaException;

import java.util.List;

/**
 * Contrato comun de lectura de los DAO (TP4). Lo implementan VehiculoDAO, ConductorDAO
 * y OrdenMantenimientoDAO: quien consume la capa de persistencia depende de la interfaz,
 * no del detalle JDBC de cada clase.
 *
 * @param <T> entidad del dominio que maneja el DAO
 */
public interface ConsultaDAO<T> {

    /** Devuelve la entidad con ese id, o null si no existe. */
    T buscarPorId(int id) throws PersistenciaException;

    /** Devuelve todas las entidades de la tabla. */
    List<T> listarTodos() throws PersistenciaException;
}

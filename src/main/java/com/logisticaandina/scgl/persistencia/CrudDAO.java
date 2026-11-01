package com.logisticaandina.scgl.persistencia;

import com.logisticaandina.scgl.excepciones.PersistenciaException;

/**
 * Contrato de escritura (alta y modificacion) que se suma al de lectura.
 * No declara un metodo de borrado fisico: en el SCGL las entidades con historial
 * se dan de baja de forma logica (cambio de estado), coherente con las FK RESTRICT
 * y con la trazabilidad del historial (RF-10).
 *
 * @param <T> entidad del dominio que maneja el DAO
 */
public interface CrudDAO<T> extends ConsultaDAO<T> {

    /** Alta: persiste la entidad y devuelve el id generado. */
    int insertar(T entidad) throws PersistenciaException;

    /** Modificacion: actualiza los datos editables de la entidad. */
    void actualizar(T entidad) throws PersistenciaException;
}

package com.logisticaandina.scgl.util;

import com.logisticaandina.scgl.dominio.Conductor;
import com.logisticaandina.scgl.dominio.Vehiculo;

import java.util.ArrayList;
import java.util.List;

/**
 * Algoritmos de ordenacion y busqueda implementados a mano (sin Collections.sort
 * ni Streams), para evidenciar la logica de control sobre colecciones (TP3, caracteristica
 * opcional; base del Taller de Algoritmos y Estructuras de Datos I).
 *   - Ordenacion: seleccion (por capacidad) e insercion (por patente).
 *   - Busqueda: binaria (por patente, sobre lista ordenada) y lineal (por nombre).
 */
public final class Algoritmos {

    private Algoritmos() { }   // clase de utilidades: no se instancia

    /** Ordenamiento por SELECCION: capacidad de carga descendente. O(n^2). */
    public static void ordenarPorCapacidadDesc(List<Vehiculo> v) {
        for (int i = 0; i < v.size() - 1; i++) {
            int idxMax = i;
            for (int j = i + 1; j < v.size(); j++) {
                if (v.get(j).getCapacidadKg() > v.get(idxMax).getCapacidadKg()) {
                    idxMax = j;
                }
            }
            if (idxMax != i) {               // intercambio (swap)
                Vehiculo tmp = v.get(i);
                v.set(i, v.get(idxMax));
                v.set(idxMax, tmp);
            }
        }
    }

    /** Ordenamiento por INSERCION: patente ascendente (precondicion de la busqueda binaria). */
    public static void ordenarPorPatenteAsc(List<Vehiculo> v) {
        for (int i = 1; i < v.size(); i++) {
            Vehiculo actual = v.get(i);
            int j = i - 1;
            while (j >= 0 && v.get(j).getPatente().compareToIgnoreCase(actual.getPatente()) > 0) {
                v.set(j + 1, v.get(j));      // desplaza a la derecha
                j--;
            }
            v.set(j + 1, actual);
        }
    }

    /**
     * Busqueda BINARIA por patente sobre una lista ya ordenada en forma ascendente.
     * Devuelve el indice del elemento o -1 si no existe. O(log n).
     */
    public static int busquedaBinariaPorPatente(List<Vehiculo> v, String patente) {
        int bajo = 0;
        int alto = v.size() - 1;
        while (bajo <= alto) {
            int medio = (bajo + alto) / 2;
            int cmp = v.get(medio).getPatente().compareToIgnoreCase(patente);
            if (cmp == 0) {
                return medio;
            } else if (cmp < 0) {
                bajo = medio + 1;
            } else {
                alto = medio - 1;
            }
        }
        return -1;
    }

    /** Busqueda LINEAL por coincidencia parcial de nombre (case-insensitive). O(n). */
    public static List<Conductor> busquedaLinealPorNombre(List<Conductor> c, String texto) {
        List<Conductor> resultado = new ArrayList<>();
        String clave = texto.toLowerCase();
        for (Conductor cond : c) {
            if (cond.getNombre().toLowerCase().contains(clave)) {
                resultado.add(cond);
            }
        }
        return resultado;
    }
}

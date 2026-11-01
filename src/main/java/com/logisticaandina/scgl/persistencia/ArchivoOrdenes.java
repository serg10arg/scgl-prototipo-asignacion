package com.logisticaandina.scgl.persistencia;

import com.logisticaandina.scgl.dominio.OrdenMantenimiento;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistencia complementaria en archivo de texto (TP4): exporta las ordenes de
 * mantenimiento a un CSV y lo recupera. Sirve como respaldo o para compartir el
 * listado fuera del sistema; la fuente de verdad sigue siendo MySQL.
 * Usa las clases de java.io (File, FileOutputStream, PrintStream, FileInputStream).
 */
public class ArchivoOrdenes {

    /** Separador ';' para que el CSV se abra en columnas en una planilla en espanol. */
    private static final String SEPARADOR = ";";

    /** Arreglo fijo con el encabezado: la cantidad de columnas no varia. */
    private static final String[] ENCABEZADO = {"id_orden", "patente", "tipo", "estado", "fecha"};

    private final File archivo;

    public ArchivoOrdenes() {
        this(new File("reportes", "ordenes_mantenimiento.csv"));
    }

    public ArchivoOrdenes(File archivo) {
        this.archivo = archivo;
    }

    /** Guarda las ordenes en el CSV (lo reemplaza si ya existe). Devuelve las filas escritas. */
    public int exportar(List<OrdenMantenimiento> ordenes) throws IOException {
        File carpeta = archivo.getParentFile();
        if (carpeta != null && !carpeta.exists() && !carpeta.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta " + carpeta.getPath());
        }
        try (PrintStream out = new PrintStream(new FileOutputStream(archivo), true, StandardCharsets.UTF_8)) {
            out.println(String.join(SEPARADOR, ENCABEZADO));
            for (OrdenMantenimiento o : ordenes) {
                String[] fila = {
                        String.valueOf(o.getId()), o.getPatenteVehiculo(), o.getTipo().name(),
                        o.getEstado().name(), String.valueOf(o.getFecha())
                };
                out.println(String.join(SEPARADOR, fila));
            }
            if (out.checkError()) {
                throw new IOException("Error de escritura en " + archivo.getPath());
            }
        }
        return ordenes.size();
    }

    /**
     * Recupera las filas del CSV. Cada registro es un arreglo de campos (String[]) y el
     * conjunto, de tamano variable, se acumula en un ArrayList: arreglo y ArrayList se
     * usan de forma complementaria.
     */
    public List<String[]> recuperar() throws IOException {
        if (!archivo.exists()) {
            throw new FileNotFoundException("No existe " + archivo.getPath() + "; exporte primero las ordenes.");
        }
        List<String[]> registros = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream(archivo), StandardCharsets.UTF_8))) {
            String linea = in.readLine();                     // encabezado: se descarta
            int numeroLinea = 1;
            while ((linea = in.readLine()) != null) {
                numeroLinea++;
                if (linea.isBlank()) continue;
                String[] campos = linea.split(SEPARADOR, -1);
                if (campos.length != ENCABEZADO.length) {
                    throw new IOException("Formato invalido en la linea " + numeroLinea + " de " + archivo.getPath());
                }
                registros.add(campos);
            }
        }
        return registros;
    }

    public String[] getEncabezado() {
        return ENCABEZADO.clone();
    }

    public String getRuta() {
        return archivo.getPath();
    }
}

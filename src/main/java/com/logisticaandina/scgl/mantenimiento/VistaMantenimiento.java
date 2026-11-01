package com.logisticaandina.scgl.mantenimiento;

import com.logisticaandina.scgl.dominio.EstadoVehiculo;
import com.logisticaandina.scgl.dominio.OrdenMantenimiento;
import com.logisticaandina.scgl.dominio.TipoOrden;
import com.logisticaandina.scgl.dominio.Vehiculo;

import java.util.List;
import java.util.Scanner;

/**
 * VISTA del patron MVC (modulo de mantenimiento). Solo presenta informacion y
 * solicita datos al usuario por consola; no conoce la base de datos ni las reglas.
 * Recibe el Scanner del menu principal para no abrir un segundo lector de System.in.
 */
public class VistaMantenimiento {

    private final Scanner sc;

    public VistaMantenimiento(Scanner sc) {
        this.sc = sc;
    }

    public void mostrarMenu() {
        System.out.println("\n------- MANTENIMIENTO DE FLOTA [CU-06] -------");
        System.out.println(" 1. Listar todas las ordenes");
        System.out.println(" 2. Listar ordenes abiertas");
        System.out.println(" 3. Abrir orden de mantenimiento (alta)");
        System.out.println(" 4. Modificar tipo de una orden abierta");
        System.out.println(" 5. Cerrar orden (baja logica)");
        System.out.println(" 6. Resumen de la flota por estado");
        System.out.println(" 7. Exportar ordenes a archivo CSV");
        System.out.println(" 8. Recuperar ordenes desde el archivo CSV");
        System.out.println(" 0. Volver al menu principal");
        System.out.println("----------------------------------------------");
    }

    // ------------------------------------------------------------ solicitudes

    public int solicitarOpcion() {
        return leerEntero("Elija una opcion: ");
    }

    public int solicitarIdVehiculo() {
        return leerEntero("Id del vehiculo: ");
    }

    public int solicitarIdOrden() {
        return leerEntero("Id de la orden: ");
    }

    public TipoOrden solicitarTipo() {
        while (true) {
            int t = leerEntero("Tipo (1 = PREVENTIVO, 2 = CORRECTIVO): ");
            if (t == 1) return TipoOrden.PREVENTIVO;
            if (t == 2) return TipoOrden.CORRECTIVO;
            System.out.println("  Opcion de tipo invalida.");
        }
    }

    // ------------------------------------------------------------ presentacion

    public void mostrarOrdenes(String titulo, List<OrdenMantenimiento> ordenes) {
        System.out.println("\n--- " + titulo + " ---");
        if (ordenes.isEmpty()) {
            System.out.println("  (sin ordenes)");
            return;
        }
        System.out.printf("  %-5s %-10s %-11s %-8s %-10s%n", "Id", "Patente", "Tipo", "Estado", "Fecha");
        for (OrdenMantenimiento o : ordenes) {
            System.out.printf("  %-5d %-10s %-11s %-8s %-10s%n",
                    o.getId(), o.getPatenteVehiculo(), o.getTipo(), o.getEstado(), o.getFecha());
        }
    }

    public void mostrarVehiculos(List<Vehiculo> vehiculos) {
        System.out.println("Vehiculos registrados:");
        System.out.printf("  %-4s %-10s %-8s %-14s%n", "Id", "Patente", "Tipo", "Estado");
        for (Vehiculo v : vehiculos) {
            System.out.printf("  %-4d %-10s %-8s %-14s%s%n", v.getId(), v.getPatente(), v.getTipo(),
                    v.getEstado(), v.requiereMantenimiento() ? "  [MANT.PEND]" : "");
        }
    }

    /** Recorre el arreglo del resumen; el indice coincide con el ordinal de EstadoVehiculo. */
    public void mostrarResumen(int[] conteo) {
        System.out.println("\n--- Resumen de la flota por estado ---");
        EstadoVehiculo[] estados = EstadoVehiculo.values();
        int total = 0;
        for (int i = 0; i < estados.length; i++) {
            System.out.printf("  %-16s %3d%n", estados[i], conteo[i]);
            total += conteo[i];
        }
        System.out.printf("  %-16s %3d%n", "TOTAL", total);
        System.out.printf("  %-16s %3d  (umbral de km superado, RN4)%n", "Requieren mant.", conteo[estados.length]);
    }

    public void mostrarRegistrosArchivo(String ruta, String[] encabezado, List<String[]> registros) {
        System.out.println("\n--- Ordenes recuperadas de " + ruta + " (" + registros.size() + ") ---");
        StringBuilder cab = new StringBuilder("  ");
        for (String col : encabezado) cab.append(String.format("%-11s", col));
        System.out.println(cab);
        for (String[] campos : registros) {
            StringBuilder fila = new StringBuilder("  ");
            for (String campo : campos) fila.append(String.format("%-11s", campo));
            System.out.println(fila);
        }
    }

    public void mostrarMensaje(String mensaje) {
        System.out.println("  " + mensaje);
    }

    public void mostrarError(String mensaje) {
        System.out.println("  ERROR: " + mensaje);
    }

    // ------------------------------------------------------- entrada robusta

    private int leerEntero(String prompt) {
        while (true) {
            System.out.print(prompt);
            String linea = sc.nextLine().trim();
            try {
                return Integer.parseInt(linea);
            } catch (NumberFormatException e) {
                System.out.println("  Valor no valido. Ingrese un numero entero.");
            }
        }
    }
}

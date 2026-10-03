package com.logisticaandina.scgl;

import com.logisticaandina.scgl.dominio.*;
import com.logisticaandina.scgl.excepciones.*;
import com.logisticaandina.scgl.persistencia.*;
import com.logisticaandina.scgl.servicio.MotorAsignacion;
import com.logisticaandina.scgl.util.Algoritmos;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Menu de seleccion interactivo del prototipo SCGL (TP3).
 * Reemplaza la demo de 3 escenarios fijos por un programa dirigido por el usuario:
 * registra asignaciones (CU-02) validando las reglas de negocio antes de persistir,
 * y ofrece listados y busquedas de solo lectura sobre el modelo.
 *
 * Concentra la interaccion por consola; la validacion sigue en MotorAsignacion y el
 * acceso a datos en los DAO (separacion de responsabilidades reutilizada de TP1/TP2).
 */
public class MenuConsola {

    // Limite de jornada semanal para transporte de cargas: 44 h
    // (CCT de Transporte de Cargas, art. 30.1; marco Ley 11.544). Parametrizable (RNF4).
    private static final double LIMITE_LEGAL_HORAS = 44.0;

    private final Scanner sc;
    private final VehiculoDAO vehiculoDAO;
    private final ConductorDAO conductorDAO;
    private final SolicitudDAO solicitudDAO;
    private final MotorAsignacion motor;

    public MenuConsola() {
        this.sc = new Scanner(System.in);
        this.vehiculoDAO = new VehiculoDAO();
        this.conductorDAO = new ConductorDAO();
        this.solicitudDAO = new SolicitudDAO();
        this.motor = new MotorAsignacion(new AsignacionDAO());
    }

    /** Bucle principal del menu (estructura repetitiva + seleccion). */
    public void iniciar() {
        System.out.println("=== SCGL - Sistema Centralizado de Gestion Logistica (prototipo) ===");
        boolean salir = false;
        while (!salir) {
            try {
                mostrarMenu();
                int opcion = leerEntero("Elija una opcion: ");
                switch (opcion) {
                    case 1 -> registrarAsignacion();
                    case 2 -> listarSolicitudesPendientes();
                    case 3 -> listarVehiculosOrdenadosPorCapacidad();
                    case 4 -> listarConductores();
                    case 5 -> buscarVehiculoPorPatente();
                    case 6 -> buscarConductorPorNombre();
                    case 0 -> salir = true;
                    default -> System.out.println("  Opcion invalida. Intente nuevamente.");
                }
            } catch (NoSuchElementException e) {
                // Entrada cerrada (EOF: Ctrl+D / Ctrl+Z o fin de un pipe): salir prolijo.
                System.out.println("\n  Entrada finalizada.");
                salir = true;
            }
        }
        System.out.println("\nFin del programa. Hasta luego.");
    }

    private void mostrarMenu() {
        System.out.println("\n---------------- MENU PRINCIPAL ----------------");
        System.out.println(" 1. Registrar asignacion (validar y persistir) [CU-02]");
        System.out.println(" 2. Listar solicitudes pendientes");
        System.out.println(" 3. Listar vehiculos ordenados por capacidad");
        System.out.println(" 4. Listar conductores");
        System.out.println(" 5. Buscar vehiculo por patente");
        System.out.println(" 6. Buscar conductor por nombre");
        System.out.println(" 0. Salir");
        System.out.println("------------------------------------------------");
    }

    // ------------------------------------------------------------------ CU-02

    /** Opcion 1: registra una asignacion validando RN1-RN5 antes de persistir (CU-02). */
    private void registrarAsignacion() {
        System.out.println("\n--- Registrar asignacion (CU-02) ---");
        try {
            List<SolicitudEnvio> pendientes = solicitudDAO.listarPendientes();
            if (pendientes.isEmpty()) {
                System.out.println("  No hay solicitudes pendientes para asignar.");
                return;
            }
            System.out.println("Solicitudes pendientes:");
            imprimirSolicitudes(pendientes);
            int idSol = leerEntero("Id de la solicitud a asignar: ");
            SolicitudEnvio solicitud = solicitudDAO.buscarPorId(idSol);
            if (solicitud == null || solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
                System.out.println("  La solicitud no existe o no esta en estado PENDIENTE.");
                return;
            }

            System.out.println("Vehiculos registrados (se validara su disponibilidad al confirmar):");
            imprimirVehiculos(vehiculoDAO.listarTodos());
            int idVeh = leerEntero("Id del vehiculo: ");
            Vehiculo vehiculo = vehiculoDAO.buscarPorId(idVeh);
            if (vehiculo == null) {
                System.out.println("  El vehiculo indicado no existe.");
                return;
            }

            System.out.println("Conductores:");
            imprimirConductores(conductorDAO.listarTodos());
            int idCond = leerEntero("Id del conductor: ");
            Conductor conductor = conductorDAO.buscarPorId(idCond);
            if (conductor == null) {
                System.out.println("  El conductor indicado no existe.");
                return;
            }

            double horas = leerDouble("Horas estimadas del viaje: ");

            Asignacion a = motor.procesarAsignacion(
                    solicitud, vehiculo, conductor, horas, LIMITE_LEGAL_HORAS, LocalDate.now());

            System.out.println("  OK -> asignacion #" + a.getId()
                    + " confirmada y persistida (vehiculo " + vehiculo.getPatente()
                    + ", conductor " + conductor.getNombre() + ").");

        } catch (AsignacionInvalidaException e) {
            System.out.println("  RECHAZADA (no se persiste): " + e.getMessage());
        } catch (PersistenciaException e) {
            System.out.println("  ERROR de persistencia: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------- listados

    private void listarSolicitudesPendientes() {
        System.out.println("\n--- Solicitudes pendientes ---");
        try {
            List<SolicitudEnvio> l = solicitudDAO.listarPendientes();
            if (l.isEmpty()) {
                System.out.println("  (sin solicitudes pendientes)");
            } else {
                imprimirSolicitudes(l);
            }
        } catch (PersistenciaException e) {
            System.out.println("  ERROR de persistencia: " + e.getMessage());
        }
    }

    /** Opcion 3: ordena por capacidad (seleccion, a mano) y lista. */
    private void listarVehiculosOrdenadosPorCapacidad() {
        System.out.println("\n--- Vehiculos ordenados por capacidad (desc) ---");
        try {
            List<Vehiculo> l = vehiculoDAO.listarTodos();
            if (l.isEmpty()) {
                System.out.println("  (sin vehiculos registrados)");
                return;
            }
            Algoritmos.ordenarPorCapacidadDesc(l);
            imprimirVehiculos(l);
        } catch (PersistenciaException e) {
            System.out.println("  ERROR de persistencia: " + e.getMessage());
        }
    }

    private void listarConductores() {
        System.out.println("\n--- Conductores ---");
        try {
            List<Conductor> l = conductorDAO.listarTodos();
            if (l.isEmpty()) {
                System.out.println("  (sin conductores registrados)");
            } else {
                imprimirConductores(l);
            }
        } catch (PersistenciaException e) {
            System.out.println("  ERROR de persistencia: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------- busquedas

    /** Opcion 5: ordena por patente (insercion) y busca con busqueda binaria (ambas a mano). */
    private void buscarVehiculoPorPatente() {
        System.out.println("\n--- Buscar vehiculo por patente ---");
        try {
            List<Vehiculo> l = vehiculoDAO.listarTodos();
            if (l.isEmpty()) {
                System.out.println("  (sin vehiculos registrados)");
                return;
            }
            Algoritmos.ordenarPorPatenteAsc(l);                 // precondicion de la binaria
            String patente = leerTexto("Patente a buscar: ");
            int idx = Algoritmos.busquedaBinariaPorPatente(l, patente);
            if (idx >= 0) {
                System.out.println("  Encontrado:");
                imprimirVehiculos(List.of(l.get(idx)));
            } else {
                System.out.println("  No se encontro ningun vehiculo con patente " + patente + ".");
            }
        } catch (PersistenciaException e) {
            System.out.println("  ERROR de persistencia: " + e.getMessage());
        }
    }

    /** Opcion 6: busqueda lineal por coincidencia de nombre (a mano). */
    private void buscarConductorPorNombre() {
        System.out.println("\n--- Buscar conductor por nombre ---");
        try {
            List<Conductor> l = conductorDAO.listarTodos();
            String nombre = leerTexto("Nombre (o parte) a buscar: ");
            List<Conductor> hallados = Algoritmos.busquedaLinealPorNombre(l, nombre);
            if (hallados.isEmpty()) {
                System.out.println("  Sin coincidencias para \"" + nombre + "\".");
            } else {
                System.out.println("  Coincidencias (" + hallados.size() + "):");
                imprimirConductores(hallados);
            }
        } catch (PersistenciaException e) {
            System.out.println("  ERROR de persistencia: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------- impresion

    private void imprimirSolicitudes(List<SolicitudEnvio> l) {
        System.out.printf("  %-4s %-16s %-16s %10s %-10s%n",
                "Id", "Origen", "Destino", "Peso(kg)", "Estado");
        for (SolicitudEnvio s : l) {
            System.out.printf("  %-4d %-16s %-16s %10.2f %-10s%n",
                    s.getId(), s.getOrigen(), s.getDestino(), s.getPesoKg(), s.getEstado());
        }
    }

    private void imprimirVehiculos(List<Vehiculo> l) {
        System.out.printf("  %-4s %-10s %-8s %10s %-14s %9s%n",
                "Id", "Patente", "Tipo", "Cap.(kg)", "Estado", "$/km");
        for (Vehiculo v : l) {
            // Polimorfismo en el punto de uso: getTipo() y costoPorKilometro() resuelven
            // en VehiculoPesado o VehiculoLigero segun el objeto concreto.
            System.out.printf("  %-4d %-10s %-8s %10.2f %-14s %9.2f%s%n",
                    v.getId(), v.getPatente(), v.getTipo(), v.getCapacidadKg(),
                    v.getEstado(), v.costoPorKilometro(),
                    v.requiereMantenimiento() ? "  [MANT.PEND]" : "");
        }
    }

    private void imprimirConductores(List<Conductor> l) {
        System.out.printf("  %-4s %-22s %-10s %-12s %8s%n",
                "Id", "Nombre", "Licencia", "Vence", "Horas");
        for (Conductor c : l) {
            System.out.printf("  %-4d %-22s %-10s %-12s %8.2f%n",
                    c.getId(), c.getNombre(), c.getLicencia().getTipo(),
                    c.getLicencia().getFechaVencimiento(), c.getHorasAcumuladas());
        }
    }

    // ------------------------------------------------- entrada robusta

    /** Lee una linea y la convierte a entero; reintenta ante entradas no numericas. */
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

    /** Lee un decimal positivo (acepta coma o punto); reintenta ante entradas invalidas. */
    private double leerDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String linea = sc.nextLine().trim().replace(',', '.');
            try {
                double v = Double.parseDouble(linea);
                if (v <= 0) {
                    System.out.println("  Debe ser un valor positivo.");
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                System.out.println("  Valor no valido. Ingrese un numero (ej. 6.5).");
            }
        }
    }

    private String leerTexto(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }
}

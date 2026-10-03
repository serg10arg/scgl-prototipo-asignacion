package com.logisticaandina.scgl;

/**
 * Punto de entrada del prototipo SCGL. Delega toda la interaccion en el
 * menu de seleccion interactivo (TP3). La demostracion de 3 escenarios fijos
 * de TP1/TP2 queda cubierta ahora por la opcion 1 del menu (Registrar asignacion).
 */
public class App {
    public static void main(String[] args) {
        new MenuConsola().iniciar();
    }
}

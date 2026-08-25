package com.logisticaandina.scgl.dominio;

/** Vehiculo ligero. Sobrescribe el costo por km (polimorfismo). */
public class VehiculoLigero extends Vehiculo {

    public VehiculoLigero(int id, String patente, double capacidadKg, int kmActual,
                          int kmUltimoMantenimiento, int umbralMantenimientoKm,
                          EstadoVehiculo estado) {
        super(id, patente, capacidadKg, kmActual, kmUltimoMantenimiento,
              umbralMantenimientoKm, estado);
    }

    @Override
    public double costoPorKilometro() {
        return 0.95; // [SUPUESTO] valor de referencia para el prototipo
    }

    @Override
    public TipoVehiculo getTipo() { return TipoVehiculo.LIGERO; }
}

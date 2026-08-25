package com.logisticaandina.scgl.dominio;

/** Vehiculo pesado. Sobrescribe el costo por km (polimorfismo). */
public class VehiculoPesado extends Vehiculo {

    public VehiculoPesado(int id, String patente, double capacidadKg, int kmActual,
                          int kmUltimoMantenimiento, int umbralMantenimientoKm,
                          EstadoVehiculo estado) {
        super(id, patente, capacidadKg, kmActual, kmUltimoMantenimiento,
              umbralMantenimientoKm, estado);
    }

    @Override
    public double costoPorKilometro() {
        return 1.80; // [SUPUESTO] valor de referencia para el prototipo
    }

    @Override
    public TipoVehiculo getTipo() { return TipoVehiculo.PESADO; }
}

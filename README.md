# SCGL — Prototipo del módulo de Asignación

Prototipo operacional del **Sistema Centralizado de Gestión Logística y Mantenimiento
de Flotas (SCGL-Backend)** de *Logística Andina S.R.L.*, desarrollado en **Java puro
(orientado a objetos) + MySQL con JDBC**, sin frameworks de alto nivel.

Materializa el **módulo de asignación de viajes**, el de mayor riesgo del proyecto según
la justificación, como primera iteración de la fase de Inicio del PUD.

## Qué demuestra

Dada una solicitud de envío, el prototipo ejecuta el emparejamiento viaje–vehículo–conductor
(RF4) aplicando automáticamente las validaciones que hoy se hacen a mano, y solo entonces
persiste la operación:

- **RF5 / RN2 / RN3** — el conductor no excede el límite legal de horas y tiene licencia vigente.
- **RF6 / RN1 / RN4** — el vehículo está operativo y sin mantenimiento preventivo pendiente.
- **RN5** — capacidad del vehículo compatible con la carga.
- **RNF1** — la asignación se persiste como transacción atómica (commit/rollback).

Si una validación falla, se lanza una excepción de dominio y **no se persiste nada**
(flujo alternativo del CU-02), eliminando el conflicto de asignación (Problema 1).

## Arquitectura

- `dominio/` — Modelo OO. `Vehiculo` es **abstracta**; `VehiculoPesado` y `VehiculoLigero`
  la extienden y redefinen `costoPorKilometro()` (**herencia + polimorfismo**). Las reglas
  de negocio quedan **encapsuladas** en las entidades.
- `excepciones/` — Jerarquía de excepciones de validación y de persistencia.
- `persistencia/` — **JDBC puro**. DAOs y transacción atómica en `AsignacionDAO`.
  El ENUM `tipo` de la tabla `vehiculo` actúa como discriminador de herencia.
- `servicio/` — `MotorAsignacion` (CU-02): valida y, si corresponde, persiste.
- `App.java` — Demostración de 3 escenarios (1 válido, 2 rechazos).

## Cómo ejecutar

1. Crear la base y los datos de demo:
   ```
   mysql -u root -p < db/schema.sql
   ```
2. Ajustar `src/main/resources/config.properties` (URL, usuario, password).
3. Compilar y ejecutar (con el driver Connector/J en el classpath):
   ```
   mvn -q compile exec:java -Dexec.mainClass=com.logisticaandina.scgl.App
   ```
   o, sin Maven, con `javac`/`java` agregando el .jar del Connector/J al classpath.

## Salida esperada

```
A) Asignacion valida     -> OK -> asignacion #1 persistida (...)
B) Rechazo por vehiculo  -> RECHAZADA (no se persiste): ... requiere mantenimiento preventivo ...
C) Rechazo por conductor -> RECHAZADA (no se persiste): ... excede el limite legal de horas ...
```

## Trazabilidad

Cada regla y requerimiento implementado se comenta en el código con su etiqueta
(RF*, RNF*, RN*, CU-*), coherente con el documento del TP1.

## Nota técnica

La única dependencia externa es el **driver JDBC** de MySQL (Connector/J), que es un
*driver*, no un framework de alto nivel — por lo que se respeta la restricción tecnológica
del proyecto (RNF9).

# SCGL-Backend · Prototipo del módulo de Asignación

![Java](https://img.shields.io/badge/Java-17-orange)
![MySQL](https://img.shields.io/badge/MySQL-8-blue)
![Build](https://img.shields.io/badge/Maven-3.9%2B-C71A36)
![Metodología](https://img.shields.io/badge/Metodolog%C3%ADa-PUD-2E6E5E)
![Estado](https://img.shields.io/badge/Estado-TP2%20completado-brightgreen)

> Prototipo operacional del **Sistema Centralizado de Gestión Logística y Mantenimiento de
> Flotas (SCGL-Backend)** de *Logística Andina S.R.L.*, desarrollado en **Java (orientado a
> objetos) + MySQL con JDBC**, sin frameworks de alto nivel. Es un trabajo académico
> **incremental** que evoluciona a lo largo de los TP de la materia (fase de Inicio del PUD).

---

## Tabla de contenidos

1. [Contexto](#1-contexto)
2. [Alcance](#2-alcance)
3. [Qué demuestra](#3-qué-demuestra)
4. [Arquitectura](#4-arquitectura)
5. [Modelo de datos y reglas de negocio](#5-modelo-de-datos-y-reglas-de-negocio)
6. [Stack tecnológico](#6-stack-tecnológico)
7. [Requisitos previos](#7-requisitos-previos)
8. [Instalación y ejecución](#8-instalación-y-ejecución)
9. [Scripts SQL](#9-scripts-sql)
10. [Estructura del repositorio](#10-estructura-del-repositorio)
11. [Documentación](#11-documentación)
12. [Hoja de ruta (incremental)](#12-hoja-de-ruta-incremental)
13. [Autoría](#13-autoría)

---

## 1. Contexto

Proyecto de la materia **Seminario de Práctica de Informática** (Licenciatura en
Informática). Se desarrolla siguiendo el **Proceso Unificado de Desarrollo (PUD)** y de forma
**incremental** a lo largo de cuatro trabajos prácticos (TP1 a TP4): cada entrega retoma y
amplía la anterior sobre **este mismo repositorio**.

El caso es la empresa ficticia *Logística Andina S.R.L.* (transporte de cargas), que creció de
15 a más de 50 vehículos y opera hoy en silos (Despacho, RRHH/Conductores, Taller). El SCGL
centraliza la operación en una única fuente de datos con reglas de negocio validadas por el
sistema.

## 2. Alcance

Este repositorio contiene el **prototipo**: un modelo operacional que implementa **solo
algunas características** del sistema final (Kendall & Kendall, 2011). En concreto, materializa
el **módulo de asignación de viajes (CU-02)**, el de mayor riesgo y valor del proyecto.

| Elemento | Estado en el prototipo |
|----------|------------------------|
| Módulo de asignación (CU-02: emparejar solicitud–vehículo–conductor con validaciones) | ✅ implementado |
| Modelo de datos completo (5 tablas, InnoDB) | ✅ creado |
| Módulo de mantenimiento (CU-06 / RF-09) | 🟡 modelado y con tabla; clase Java pendiente |
| Cierre de viaje, alertas, consultas/indicadores desde la app (RF-07/08/10/11) | ⛔ pendiente |

> El **modelo de análisis y de clases es integral** (describe todo el sistema); el prototipo
> implementa el subconjunto operacional. Esta distinción es propia del PUD y está detallada en
> el informe y en [`docs/PROJECT_LOG.md`](docs/PROJECT_LOG.md).

## 3. Qué demuestra

Dada una solicitud de envío, el prototipo ejecuta el emparejamiento viaje–vehículo–conductor
(**RF-04**) aplicando automáticamente las validaciones que hoy se hacen a mano, y **solo
entonces** persiste la operación:

- **RF-06 / RN1 / RN4** — el vehículo está operativo y sin mantenimiento preventivo pendiente.
- **RN5** — la capacidad del vehículo es compatible con la carga.
- **RF-05 / RN3 / RN2** — el conductor tiene licencia vigente y no excede el límite legal de horas.
- **RNF-01** — la asignación se persiste como **transacción atómica** (commit/rollback).

Si una validación falla, se lanza una excepción de dominio y **no se persiste nada** (flujo
alternativo del CU-02), eliminando de raíz el conflicto de asignación (Problema 1 del TP1).

## 4. Arquitectura

Diseño por capas, con las reglas de negocio **encapsuladas** en el dominio:

- **`dominio/`** — Modelo OO. `Vehiculo` es **abstracta**; `VehiculoPesado` y `VehiculoLigero`
  la extienden y redefinen `costoPorKilometro()` (**herencia + polimorfismo**). Incluye
  `Conductor`, `Licencia`, `SolicitudEnvio`, `Asignacion` y las enumeraciones de estado.
- **`servicio/`** — `MotorAsignacion` (CU-02): valida en orden (RN1 → RN4 → RN5 → RN3 → RN2) y,
  si corresponde, delega la persistencia.
- **`persistencia/`** — **JDBC puro**: `ConexionMySQL` y los DAO. La transacción atómica vive
  en `AsignacionDAO.guardarEnTransaccion()`.
- **`excepciones/`** — Jerarquía de excepciones de validación (`AsignacionInvalidaException` y
  derivadas) y de persistencia (`PersistenciaException`).
- **`App.java`** — Punto de entrada (demostración de 3 escenarios: 1 válido, 2 rechazos).

## 5. Modelo de datos y reglas de negocio

Base **`scgl`** (MySQL / InnoDB), en tercera forma normal, con 5 tablas:
`conductor`, `vehiculo`, `solicitud_envio`, `asignacion` y `orden_mantenimiento`.
La herencia de vehículos se mapea con **tabla única + discriminador** (`vehiculo.tipo`).

Reglas de negocio principales (etiquetadas en el código y en el modelo):

| Regla | Descripción |
|-------|-------------|
| RN1 | El vehículo debe estar operativo para ser asignado. |
| RN2 | El conductor no debe exceder el límite legal de jornada (44 h semanales). |
| RN3 | El conductor debe tener licencia vigente. |
| RN4 | El vehículo ingresa a mantenimiento al alcanzar el umbral de kilometraje. |
| RN5 | La capacidad del vehículo debe ser compatible con la carga. |

## 6. Stack tecnológico

- **Java 17** (orientado a objetos, sin frameworks de alto nivel).
- **Maven** (empaquetado JAR).
- **MySQL 8** con motor **InnoDB** (claves foráneas + transacciones ACID).
- **JDBC** mediante `mysql-connector-j 8.4.0` — única dependencia externa.

> **Nota técnica (RNF-09):** Connector/J es un *driver* JDBC, no un framework de alto nivel;
> por eso se respeta la restricción tecnológica del proyecto.

## 7. Requisitos previos

- JDK 17+
- Maven 3.9+
- MySQL 8 (servidor accesible localmente)

## 8. Instalación y ejecución

1. **Clonar** el repositorio:
   ```bash
   git clone https://github.com/serg10arg/scgl-prototipo-asignacion.git
   cd scgl-prototipo-asignacion
   ```
2. **Crear la base, las tablas y los datos de demostración:**
   ```bash
   mysql -u root -p < db/schema.sql
   mysql -u root -p < db/datos.sql
   ```
3. **Configurar la conexión:** copiar `config.properties.example` a
   `src/main/resources/config.properties` y ajustar URL, usuario y contraseña.
4. **Compilar y ejecutar:**
   ```bash
   mvn -q compile exec:java -Dexec.mainClass=com.logisticaandina.scgl.App
   ```

**Salida esperada:**
```
A) Asignacion valida     -> OK -> asignacion #1 persistida (...)
B) Rechazo por vehiculo  -> RECHAZADA (no se persiste): ... requiere mantenimiento preventivo ...
C) Rechazo por conductor -> RECHAZADA (no se persiste): ... excede el limite legal de horas ...
```

## 9. Scripts SQL

Ubicados en `db/`, uno por operación (coherente con los entregables del TP2):

| Script | Contenido |
|--------|-----------|
| `db/schema.sql` | Creación de la base y las 5 tablas (DDL). |
| `db/datos.sql` | Datos de demostración (inserción). |
| `db/consultas.sql` | Consultas de explotación (historial, pendientes, mantenimiento, indicadores). |
| `db/borrado.sql` | Borrado de registros y verificación de integridad referencial. |

## 10. Estructura del repositorio

```
scgl-prototipo-asignacion/
├── pom.xml
├── README.md
├── SECCION-PROTOTIPO.md
├── config.properties.example
├── docs/
│   └── PROJECT_LOG.md            # bitácora del proyecto (fuente de verdad del estado)
├── db/
│   ├── schema.sql · datos.sql · consultas.sql · borrado.sql
└── src/main/java/com/logisticaandina/scgl/
    ├── App.java
    ├── dominio/                  # Vehiculo(+subclases), Conductor, Licencia, SolicitudEnvio, Asignacion, enums
    ├── servicio/                 # MotorAsignacion
    ├── persistencia/             # ConexionMySQL, *DAO
    └── excepciones/              # AsignacionInvalidaException (+derivadas), PersistenciaException
```

## 11. Documentación

- **[`docs/PROJECT_LOG.md`](docs/PROJECT_LOG.md)** — bitácora viva del proyecto: qué se
  construyó en cada fase (TP1/TP2), decisiones, y lo pendiente de TP3/TP4. **Leer antes de
  retomar el trabajo.**
- **[`SECCION-PROTOTIPO.md`](SECCION-PROTOTIPO.md)** — detalle de la sección prototipo.
- **Informe del TP** (`CABRERA-SERGIO-AP2`, entregado en la plataforma) — análisis, diseño,
  implementación, pruebas, DER, DDL/DML y comunicaciones, con la trazabilidad completa.

## 12. Hoja de ruta (incremental)

| TP | Foco | Entrega | Estado |
|----|------|---------|--------|
| TP1 | Análisis, diseño y prototipo del módulo de asignación | — | ✅ |
| TP2 | Flujos del PUD, modelo de datos, SQL y comunicaciones | — | ✅ |
| TP3 | Codificación POO: menú interactivo, pilares, excepciones | 2026-10-19 | ⏳ pendiente |
| TP4 | Versión integradora: patrón de diseño, CRUD, arreglos/`ArrayList`, archivos, video | 2026-11-09 | ⏳ pendiente |

El detalle de lo pendiente (requerimientos, características exigidas y estado real del repo por
cada TP) está en [`docs/PROJECT_LOG.md`](docs/PROJECT_LOG.md).

## 13. Autoría

- **Alumno:** Sergio Cabrera
- **Docente:** Mg. Ing. Hugo Fernando Frías
- **Materia:** Seminario de Práctica de Informática — Licenciatura en Informática
- **Uso:** proyecto académico.
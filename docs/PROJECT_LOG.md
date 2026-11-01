# PROJECT_LOG · SCGL-Backend

**Sistema Centralizado de Gestión Logística y Mantenimiento de Flotas** — prototipo del
módulo de asignación (Logística Andina S.R.L.). Bitácora viva del repositorio: es la
**fuente de verdad** del estado del código. Leer este archivo **antes** de retomar el
trabajo o de generar documentación, para no desalinear el informe del código real.

- **Stack:** Java 17 (OO puro) · Maven · MySQL 8 (InnoDB) · JDBC `mysql-connector-j 8.4.0`
  (única dependencia — RNF-09: sin frameworks de alto nivel).
- **Paquete raíz:** `com.logisticaandina.scgl` (dominio · servicio · persistencia · excepciones · App).
- **Arquitectura:** dominio con reglas encapsuladas → servicio de validación (`MotorAsignacion`)
  → persistencia por DAO (JDBC). Cableado por `new` (sin contenedor de inyección, RNF-09).
- **Convención de commits:** Conventional Commits en español, minúsculas y sin tildes
  (`feat(scope): ...`, `docs:`, `chore:`, `refactor(...)`). Scopes: `dominio`, `servicio`,
  `persistencia`, `excepciones`, `app`, `db`.
- **Nomenclatura de artefactos** (para mantener trazabilidad con el informe): `RF-xx`
  (req. funcional), `RNF-xx` (req. no funcional), `RN-x` (regla de negocio), `CU-xx`
  (caso de uso), `CP-xx` (caso de prueba).
- **Última actualización:** 2026-11-01.

---

## Estado actual (resumen)

| Área | Estado |
|------|--------|
| Modelo de dominio (jerarquía Vehiculo, Conductor/Licencia, SolicitudEnvio, Asignacion) | ✅ |
| Reglas de negocio RN1–RN5 en `MotorAsignacion` (validación antes de persistir) | ✅ |
| Persistencia JDBC + transacción atómica (`AsignacionDAO.guardarEnTransaccion`) | ✅ |
| Base de datos: 6 tablas InnoDB + 5 FK (`db/schema.sql`; `licencia` separada en TP4) | ✅ (Fase 8) |
| Scripts SQL de inserción / consulta / borrado (`db/datos.sql`, `consultas.sql`, `borrado.sql`) | ✅ (Fase 4) |
| CU-02 «Asignar viaje» de punta a punta (desde el menú interactivo) | ✅ (Fase 6) |
| Documento TP3 (`CABRERA-SERGIO-AP3`) entregado — **100/100** | ✅ (Fase 7) |
| Menú de selección interactivo por consola (`MenuConsola`) | ✅ (Fase 6) |
| Ordenación y búsqueda a mano (`util/Algoritmos`) | ✅ (Fase 6) |
| Cierre de viaje + alerta de mantenimiento (RF-07/RF-08) | ⛔ pendiente |
| Módulo de mantenimiento en código (RF-09) — hoy solo existe la tabla | ⛔ pendiente |
| Consultas/indicadores desde la app (RF-10/RF-11) | ⛔ pendiente |
| Pruebas automatizadas (JUnit) — los CP del informe son especificaciones | ⛔ pendiente |

---

## Fase 1 · Estructura del proyecto y modelo de dominio

- **Estado:** ✅ completada (2026-08-25 · TP1)
- **Entregado:**
    - **Proyecto Maven** (`scgl-prototipo-asignacion`), Java 17, empaquetado JAR, con
      `mysql-connector-j 8.4.0` como única dependencia (RNF-09); `.gitignore` con IDE, SO y
      credenciales locales.
    - **Jerarquía de vehículos:** `Vehiculo` (abstracta) con `VehiculoPesado` y
      `VehiculoLigero`, que redefinen `costoPorKilometro()` y `getTipo()` (herencia +
      polimorfismo, RNF-08).
    - **Entidades del dominio:** `Conductor` (con `Licencia` como objeto de valor),
      `SolicitudEnvio` y `Asignacion` (entidad asociativa que vincula solicitud, vehículo y
      conductor).
    - **Enumeraciones:** `TipoVehiculo`, `EstadoVehiculo` (OPERATIVO / EN_TALLER /
      MANT_PENDIENTE), `EstadoSolicitud` (PENDIENTE / ASIGNADA / CERRADA), `EstadoAsignacion`
      (CONFIRMADA / CERRADA).
    - **Reglas de negocio como comportamiento del dominio:** `Vehiculo.estaOperativo()` (RN1),
      `requiereMantenimiento()` (RN4), `puedeTransportar(pesoKg)` (RN5);
      `Conductor.tieneLicenciaVigente(fecha)` (RN3), `tieneHorasDisponibles(horas, limite)` (RN2).
- **Decisiones y hallazgos:**
    - Las reglas viven en las entidades (no en el servicio) para mantener el dominio rico y
      encapsulado (RNF-08).
    - `Licencia` se modela como objeto de valor con su propia lógica `estaVigente(fecha)`.
    - Se elige herencia para diferenciar tipos de unidad porque difieren en **comportamiento**
      (costo por kilómetro), no en estructura.
- **Verificación:** el reactor compila; las entidades quedan disponibles para el servicio.
- **Siguiente:** Fase 2 · Reglas de negocio en el servicio y excepciones.

---

## Fase 2 · Servicio de asignación y manejo de excepciones

- **Estado:** ✅ completada (2026-08-25 · TP1)
- **Entregado:**
    - **`MotorAsignacion.procesarAsignacion(solicitud, vehiculo, conductor, horasEstimadas,
      limiteLegalHoras, hoy)`:** núcleo de CU-02. Valida en orden **RN1 → RN4 → RN5 → RN3 →
      RN2** y, solo si todas se cumplen, construye la `Asignacion` y delega su guardado.
    - **Jerarquía de excepciones de negocio:** `AsignacionInvalidaException` (base) con
      `VehiculoNoDisponibleException`, `CapacidadInsuficienteException` y
      `ConductorNoDisponibleException`; `PersistenciaException` para fallos de datos.
    - **Límite legal parametrizable:** `LIMITE_LEGAL_HORAS = 44.0` en `App` (CCT transporte de
      cargas art. 30.1; marco Ley 11.544 — RNF-04).
- **Decisiones y hallazgos:**
    - **Validar antes de persistir:** el servicio verifica todas las reglas y solo entonces
      escribe. Si una falla, lanza excepción y no se persiste nada. Esto elimina de raíz el
      *Problema 1* del TP1 (conflictos por cruce manual), en lugar de solo reducirlo.
    - Una excepción específica por tipo de regla hace el rechazo autoexplicativo y testeable.
- **Verificación:** los tres escenarios de `App` ejercitan A (válido), B (rechazo por RN4) y
  C (rechazo por RN2).
- **Siguiente:** Fase 3 · Persistencia MySQL con JDBC.

---

## Fase 3 · Persistencia MySQL con JDBC

- **Estado:** ✅ completada (2026-08-25 · TP1)
- **Entregado:**
    - **`ConexionMySQL.obtener()`:** fábrica de conexiones JDBC leyendo `config.properties`
      (plantilla `config.properties.example` versionada; credenciales fuera del repo).
    - **DAOs:** `ConductorDAO`, `VehiculoDAO`, `SolicitudDAO` (`buscarPorId`) y `AsignacionDAO`
      (`guardarEnTransaccion`). `VehiculoDAO` reconstruye la subclase concreta según el
      discriminador `tipo`.
    - **Transacción atómica (RNF-01):** `AsignacionDAO.guardarEnTransaccion` abre la
      transacción (`setAutoCommit(false)`), inserta la asignación y hace `commit`; ante error,
      `rollback`.
    - **Esquema inicial** `db/schema.sql` (DDL + datos de demostración) y **`App`** en command
      mode que ejecuta el flujo crear → validar → persistir por consola.
    - **README** de instalación y documento de la sección prototipo.
- **Decisiones y hallazgos:**

  | # | Decisión | Motivo | Evidencia |
      |---|----------|--------|-----------|
  | 1 | La transacción se encapsula en el **DAO** (`guardarEnTransaccion`), no en el servicio | El servicio ya validó todo antes; el DAO es el dueño del acceso y de la atomicidad | `commit`/`rollback` en `AsignacionDAO`; escenarios A/B/C |
  | 2 | **JDBC puro**, sin ORM ni framework de inyección; cableado por `new` | RNF-09 (Java y MySQL sin frameworks de alto nivel) | `pom.xml`: única dependencia `mysql-connector-j 8.4.0` |
  | 3 | Credenciales fuera del repo (`config.properties.example` + `.gitignore`) | Seguridad (RNF-05); reproducibilidad | plantilla versionada, secreto ignorado |

- **Verificación:** `mvn package` en verde; `java -jar` ejecuta los tres escenarios contra MySQL.
- **Deuda que entra en fases siguientes:** solo se implementa el núcleo de asignación
  (RF-04/05/06); el cierre de viaje y las alertas quedan diseñados pero no codificados.
- **Siguiente:** Fase 4 · Modelo de datos relacional y scripts SQL.

---

## Fase 4 · Modelo de datos relacional y scripts SQL

- **Estado:** ✅ completada (2026-09 · TP2)
- **Entregado:**
    - **`db/schema.sql` como DDL puro:** se separaron los datos de demostración; el esquema
      queda solo con `CREATE DATABASE` + 5 `CREATE TABLE` (conductor, vehiculo,
      solicitud_envio, orden_mantenimiento, asignacion), InnoDB y 4 claves foráneas.
    - **`db/datos.sql`:** datos de demostración (2 conductores, 2 vehículos, 1 orden, 2
      solicitudes) más una asignación de ejemplo para poder ejercitar las consultas.
    - **`db/consultas.sql`:** 5 consultas de explotación — historial unificado (JOIN de
      asignacion + solicitud + vehiculo + conductor, CU-07), solicitudes pendientes (CU-02),
      vehículos que requieren atención (RN1/RN4), asignaciones por conductor (CU-08) y órdenes
      de mantenimiento abiertas (CU-06).
    - **`db/borrado.sql`:** borrado de una solicitud pendiente + verificación, y la evidencia
      documentada del rechazo por integridad referencial (ERROR 1451).
- **Decisiones y hallazgos:**

  | # | Decisión | Motivo | Evidencia |
      |---|----------|--------|-----------|
  | 1 | Estructura de 4 scripts (schema / datos / consultas / borrado), uno por entregable del TP2 | Mapear 1:1 los entregables «creación, inserción, consulta y borrado» | archivos en `db/` |
  | 2 | Herencia mapeada como **tabla única con discriminador** `tipo` | Las subclases difieren en comportamiento, no en estructura; evita tablas vacías y joins; mantiene 3FN | tabla `vehiculo` con `tipo ENUM` |
  | 3 | Relación solicitud→asignacion **1:0..1** por ciclo de estados (PENDIENTE→ASIGNADA), sin `UNIQUE` físico | El estado de la solicitud gobierna la reasignación | `EstadoSolicitud`; datos.sql |
  | 4 | `ON DELETE` por defecto (**RESTRICT**) en las FK | Proteger la trazabilidad (RF-10): no arrastrar el historial al borrar un vehículo/conductor | ERROR 1451 al borrar vehículo con orden |

- **Verificación:** ejecutado en MySQL/MariaDB: se crean las **5 tablas** con motor InnoDB y
  quedan activas las **4 FK** (`fk_om_vehiculo`, `fk_asig_solicitud`, `fk_asig_vehiculo`,
  `fk_asig_conductor`); las 5 consultas devuelven datos y el borrado protegido dispara
  ERROR 1451.
- **Commits (2026-09-13):** `refactor(db): separa los datos de demostracion en datos.sql; schema.sql queda solo con el DDL`,
  `feat(db): consultas SQL de explotacion del modelo (historial, pendientes, mantenimiento, indicadores)`,
  `feat(db): script de borrado y verificacion de integridad referencial`,
  `docs: documenta los scripts de datos, consultas y borrado en el README`. El repositorio
  queda en 16 commits.
- **Siguiente:** Fase 5 · Documentación de ingeniería TP2 y reconciliación con el repo.

---

## Fase 5 · Documentación de ingeniería TP2 y reconciliación con el repo

- **Estado:** ✅ completada (2026-09 · TP2)
- **Entregado:**
    - **Informe TP2** (`CABRERA-SERGIO-AP2`) con los cuatro flujos del PUD (análisis, diseño,
      implementación, pruebas), definición de la base de datos, DER, DDL/DML y definiciones de
      comunicación; **7 diagramas** (PlantUML) y matriz de trazabilidad global RF→CU→clase→
      tabla→verificación.
    - **Guía de coloquio** con preguntas probables y datos clave.
- **Decisiones y hallazgos:**
    - **Hallazgo crítico (origen de esta bitácora):** el informe se había redactado con una
      nomenclatura divergente del repo (`viaje` vs `asignacion`, una tabla `cliente`
      inexistente, `ServicioAsignacion`/`ConexionBD`, método `esCompatibleCon`, y un chequeo
      de solapamiento que el código no hace). Se **reconció todo el documento tomando el repo
      como fuente de verdad**: nombres de clases/tablas/columnas, el diagrama de secuencia
      (transacción en el DAO, sin solapamiento) y las salidas SQL reales.
    - **Lección incorporada:** cualquier artefacto (informe, diagramas, prompts) se deriva del
      estado real del repositorio. Este `PROJECT_LOG.md` existe para hacer explícito ese
      estado y evitar la desalineación a futuro.
- **Verificación:** informe, repo y guía de coloquio coherentes entre sí (mismos nombres,
  mismo modelo, mismas salidas).
- **Ajuste por devolución del docente (2026-09-15):** el docente validó centrar las
  realizaciones y el diagrama de secuencia en CU-02 (no hace falta desarrollar los CU de ABM),
  pero pidió que **el diagrama de clases sea integral y refleje todo el modelado**. Se rehízo
  el diagrama de clases de dominio (Figura 2) como **modelo integral** —incluye
  `OrdenMantenimiento` y todas las asociaciones—, diferenciándolo del prototipo operacional
  (que implementa CU-02).
- **Resultado TP2: 100/100.** Devolución del docente: presentación clara y completa; diagramas
  de secuencia y documentación correctos; repositorio accesible; código comprensible con su
  explicación. Única observación: **detalles menores de normalización en el DER** que —según el
  propio docente— **no afectan la performance** a este volumen de datos. Es un aporte: **no
  requiere reentrega** y se complementa en las próximas entregas.
- **Siguiente:** Fase 6 · TP3 · Menú de selección interactivo.

---

## Fase 6 · TP3 · Menú de selección interactivo

- **Estado:** ✅ completada (2026-10-03 · TP3) — documento en la Fase 7
- **Entregado:**
    - **`MenuConsola`** (paquete raíz): programa interactivo por consola con bucle `while` +
      `switch`. Opciones: 1) registrar asignación (CU-02: elige solicitud pendiente, vehículo,
      conductor y horas; valida RN1–RN5 en `MotorAsignacion` antes de persistir); 2) listar
      solicitudes pendientes; 3) listar vehículos ordenados por capacidad; 4) listar
      conductores; 5) buscar vehículo por patente; 6) buscar conductor por nombre; 0) salir.
      Entrada robusta (`leerEntero`/`leerDouble` reintentan ante valores inválidos; `leerDouble`
      acepta coma o punto y exige valor positivo). Captura `AsignacionInvalidaException` (rechazo,
      no persiste) y `PersistenciaException` por opción, sin cortar el programa.
    - **`util/Algoritmos`** (clase de utilidades `final`, constructor privado), implementados a
      mano sin `Collections.sort` ni Streams: **selección** (capacidad desc., O(n²)),
      **inserción** (patente asc., precondición de la binaria), **búsqueda binaria** por patente
      (O(log n)) y **búsqueda lineal** por coincidencia parcial de nombre (O(n)).
    - **Listados de solo lectura en los DAO:** `VehiculoDAO.listarTodos()`,
      `ConductorDAO.listarTodos()` y `SolicitudDAO.listarPendientes()` (devuelven
      `List`/`ArrayList`). Se extrajo un `mapear(ResultSet)` privado en `ConductorDAO` y
      `SolicitudDAO` (como ya tenía `VehiculoDAO`), reutilizado por `buscarPorId` y los listados.
    - **`App` fino:** solo `new MenuConsola().iniciar()`. La demo de 3 escenarios fijos
      (A/B/C) se reemplaza por la opción 1 del menú; `LIMITE_LEGAL_HORAS = 44.0` se mudó de
      `App` a `MenuConsola` (sigue parametrizado, RNF-04).
- **Decisiones y hallazgos:**
    - La interacción se concentra en `MenuConsola`; la validación sigue en `MotorAsignacion` y
      el acceso a datos en los DAO (misma separación de responsabilidades de TP1/TP2).
    - Los listados son de **solo lectura**: la única escritura sigue siendo
      `AsignacionDAO.guardarEnTransaccion` (CU-02). El CRUD completo queda para TP4.
    - Cubre de la consigna TP3: **menú de selección**, estructuras condicionales/repetitivas,
      manejo de excepciones y **ordenación/búsqueda** (opcional). La tabla del menú usa
      polimorfismo en el punto de uso (`getTipo()`, `costoPorKilometro()`).
- **Ajuste posterior (2026-10-04):**
    - **Salida prolija ante EOF:** el bucle de `iniciar()` captura `NoSuchElementException`
      (entrada cerrada con Ctrl+D / Ctrl+Z o fin de un pipe), informa «Entrada finalizada.» y
      termina normalmente en lugar de abortar con una excepción. Cubre también un EOF en medio
      de la opción 1, porque esa excepción no la atrapan los `catch` de la opción y llega al bucle.
    - **Encabezado honesto en la opción 1:** «Vehiculos disponibles:» pasa a «Vehiculos
      registrados (se validara su disponibilidad al confirmar):», ya que el listado incluye
      unidades en taller o con mantenimiento pendiente (las rechaza `MotorAsignacion`, RN1/RN4).
- **Verificación:** `git apply --check` limpio y `mvn -q compile` en verde (en ambos parches).
  EOF probado sin base de datos, pasando por pipe las líneas `abc` y `9` a
  `java -cp target/classes com.logisticaandina.scgl.App` y cerrando la entrada: rechaza
  las entradas inválidas, muestra «Entrada finalizada.» y termina con exit 0.
- **Ejecución contra MySQL 8.0.41 (2026-10-04):** con `schema.sql` + `datos.sql` cargados
  (solo la solicitud 2 queda PENDIENTE, porque `datos.sql` ya asigna la 1), se corrió la
  opción 1 dos veces sobre la solicitud 2:
    - **Rechazo RN4** (vehículo 2, conductor 1, 6 h): «RECHAZADA (no se persiste): Vehiculo
      CC456DD requiere mantenimiento preventivo (umbral de km superado).» (12.000 km desde el
      último mantenimiento, umbral 10.000).
    - **Asignación válida** (vehículo 1, conductor 1, 6 h): «OK -> asignacion #2 confirmada y
      persistida (vehiculo AA123BB, conductor Ana Gomez).»
    - **En la base:** solo se agregó la asignación #2; la solicitud 2 pasó a ASIGNADA y las
      horas de Ana Gomez de 20 a 26 (la transacción de `AsignacionDAO` aplicó los tres cambios).
    - **Orden de la demo:** primero el rechazo y después la válida; al revés, la solicitud 2
      queda asignada y no hay otra pendiente para mostrar el rechazo.
- **Hallazgo y corrección: fechas corridas por zona horaria (2026-10-04).**
    - **Síntoma:** la asignación se guardó con `fecha_hora` 5 h antes de la hora real
      (00:27 local → 19:27 del día anterior).
    - **Causa:** `setTimestamp`/`getTimestamp`/`getDate` convertían entre la zona de la JVM
      (equipo en `Romance Standard Time`, UTC+2) y la de la conexión (`serverTimezone=
      America/Argentina/Buenos_Aires`, UTC-3). También afectaba la **lectura** de
      `ventana_inicio`/`ventana_fin` y, en casos límite, la fecha de vencimiento de la licencia.
    - **Corrección:** las columnas `DATETIME`/`DATE` no tienen zona, así que los DAO pasan a
      usar `java.time` directo: `setObject(LocalDateTime)` en `AsignacionDAO` y
      `getObject(..., LocalDateTime.class / LocalDate.class)` en `SolicitudDAO` y
      `ConductorDAO`, que Connector/J transfiere sin conversión. No cambia la URL ni
      `config.properties`.
    - **Verificación:** las ventanas de la solicitud 2 se leen `08:00 → 22:00` (igual que
      `datos.sql`) y una asignación registrada a las 00:50:42 se guardó como 00:50:44. La
      prueba de escritura se hizo en una base temporal (`scgl_tzcheck`, ya eliminada).
    - Después se recargaron `schema.sql` + `datos.sql` en `scgl` para descartar la asignación
      guardada con la hora corrida y dejar la solicitud 2 PENDIENTE.
- **Evidencia de ejecución:** las capturas de consola (asignación válida y rechazo por RN4)
  se tomaron con el código ya corregido y siguiendo el orden de la demo; están en el informe
  del TP3 (Fase 7).
- **Commits (2026-10-03):** `feat(persistencia): listados de solo lectura en los DAO (listarTodos, listarPendientes)`,
  `feat(util): ordenamiento (seleccion, insercion) y busqueda (binaria, lineal) a mano`,
  `feat(app): menu de seleccion interactivo del prototipo (CU-02, listados y busquedas)`,
  `refactor(app): App delega la interaccion en MenuConsola`, más este registro en la bitácora.
  Ajustes (2026-10-04): `fix(app): salida prolija ante EOF y encabezado honesto en el listado de vehiculos`,
  `fix(persistencia): fechas con java.time en JDBC, sin conversion de zona horaria`.
- **Siguiente:** Fase 7 · Documentación del TP3.

---

## Fase 7 · Documentación del TP3

- **Estado:** ✅ completada (2026-10 · TP3)
- **Entregado:**
    - **Informe TP3** (`CABRERA-SERGIO-AP3`, 6 páginas): introducción; explicación del
      desarrollo en Java organizada **por característica de la consigna** (sintaxis y tipos,
      objetos y constructores, encapsulamiento, herencia, polimorfismo, abstracción,
      excepciones, menú de selección, estructuras de control, ordenación y búsqueda), cada una
      con su fragmento de código real del repo; tabla resumen característica → archivo/método;
      evidencia de ejecución (Figura 1: asignación válida #2; Figura 2: rechazo por RN4);
      repositorio y commits; referencias APA.
    - **README** actualizado al menú interactivo (`MenuConsola`, `util/Algoritmos`, salida
      esperada).
- **Decisiones y hallazgos:**
    - El informe se derivó del repo (regla de la Fase 5): mismos nombres de clases, métodos y
      mensajes de consola.
- **Resultado TP3: 100/100.** Sin observaciones nuevas; sigue abierta solo la observación menor
  de normalización del DER del TP2 (ver Deuda conocida).
- **Siguiente:** Fase 8 · TP4 · Versión final integradora.

---

## Fase 8 · TP4 · Versión final integradora

- **Estado:** 🟡 en curso (2026-11 · TP4 · entrega 2026-11-09)
- **Decisiones de alcance (2026-11-01):**
    - **Patrón de diseño seleccionado: MVC** (el que desarrolla el material M4L4), aplicado al
      módulo de mantenimiento (CU-06 / RF-09); el **Modelo** persiste a través del DAO ya
      definido en la arquitectura (coherencia con TP1/TP2). Reemplaza la idea previa
      «DAO + Factory».
    - **CRUD de órdenes de mantenimiento** con alta, consulta y actualización; la **baja es
      lógica** (cerrar la orden, sin `DELETE`), coherente con las FK `RESTRICT` y la
      trazabilidad del historial (RF-10).
    - **Corrección de la observación del TP2 (normalización del DER):** la licencia pasa a su
      propia tabla.
- **C1 · Licencia en tabla propia (2026-11-01):**
    - `db/schema.sql`: `conductor` queda con `id_conductor`, `nombre`, `horas_acumuladas`; nueva
      tabla `licencia` (`id_licencia`, `id_conductor` UNIQUE + `fk_lic_conductor`, `numero`
      UNIQUE, `tipo`, `fecha_vencimiento`). Relación **1:1**; la licencia depende
      existencialmente del conductor y corresponde al objeto de valor `Licencia` del dominio.
    - `db/datos.sql`: conductores y licencias en inserts separados (mismos valores).
    - `ConductorDAO`: `SELECT_BASE` con `JOIN licencia`; `mapear` lee `numero`, `tipo`,
      `fecha_vencimiento`. El dominio (`Conductor`, `Licencia`) **no cambia**.
    - `db/consultas.sql`: consulta 6 (conductores y vigencia de su licencia, RN3).
      `db/borrado.sql`: evidencia de `RESTRICT` sobre `fk_lic_conductor` (ERROR 1451).
    - **Verificación:** scripts cargados en MariaDB local: 6 tablas, 5 FK; `DELETE` de un
      conductor con licencia → ERROR 1451. Menú: opción 4 lista conductores con su licencia;
      opción 1 rechaza por RN4 (CC456DD) y confirma la asignación #2 (AA123BB + Ana Gomez).
- **Pendiente en esta fase:** C2 dominio `OrdenMantenimiento` + interfaz `CrudDAO<T>` +
  `OrdenMantenimientoDAO`; C3 MVC de mantenimiento (opción 7 del menú); C4 arreglo +
  `ArrayList` (resumen de flota); C5 archivos (exportar/recuperar CSV); informe AP4; video.

---

## Pendiente — TP3 y TP4 (consignas de la cátedra)

> Estas dos secciones **sí** son las consignas de la materia (TP3 y TP4), incrementales
> sobre TP1/TP2 y sobre **este mismo repo**: no se empieza de cero, se extiende lo ya
> construido. Se listan ordenadas por entrega, con el estado real del repo respecto de cada
> requerimiento.

### TP3 · Codificación del prototipo en Java (POO) — entrega 2026-10-19
- **Objetivo:** avanzar la codificación del prototipo aplicando programación orientada a objetos.
- **Entregables:** explicación del desarrollo en Java; presentación del desarrollo en Java;
  el programa compila y se ejecuta correctamente.
- **Características requeridas por la consigna:**
    - Sintaxis, tipos de datos y estructuras de control correctas.
    - Tratamiento y manejo de excepciones.
    - Los cuatro pilares: encapsulamiento, herencia, polimorfismo y abstracción.
    - **Menú de selección** (programa interactivo por consola).
    - Estructuras condicionales y repetitivas.
    - Declaración y creación de objetos; constructores para inicializarlos.
    - Algoritmos de ordenación y búsqueda (opcional).
- **Estado en el repo:** los pilares POO (herencia/polimorfismo en la jerarquía `Vehiculo`,
  encapsulamiento en el dominio, abstracción con `Vehiculo` abstracta) y el manejo de
  excepciones **ya están** (Fases 1-2). El **menú de selección interactivo** y los algoritmos
  de ordenación/búsqueda **ya están** (Fase 6). **Entregado y aprobado 100/100** (Fase 7). La ampliación de
  cobertura funcional más allá de la asignación pasa al TP4.
- **Formato:** PDF A4 Calibri 11, portada, `CABRERA-SERGIO-AP3.PDF`
  (⚠️ la plantilla de la consigna dice literalmente «AP1»; se asume **AP3** por el patrón AP1/AP2 —
  confirmar con la cátedra), enlace GitHub, APA; entrega incremental (correcciones previas).
- **Rúbrica (100):** el código compila/ejecuta (20) · el prototipo cubre las características
  solicitadas (45) · documento explicando las secciones del código (20) · formato y
  correcciones de la entrega anterior (15).
- **Material:** M3L3 · Programación orientada a objetos.

### TP4 · Versión final integradora — entrega 2026-11-09
- **Objetivo:** proyecto integrador final, con un patrón de diseño coherente con la arquitectura.
- **Entregables:** presentación del desarrollo en Java; proyecto integrador **completo**;
  **video de ~3 minutos** presentando el alcance del proyecto.
- **Características requeridas por la consigna:**
    - Corrección de las observaciones de TP1-TP3.
    - **Selección de un patrón de diseño y su justificación.**
    - Persistencia y consulta en MySQL: establecer conexiones, consultar, **actualizar
      registros** y **presentar resultados en la interfaz**.
    - Manejo de excepciones para la interacción con MySQL.
    - Inclusión pertinente de **clases abstractas o interfaces**.
    - Utilización complementaria de **arreglos y `ArrayList`**.
    - Uso de **archivos** para guardar/recuperar información (opcional; recomendado antes del EFIP I).
- **Estado en el repo:** la persistencia MySQL/JDBC con transacción y el manejo de excepciones
  **ya están** (Fase 3), y hay clases abstractas/interfaces (jerarquía `Vehiculo`). **Falta:**
  elegir y **justificar un patrón de diseño** explícito; el **CRUD completo** (alta/consulta/
  actualización) con resultados mostrados en el menú; el uso de **`ArrayList`/arreglos** en la
  capa de aplicación; (opcional) **archivos**; y el **video**.
- **Formato:** ídem, `CABRERA-SERGIO-AP4.PDF` + video (~3 min).
- **Material:** M4L4 · Patrones de diseño / JDBC.

### Incrementalidad (cómo encaja con lo ya hecho)
- TP3 y TP4 se desarrollan sobre `scgl-prototipo-asignacion`; lo construido en TP1/TP2 se
  **reutiliza**, no se rehace. El trabajo nuevo es, en orden: primero el **menú interactivo**
  y el redondeo de POO (TP3); después el **patrón de diseño + CRUD completo + colecciones +
  (archivos) + video** (TP4).
- Regla de oro (motivo de este log): todo artefacto —informe, diagramas, prompts— se deriva
  del estado real del repo. Actualizar este archivo al cerrar cada TP.

---

## Deuda conocida (se resuelve dentro de TP3/TP4)

- **Cobertura funcional parcial:** implementado el núcleo de asignación (RF-04/05/06); el CRUD
  completo y la presentación de resultados en la interfaz → **TP4**.
- **Patrón de diseño:** aún no hay uno elegido/justificado explícitamente → **TP4**.
- **Colecciones (`ArrayList`/arreglos)** en la capa de aplicación: `List`/`ArrayList` ya se
  usan en los listados y en `MenuConsola` (Fase 6); falta sumar arreglos → **TP4**.
- **Archivos** (persistencia complementaria, opcional) → **TP4**.
- **`OrdenMantenimiento` sin clase de dominio** (solo existe la tabla): se suma en TP4
  (Fase 8, C2).
- ~~**Afinar la normalización del DER**~~ → **resuelto en TP4 (Fase 8, C1)**: licencia en
  tabla propia. Texto original: (observación menor del docente en TP2, sin impacto de
  performance a este volumen): a confirmar el detalle con el docente. Candidatos probables:
  llevar catálogos/estados a tablas de referencia en lugar de ENUM, o separar la licencia del
  conductor en su propia tabla. Se complementa en TP3/TP4; no exige reentrega del TP2.
- **Sin pruebas automatizadas:** los `CP-xx` del informe son especificaciones, no JUnit
  (mejora deseable, no exigida por las consignas).
- **Cableado por `new`/`ServiceLoader`:** decisión deliberada por RNF-09; el patrón de diseño
  del TP4 puede reordenar esta parte.
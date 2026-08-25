# Prototipo operacional (sección para el documento)

Conforme a la sección "Formato entregable", se desarrolló un prototipo del sistema. Un
prototipo es "un modelo operacional que incluye solo algunas características del sistema
final" (Kendall & Kendall, 2011); en este caso materializa el módulo de asignación de
viajes, seleccionado por ser el de mayor riesgo y el núcleo de valor del SCGL, en
coherencia con el enfoque del PUD centrado en la arquitectura.

**Qué demuestra.** El prototipo prueba, de extremo a extremo, la transformación central de
la propuesta: reemplazar el cruce manual de tres planillas por una validación automática y
consistente. Ante una solicitud de envío, el sistema empareja vehículo y conductor (RF4) y,
antes de confirmar, verifica automáticamente que el conductor no exceda el límite legal de jornada —fijado
en 44 horas semanales conforme al Convenio Colectivo de Trabajo del transporte de cargas
(art. 30.1), en el marco de la Ley de Jornada 11.544— y posea licencia vigente (RF5), y que el vehículo esté operativo y sin mantenimiento
preventivo pendiente (RF6). Si alguna validación falla, la asignación se rechaza y no se
persiste ninguna operación; si todas se cumplen, la asignación se registra como una
transacción atómica (RNF1).

**Decisiones técnicas.** (1) La lógica se implementó en Java puro orientado a objetos: la
clase abstracta Vehiculo y sus subclases VehiculoPesado y VehiculoLigero aplican herencia y
polimorfismo, y las reglas de negocio quedan encapsuladas en las entidades del dominio
(OE4, RNF8). (2) La persistencia se resolvió con MySQL mediante JDBC, con un modelo
normalizado (RNF2) e integridad referencial por claves foráneas (RNF3); la operación de
asignación se ejecuta con control transaccional explícito (setAutoCommit(false), commit y
rollback) para garantizar la atomicidad (RNF1). (3) El manejo de rechazos se modeló con una
jerarquía de excepciones de dominio, de modo que un dato inválido interrumpe el proceso
antes de tocar la base, materializando el flujo alternativo del CU-02. (4) En coherencia con
la justificación técnica, no se emplearon frameworks de alto nivel: la única dependencia es
el driver JDBC (Connector/J), que no constituye un framework (RNF9).

**Carácter incremental.** Al ser un modelo operacional y no una maqueta descartable, sus
módulos se integran progresivamente hacia la versión final, en línea con el carácter
iterativo e incremental del PUD.

**Repositorio.** El código fuente completo y el esquema de base de datos están disponibles
en: [ENLACE GITHUB].

Referencia: Kendall, K., & Kendall, J. (2011). *Análisis y diseño de sistemas*. Pearson Education.

-- =====================================================================
-- SCGL  |  Consultas SQL de explotacion del modelo. Ejecutar despues
-- de schema.sql y datos.sql.
-- =====================================================================
USE scgl;

-- Consulta 1 - Historial unificado de asignaciones (RF10 / CU-07):
-- une asignacion, solicitud, vehiculo y conductor en una sola vista.
SELECT a.id_asignacion                       AS asignacion,
       CONCAT(s.origen, ' -> ', s.destino)   AS ruta,
       v.patente                             AS vehiculo,
       c.nombre                              AS conductor,
       a.fecha_hora                          AS fecha,
       a.estado                              AS estado
FROM asignacion a
JOIN solicitud_envio s ON a.id_solicitud = s.id_solicitud
JOIN vehiculo v        ON a.id_vehiculo  = v.id_vehiculo
JOIN conductor c       ON a.id_conductor = c.id_conductor;

-- Consulta 2 - Solicitudes pendientes de asignacion (RF3 / CU-02).
SELECT id_solicitud, origen, destino, peso_kg, ventana_inicio, ventana_fin
FROM solicitud_envio
WHERE estado = 'PENDIENTE'
ORDER BY ventana_inicio;

-- Consulta 3 - Vehiculos que requieren atencion (RF8 / RN1 / RN4):
-- no operativos, o con km desde el ultimo mantenimiento sobre el umbral.
SELECT patente, tipo, estado,
       km_actual, km_ultimo_mantenimiento, umbral_mantenimiento_km,
       (km_actual - km_ultimo_mantenimiento) AS km_desde_ultimo
FROM vehiculo
WHERE estado <> 'OPERATIVO'
   OR (km_actual - km_ultimo_mantenimiento) >= umbral_mantenimiento_km;

-- Consulta 4 - Indicador de gestion: asignaciones por conductor (RF11 / CU-08).
SELECT c.nombre                    AS conductor,
       COUNT(a.id_asignacion)      AS asignaciones
FROM conductor c
LEFT JOIN asignacion a ON a.id_conductor = c.id_conductor
GROUP BY c.id_conductor, c.nombre
ORDER BY asignaciones DESC;

-- Consulta 5 - Ordenes de mantenimiento abiertas y su vehiculo (RF9 / CU-06).
SELECT o.id_orden, v.patente, o.tipo, o.estado, o.fecha
FROM orden_mantenimiento o
JOIN vehiculo v ON o.id_vehiculo = v.id_vehiculo
WHERE o.estado = 'ABIERTA';

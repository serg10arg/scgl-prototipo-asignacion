-- =====================================================================
-- SCGL  |  Borrado de registros y verificacion de integridad referencial.
-- Ejecutar despues de schema.sql y datos.sql.
-- =====================================================================
USE scgl;

-- Borrado de una solicitud pendiente no asignada (id 2).
DELETE FROM solicitud_envio WHERE id_solicitud = 2;

-- Verificacion: la solicitud 2 ya no existe.
SELECT id_solicitud, origen, destino, estado
FROM solicitud_envio
ORDER BY id_solicitud;

-- Integridad referencial (RNF3): el motor impide borrar un vehiculo
-- referenciado por una orden de mantenimiento. La siguiente sentencia
-- debe FALLAR con error 1451 (foreign key constraint), evitando datos
-- huerfanos. Se deja documentada como evidencia.
-- DELETE FROM vehiculo WHERE id_vehiculo = 2;

-- =====================================================================
-- SCGL  |  Datos de demostracion (para ejercitar los 3 escenarios de la
-- App y las consultas SQL). Ejecutar despues de schema.sql.
-- =====================================================================
USE scgl;

-- Conductores
INSERT INTO conductor (nombre, num_licencia, tipo_licencia, fecha_venc_licencia, horas_acumuladas) VALUES
    ('Ana Gomez',   'LIC-1001', 'E1', '2027-12-31', 20.00),  -- id 1: apto
    ('Luis Pereyra','LIC-1002', 'E1', '2027-06-30', 40.00);  -- id 2: 40 h; +6 h supera el limite de 44 h (RN2)

-- Vehiculos
INSERT INTO vehiculo (patente, tipo, capacidad_kg, km_actual, km_ultimo_mantenimiento, umbral_mantenimiento_km, estado) VALUES
    ('AA123BB', 'PESADO', 20000.00, 145000, 140000, 10000, 'OPERATIVO'),  -- id 1: operativo, sin mant.
    ('CC456DD', 'PESADO', 20000.00, 152000, 140000, 10000, 'OPERATIVO');  -- id 2: 12k km >= 10k -> requiere mant. (RN4)

-- Una orden preventiva abierta coherente con el vehiculo 2
INSERT INTO orden_mantenimiento (id_vehiculo, tipo, estado, fecha) VALUES
    (2, 'PREVENTIVO', 'ABIERTA', CURRENT_DATE);

-- Solicitudes
INSERT INTO solicitud_envio (origen, destino, peso_kg, ventana_inicio, ventana_fin) VALUES
    ('Mendoza','Cordoba', 15000.00, '2026-08-15 08:00:00', '2026-08-15 20:00:00'),  -- id 1
    ('Mendoza','Rosario', 15000.00, '2026-08-16 08:00:00', '2026-08-16 22:00:00');  -- id 2

-- Asignacion valida del escenario A (solicitud 1 + vehiculo 1 + conductor 1),
-- equivalente a la que persiste MotorAsignacion (RF4 / RNF1). Deja datos para
-- ejercitar las consultas del historial y los indicadores.
INSERT INTO asignacion (id_solicitud, id_vehiculo, id_conductor, fecha_hora, horas_estimadas, estado) VALUES
    (1, 1, 1, '2026-08-15 06:00:00', 6.00, 'CONFIRMADA');
UPDATE solicitud_envio SET estado = 'ASIGNADA' WHERE id_solicitud = 1;

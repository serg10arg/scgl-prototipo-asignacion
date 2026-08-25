CREATE DATABASE IF NOT EXISTS scgl CHARACTER SET utf8mb4;
USE scgl;

DROP TABLE IF EXISTS asignacion;
DROP TABLE IF EXISTS orden_mantenimiento;
DROP TABLE IF EXISTS solicitud_envio;
DROP TABLE IF EXISTS vehiculo;
DROP TABLE IF EXISTS conductor;

-- Conductores (RF2). horas_acumuladas es la base de la regla RN2.
CREATE TABLE conductor (
                           id_conductor        INT AUTO_INCREMENT PRIMARY KEY,
                           nombre              VARCHAR(120)  NOT NULL,
                           num_licencia        VARCHAR(40)   NOT NULL UNIQUE,
                           tipo_licencia       VARCHAR(10)   NOT NULL,          -- p.ej. 'E1' pesados
                           fecha_venc_licencia DATE          NOT NULL,          -- RN3
                           horas_acumuladas    DECIMAL(5,2)  NOT NULL DEFAULT 0 -- RN2 (jornada semanal; limite 44 h - CCT cargas)
) ENGINE=InnoDB;

-- Vehiculos (RF1). La columna 'tipo' actua como discriminador de herencia
-- (mapea a VehiculoPesado / VehiculoLigero en la capa de dominio).
CREATE TABLE vehiculo (
                          id_vehiculo             INT AUTO_INCREMENT PRIMARY KEY,
                          patente                 VARCHAR(15)  NOT NULL UNIQUE,
                          tipo                    ENUM('PESADO','LIGERO') NOT NULL,
                          capacidad_kg            DECIMAL(10,2) NOT NULL,       -- RN5
                          km_actual               INT NOT NULL DEFAULT 0,       -- RN8
                          km_ultimo_mantenimiento INT NOT NULL DEFAULT 0,
                          umbral_mantenimiento_km INT NOT NULL,                 -- RN4 [SUPUESTO: por tipo de unidad]
                          estado                  ENUM('OPERATIVO','EN_TALLER','MANT_PENDIENTE')
                            NOT NULL DEFAULT 'OPERATIVO'  -- RN1
) ENGINE=InnoDB;

-- Solicitudes de envio (RF3).
CREATE TABLE solicitud_envio (
                                 id_solicitud    INT AUTO_INCREMENT PRIMARY KEY,
                                 origen          VARCHAR(120) NOT NULL,
                                 destino         VARCHAR(120) NOT NULL,
                                 peso_kg         DECIMAL(10,2) NOT NULL,               -- RN5
                                 ventana_inicio  DATETIME NOT NULL,                    -- RN7 [SUPUESTO: SLA]
                                 ventana_fin     DATETIME NOT NULL,
                                 estado          ENUM('PENDIENTE','ASIGNADA','CERRADA')
                    NOT NULL DEFAULT 'PENDIENTE'
) ENGINE=InnoDB;

-- Ordenes de mantenimiento (RF9 / CU-06). Sostienen el estado del vehiculo (RN1).
CREATE TABLE orden_mantenimiento (
                                     id_orden    INT AUTO_INCREMENT PRIMARY KEY,
                                     id_vehiculo INT NOT NULL,
                                     tipo        ENUM('PREVENTIVO','CORRECTIVO') NOT NULL,
                                     estado      ENUM('ABIERTA','CERRADA') NOT NULL DEFAULT 'ABIERTA',
                                     fecha       DATE NOT NULL,
                                     CONSTRAINT fk_om_vehiculo FOREIGN KEY (id_vehiculo)
                                         REFERENCES vehiculo(id_vehiculo)
) ENGINE=InnoDB;

-- Asignaciones confirmadas (RF4). Solo se persisten las asignaciones validas.
CREATE TABLE asignacion (
                            id_asignacion   INT AUTO_INCREMENT PRIMARY KEY,
                            id_solicitud    INT NOT NULL,
                            id_vehiculo     INT NOT NULL,
                            id_conductor    INT NOT NULL,
                            fecha_hora      DATETIME NOT NULL,
                            horas_estimadas DECIMAL(5,2) NOT NULL,
                            estado          ENUM('CONFIRMADA','CERRADA') NOT NULL DEFAULT 'CONFIRMADA',
                            CONSTRAINT fk_asig_solicitud FOREIGN KEY (id_solicitud)
                                REFERENCES solicitud_envio(id_solicitud),
                            CONSTRAINT fk_asig_vehiculo  FOREIGN KEY (id_vehiculo)
                                REFERENCES vehiculo(id_vehiculo),
                            CONSTRAINT fk_asig_conductor FOREIGN KEY (id_conductor)
                                REFERENCES conductor(id_conductor)
) ENGINE=InnoDB;

-- =====================================================================
-- Datos de demostracion (para ejercitar los 3 escenarios de la App)
-- =====================================================================

-- Conductores
INSERT INTO conductor (nombre, num_licencia, tipo_licencia, fecha_venc_licencia, horas_acumuladas) VALUES
                                                                                                       ('Ana Gomez',   'LIC-1001', 'E1', '2027-12-31', 20.00),  -- id 1: apto
                                                                                                       ('Luis Pereyra','LIC-1002', 'E1', '2027-06-30', 40.00);  -- id 2: 40 h en la semana; +6 h supera el limite de 44 h (RN2)

-- Vehiculos
INSERT INTO vehiculo (patente, tipo, capacidad_kg, km_actual, km_ultimo_mantenimiento, umbral_mantenimiento_km, estado) VALUES
                                                                                                                            ('AA123BB', 'PESADO', 20000.00, 145000, 140000, 10000, 'OPERATIVO'),      -- id 1: operativo, sin mant.
                                                                                                                            ('CC456DD', 'PESADO', 20000.00, 152000, 140000, 10000, 'OPERATIVO');      -- id 2: km-140k=12k >= 10k -> requiere mant. (RN4)

-- Una orden preventiva abierta coherente con el vehiculo 2
INSERT INTO orden_mantenimiento (id_vehiculo, tipo, estado, fecha) VALUES
    (2, 'PREVENTIVO', 'ABIERTA', CURRENT_DATE);

-- Solicitudes
INSERT INTO solicitud_envio (origen, destino, peso_kg, ventana_inicio, ventana_fin) VALUES
                                                                                        ('Mendoza','Cordoba', 15000.00, '2026-08-15 08:00:00', '2026-08-15 20:00:00'), -- id 1
                                                                                        ('Mendoza','Rosario', 15000.00, '2026-08-16 08:00:00', '2026-08-16 22:00:00'); -- id 2

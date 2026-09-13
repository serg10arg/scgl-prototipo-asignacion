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

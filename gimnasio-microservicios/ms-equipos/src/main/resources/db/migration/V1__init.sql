CREATE TABLE IF NOT EXISTS equipos (
    id                BIGSERIAL PRIMARY KEY,
    nombre            VARCHAR(80)  NOT NULL,
    tipo              VARCHAR(50)  NOT NULL,
    codigo_interno    VARCHAR(30)  NOT NULL UNIQUE,
    sucursal_id       BIGINT       NOT NULL,
    fecha_adquisicion DATE,
    estado            VARCHAR(20)  NOT NULL CHECK (estado IN ('OPERATIVO','EN_MANTENIMIENTO','DADO_DE_BAJA'))
);

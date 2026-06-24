CREATE TABLE IF NOT EXISTS asistencias (
    id             BIGSERIAL PRIMARY KEY,
    socio_id       BIGINT     NOT NULL,
    sucursal_id    BIGINT     NOT NULL,
    fecha_ingreso  TIMESTAMP  NOT NULL,
    fecha_salida   TIMESTAMP
);

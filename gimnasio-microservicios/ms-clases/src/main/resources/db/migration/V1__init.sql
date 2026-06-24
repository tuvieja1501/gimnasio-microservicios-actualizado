CREATE TABLE IF NOT EXISTS clases (
    id                BIGSERIAL PRIMARY KEY,
    nombre            VARCHAR(80)  NOT NULL,
    descripcion       VARCHAR(250),
    instructor_id     BIGINT       NOT NULL,
    sucursal_id       BIGINT       NOT NULL,
    fecha_hora        TIMESTAMP    NOT NULL,
    duracion_minutos  INTEGER      NOT NULL CHECK (duracion_minutos BETWEEN 15 AND 180),
    cupo_maximo       INTEGER      NOT NULL CHECK (cupo_maximo > 0),
    cupos_disponibles INTEGER      NOT NULL CHECK (cupos_disponibles >= 0)
);

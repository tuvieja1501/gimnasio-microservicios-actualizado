CREATE TABLE IF NOT EXISTS rutinas (
    id                BIGSERIAL PRIMARY KEY,
    nombre            VARCHAR(100) NOT NULL,
    objetivo          VARCHAR(250),
    socio_id          BIGINT       NOT NULL,
    instructor_id     BIGINT       NOT NULL,
    fecha_creacion    DATE         NOT NULL,
    duracion_semanas  INTEGER      NOT NULL CHECK (duracion_semanas BETWEEN 1 AND 52)
);

CREATE TABLE IF NOT EXISTS ejercicios (
    id                BIGSERIAL PRIMARY KEY,
    nombre            VARCHAR(100) NOT NULL,
    series            INTEGER      NOT NULL CHECK (series > 0),
    repeticiones      INTEGER      NOT NULL CHECK (repeticiones > 0),
    descanso_segundos INTEGER      CHECK (descanso_segundos >= 0),
    observaciones     VARCHAR(250),
    rutina_id         BIGINT       NOT NULL,
    CONSTRAINT fk_ejercicio_rutina FOREIGN KEY (rutina_id) REFERENCES rutinas(id) ON DELETE CASCADE
);

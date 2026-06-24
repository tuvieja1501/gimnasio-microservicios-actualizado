CREATE TABLE IF NOT EXISTS reservas (
    id              BIGSERIAL PRIMARY KEY,
    socio_id        BIGINT       NOT NULL,
    clase_id        BIGINT       NOT NULL,
    fecha_reserva   TIMESTAMP    NOT NULL,
    estado          VARCHAR(20)  NOT NULL CHECK (estado IN ('CONFIRMADA', 'CANCELADA', 'ASISTIDA')),
    CONSTRAINT uq_socio_clase UNIQUE (socio_id, clase_id)
);

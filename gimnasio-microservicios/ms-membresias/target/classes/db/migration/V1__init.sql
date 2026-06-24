-- =============================================================
-- Migracion inicial - ms-membresias
-- =============================================================

CREATE TABLE IF NOT EXISTS planes_membresia (
    id              BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(80)    NOT NULL,
    descripcion     VARCHAR(250),
    duracion_meses  INTEGER        NOT NULL,
    precio          NUMERIC(10,2)  NOT NULL CHECK (precio > 0),
    activo          BOOLEAN        NOT NULL
);

CREATE TABLE IF NOT EXISTS membresias (
    id             BIGSERIAL PRIMARY KEY,
    socio_id       BIGINT       NOT NULL,
    plan_id        BIGINT       NOT NULL,
    fecha_inicio   DATE         NOT NULL,
    fecha_fin      DATE         NOT NULL,
    estado         VARCHAR(20)  NOT NULL CHECK (estado IN ('VIGENTE', 'VENCIDA', 'CANCELADA')),
    CONSTRAINT fk_membresia_plan FOREIGN KEY (plan_id) REFERENCES planes_membresia(id)
);

INSERT INTO planes_membresia (nombre, descripcion, duracion_meses, precio, activo) VALUES
('Mensual Basico',     'Acceso libre a maquinas, sin clases grupales',           1, 25000,  TRUE),
('Trimestral Premium', 'Acceso libre + clases grupales ilimitadas',              3, 65000,  TRUE),
('Anual VIP',          'Todo incluido + 2 sesiones mensuales con personal trainer', 12, 220000, TRUE);

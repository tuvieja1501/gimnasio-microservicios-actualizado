-- =============================================================
-- Migracion inicial - ms-socios
-- Crea la tabla socios y carga datos de ejemplo.
-- Si Hibernate ya creo el schema (ddl-auto=update), este script
-- se puede ejecutar manualmente para poblar datos de prueba.
-- =============================================================

CREATE TABLE IF NOT EXISTS socios (
    id                BIGSERIAL PRIMARY KEY,
    nombre            VARCHAR(80)  NOT NULL,
    apellido          VARCHAR(80)  NOT NULL,
    rut               VARCHAR(12)  NOT NULL UNIQUE,
    email             VARCHAR(120) NOT NULL UNIQUE,
    telefono          VARCHAR(20),
    fecha_nacimiento  DATE,
    fecha_registro    DATE         NOT NULL,
    estado            VARCHAR(20)  NOT NULL CHECK (estado IN ('ACTIVO', 'INACTIVO', 'MOROSO'))
);

INSERT INTO socios (nombre, apellido, rut, email, telefono, fecha_nacimiento, fecha_registro, estado)
VALUES
('Camila',  'Rojas',     '18234567-2', 'camila.rojas@mail.cl',  '+56912345678', '1998-03-15', CURRENT_DATE, 'ACTIVO'),
('Diego',   'Fernandez', '19876543-1', 'diego.fernandez@mail.cl', '+56987654321', '2000-07-22', CURRENT_DATE, 'ACTIVO'),
('Valentina','Soto',     '17654321-K', 'valentina.soto@mail.cl', '+56923456789', '1995-11-30', CURRENT_DATE, 'MOROSO')
ON CONFLICT (rut) DO NOTHING;

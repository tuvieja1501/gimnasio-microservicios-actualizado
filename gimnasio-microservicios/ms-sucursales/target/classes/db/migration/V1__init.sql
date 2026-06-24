CREATE TABLE IF NOT EXISTS sucursales (
    id              BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(80)  NOT NULL UNIQUE,
    direccion       VARCHAR(200) NOT NULL,
    comuna          VARCHAR(80)  NOT NULL,
    telefono        VARCHAR(20),
    capacidad       INTEGER      NOT NULL CHECK (capacidad > 0),
    hora_apertura   TIME         NOT NULL,
    hora_cierre     TIME         NOT NULL,
    activa          BOOLEAN      NOT NULL,
    CONSTRAINT chk_horario CHECK (hora_apertura < hora_cierre)
);

INSERT INTO sucursales (nombre, direccion, comuna, telefono, capacidad, hora_apertura, hora_cierre, activa) VALUES
('Sucursal Providencia', 'Av. Providencia 1234',     'Providencia', '+56222345678', 120, '06:00', '23:00', TRUE),
('Sucursal Las Condes',  'Av. Apoquindo 4500',       'Las Condes',  '+56223456789', 200, '06:30', '23:30', TRUE),
('Sucursal Maipu',       'Av. Pajaritos 2100',       'Maipu',       '+56224567890', 80,  '07:00', '22:00', TRUE)
ON CONFLICT (nombre) DO NOTHING;

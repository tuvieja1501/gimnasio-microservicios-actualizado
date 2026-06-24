CREATE TABLE IF NOT EXISTS instructores (
    id                 BIGSERIAL PRIMARY KEY,
    nombre             VARCHAR(80)  NOT NULL,
    apellido           VARCHAR(80)  NOT NULL,
    rut                VARCHAR(12)  NOT NULL UNIQUE,
    email              VARCHAR(120) NOT NULL UNIQUE,
    especialidad       VARCHAR(80)  NOT NULL,
    anios_experiencia  INTEGER      NOT NULL CHECK (anios_experiencia >= 0),
    activo             BOOLEAN      NOT NULL
);

INSERT INTO instructores (nombre, apellido, rut, email, especialidad, anios_experiencia, activo) VALUES
('Andres',  'Castro',   '15234567-8', 'andres.castro@gym.cl',   'Spinning',   8,  TRUE),
('Paula',   'Munoz',    '16345678-9', 'paula.munoz@gym.cl',     'Yoga',       5,  TRUE),
('Tomas',   'Vidal',    '17456789-0', 'tomas.vidal@gym.cl',     'Crossfit',   6,  TRUE)
ON CONFLICT (rut) DO NOTHING;

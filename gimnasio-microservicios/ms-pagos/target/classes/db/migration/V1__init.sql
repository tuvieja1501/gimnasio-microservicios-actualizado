CREATE TABLE IF NOT EXISTS pagos (
    id            BIGSERIAL PRIMARY KEY,
    socio_id      BIGINT        NOT NULL,
    membresia_id  BIGINT        NOT NULL,
    monto         NUMERIC(10,2) NOT NULL CHECK (monto > 0),
    metodo_pago   VARCHAR(30)   NOT NULL CHECK (metodo_pago IN ('EFECTIVO','TARJETA_CREDITO','TARJETA_DEBITO','TRANSFERENCIA')),
    fecha_pago    TIMESTAMP     NOT NULL,
    estado        VARCHAR(20)   NOT NULL CHECK (estado IN ('PAGADO','PENDIENTE','ANULADO')),
    referencia    VARCHAR(80)
);

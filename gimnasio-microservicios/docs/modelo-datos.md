# Modelo de Datos

Cada microservicio posee su propia base de datos PostgreSQL **independiente**, siguiendo el principio de Database-per-Service.

---

## ms-socios → `gimnasio_socios`

### Tabla `socios`
| Columna | Tipo | Restricciones |
|---------|------|---------------|
| id | BIGSERIAL | PK |
| nombre | VARCHAR(80) | NOT NULL |
| apellido | VARCHAR(80) | NOT NULL |
| rut | VARCHAR(12) | NOT NULL, UNIQUE |
| email | VARCHAR(120) | NOT NULL, UNIQUE |
| telefono | VARCHAR(20) | - |
| fecha_nacimiento | DATE | - |
| fecha_registro | DATE | NOT NULL |
| estado | VARCHAR(20) | NOT NULL, CHECK IN (ACTIVO, INACTIVO, MOROSO) |

---

## ms-membresias → `gimnasio_membresias`

### Tabla `planes_membresia`
| Columna | Tipo | Restricciones |
|---------|------|---------------|
| id | BIGSERIAL | PK |
| nombre | VARCHAR(80) | NOT NULL |
| descripcion | VARCHAR(250) | - |
| duracion_meses | INTEGER | NOT NULL |
| precio | NUMERIC(10,2) | NOT NULL, > 0 |
| activo | BOOLEAN | NOT NULL |

### Tabla `membresias`
| Columna | Tipo | Restricciones |
|---------|------|---------------|
| id | BIGSERIAL | PK |
| socio_id | BIGINT | NOT NULL (ref. lógica a ms-socios) |
| plan_id | BIGINT | NOT NULL, **FK → planes_membresia(id)** |
| fecha_inicio | DATE | NOT NULL |
| fecha_fin | DATE | NOT NULL |
| estado | VARCHAR(20) | CHECK IN (VIGENTE, VENCIDA, CANCELADA) |

**Relación JPA:** `PlanMembresia (1) ←→ (N) Membresia` con `@OneToMany` / `@ManyToOne`.

---

## ms-instructores → `gimnasio_instructores`

### Tabla `instructores`
| Columna | Tipo | Restricciones |
|---------|------|---------------|
| id | BIGSERIAL | PK |
| nombre | VARCHAR(80) | NOT NULL |
| apellido | VARCHAR(80) | NOT NULL |
| rut | VARCHAR(12) | NOT NULL, UNIQUE |
| email | VARCHAR(120) | NOT NULL, UNIQUE |
| especialidad | VARCHAR(80) | NOT NULL |
| anios_experiencia | INTEGER | NOT NULL, >= 0 |
| activo | BOOLEAN | NOT NULL |

---

## ms-clases → `gimnasio_clases`

### Tabla `clases`
| Columna | Tipo | Restricciones |
|---------|------|---------------|
| id | BIGSERIAL | PK |
| nombre | VARCHAR(80) | NOT NULL |
| descripcion | VARCHAR(250) | - |
| instructor_id | BIGINT | NOT NULL (ref. lógica a ms-instructores) |
| sucursal_id | BIGINT | NOT NULL (ref. lógica a ms-sucursales) |
| fecha_hora | TIMESTAMP | NOT NULL |
| duracion_minutos | INTEGER | NOT NULL, 15-180 |
| cupo_maximo | INTEGER | NOT NULL, > 0 |
| cupos_disponibles | INTEGER | NOT NULL, >= 0 |

---

## ms-reservas → `gimnasio_reservas`

### Tabla `reservas`
| Columna | Tipo | Restricciones |
|---------|------|---------------|
| id | BIGSERIAL | PK |
| socio_id | BIGINT | NOT NULL |
| clase_id | BIGINT | NOT NULL |
| fecha_reserva | TIMESTAMP | NOT NULL |
| estado | VARCHAR(20) | CHECK IN (CONFIRMADA, CANCELADA, ASISTIDA) |

**Restricción única compuesta:** `UNIQUE (socio_id, clase_id)` — un socio no puede reservar dos veces la misma clase.

---

## ms-pagos → `gimnasio_pagos`

### Tabla `pagos`
| Columna | Tipo | Restricciones |
|---------|------|---------------|
| id | BIGSERIAL | PK |
| socio_id | BIGINT | NOT NULL |
| membresia_id | BIGINT | NOT NULL |
| monto | NUMERIC(10,2) | NOT NULL, > 0 |
| metodo_pago | VARCHAR(30) | CHECK IN (EFECTIVO, TARJETA_CREDITO, TARJETA_DEBITO, TRANSFERENCIA) |
| fecha_pago | TIMESTAMP | NOT NULL |
| estado | VARCHAR(20) | CHECK IN (PAGADO, PENDIENTE, ANULADO) |
| referencia | VARCHAR(80) | - |

---

## ms-equipos → `gimnasio_equipos`

### Tabla `equipos`
| Columna | Tipo | Restricciones |
|---------|------|---------------|
| id | BIGSERIAL | PK |
| nombre | VARCHAR(80) | NOT NULL |
| tipo | VARCHAR(50) | NOT NULL |
| codigo_interno | VARCHAR(30) | NOT NULL, UNIQUE |
| sucursal_id | BIGINT | NOT NULL |
| fecha_adquisicion | DATE | - |
| estado | VARCHAR(20) | CHECK IN (OPERATIVO, EN_MANTENIMIENTO, DADO_DE_BAJA) |

---

## ms-rutinas → `gimnasio_rutinas`

### Tabla `rutinas`
| Columna | Tipo | Restricciones |
|---------|------|---------------|
| id | BIGSERIAL | PK |
| nombre | VARCHAR(100) | NOT NULL |
| objetivo | VARCHAR(250) | - |
| socio_id | BIGINT | NOT NULL |
| instructor_id | BIGINT | NOT NULL |
| fecha_creacion | DATE | NOT NULL |
| duracion_semanas | INTEGER | NOT NULL, 1-52 |

### Tabla `ejercicios`
| Columna | Tipo | Restricciones |
|---------|------|---------------|
| id | BIGSERIAL | PK |
| nombre | VARCHAR(100) | NOT NULL |
| series | INTEGER | NOT NULL, > 0 |
| repeticiones | INTEGER | NOT NULL, > 0 |
| descanso_segundos | INTEGER | >= 0 |
| observaciones | VARCHAR(250) | - |
| rutina_id | BIGINT | **FK → rutinas(id) ON DELETE CASCADE** |

**Relación JPA:** `Rutina (1) ←→ (N) Ejercicio` con `@OneToMany(cascade=ALL, orphanRemoval=true)` y `@ManyToOne` en el lado de `Ejercicio`. Esta relación demuestra **integridad referencial** dentro del microservicio.

---

## ms-asistencias → `gimnasio_asistencias`

### Tabla `asistencias`
| Columna | Tipo | Restricciones |
|---------|------|---------------|
| id | BIGSERIAL | PK |
| socio_id | BIGINT | NOT NULL |
| sucursal_id | BIGINT | NOT NULL |
| fecha_ingreso | TIMESTAMP | NOT NULL |
| fecha_salida | TIMESTAMP | NULL = ingreso abierto |

---

## ms-sucursales → `gimnasio_sucursales`

### Tabla `sucursales`
| Columna | Tipo | Restricciones |
|---------|------|---------------|
| id | BIGSERIAL | PK |
| nombre | VARCHAR(80) | NOT NULL, UNIQUE |
| direccion | VARCHAR(200) | NOT NULL |
| comuna | VARCHAR(80) | NOT NULL |
| telefono | VARCHAR(20) | - |
| capacidad | INTEGER | NOT NULL, > 0 |
| hora_apertura | TIME | NOT NULL |
| hora_cierre | TIME | NOT NULL |
| activa | BOOLEAN | NOT NULL |

CHECK constraint: `hora_apertura < hora_cierre`.

---

## Decisiones de modelado

1. **Normalización:** todas las tablas están en 3FN. No hay grupos repetidos, dependencias parciales ni transitivas.
2. **Claves foráneas físicas** existen únicamente dentro de un mismo microservicio (ej. `ejercicios.rutina_id → rutinas.id`, `membresias.plan_id → planes_membresia.id`).
3. **Referencias lógicas entre microservicios** (ej. `reservas.socio_id`) no usan FK SQL, porque las tablas viven en bases de datos diferentes. La integridad referencial se garantiza en la **capa de servicio** validando contra el microservicio remoto vía Feign Client antes de persistir.
4. **Enums** se persisten como `VARCHAR` con `CHECK` constraint, lo que permite legibilidad directa en la BD y evita acoplar el esquema a valores numéricos.
5. **Auditoría mínima:** los timestamps clave (`fecha_registro`, `fecha_pago`, `fecha_ingreso`, etc.) son `NOT NULL` y se asignan en la capa de servicio al crear el recurso.

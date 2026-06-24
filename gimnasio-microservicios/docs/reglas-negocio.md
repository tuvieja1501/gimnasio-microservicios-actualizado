# Reglas de Negocio Implementadas

Cada microservicio aplica reglas específicas en su **capa de servicio**, lanzando `ReglaNegocioException` (HTTP 409 CONFLICT) cuando una operación viola una restricción del dominio. Las validaciones de formato/obligatoriedad se hacen con **Bean Validation** en los DTOs (HTTP 400 BAD_REQUEST).

---

## ms-socios

| # | Regla | Mecanismo |
|---|-------|-----------|
| RN-S1 | El RUT del socio es único en el sistema | `repo.existsByRut()` antes de crear/actualizar |
| RN-S2 | El email del socio es único | `repo.existsByEmail()` |
| RN-S3 | Un socio debe tener mínimo 14 años | Cálculo de edad con `Period.between` en el service |
| RN-S4 | El estado solo puede ser ACTIVO, INACTIVO o MOROSO | Enum `EstadoSocio` + CHECK en BD |
| RN-S5 | La fecha de nacimiento debe ser pasada | `@Past` en el DTO |
| RN-S6 | El RUT debe respetar formato chileno | `@Pattern("^[0-9]{7,8}-[0-9kK]$")` |

---

## ms-membresias

| # | Regla | Mecanismo |
|---|-------|-----------|
| RN-M1 | Solo se puede crear una membresía si el socio existe en ms-socios | Feign `SocioClient.obtenerSocio()` |
| RN-M2 | El socio debe estar en estado ACTIVO para tomar una membresía | Validación post-Feign |
| RN-M3 | Un socio no puede tener dos membresías VIGENTES simultáneamente | `repo.existsBySocioIdAndEstado(VIGENTE)` |
| RN-M4 | El plan debe estar activo para asignarlo | Check `plan.activo == true` |
| RN-M5 | `fecha_fin` se calcula automáticamente como `fecha_inicio + duracion_meses` | En el service, no se acepta del cliente |
| RN-M6 | Solo se pueden cancelar membresías VIGENTES | Check en `cancelar()` |
| RN-M7 | La duración del plan está entre 1 y 36 meses | `@Min(1)` `@Max(36)` en DTO |

---

## ms-instructores

| # | Regla | Mecanismo |
|---|-------|-----------|
| RN-I1 | RUT único | `existsByRut()` |
| RN-I2 | Email único | `existsByEmail()` |
| RN-I3 | Años de experiencia >= 0 y <= 60 | `@Min(0) @Max(60)` |

---

## ms-clases

| # | Regla | Mecanismo |
|---|-------|-----------|
| RN-C1 | El instructor debe existir y estar activo | Feign `InstructorClient` + check `activo` |
| RN-C2 | La sucursal debe existir y estar activa | Feign `SucursalClient` + check `activa` |
| RN-C3 | La clase debe agendarse a futuro | `@Future` en DTO |
| RN-C4 | El cupo de la clase no puede exceder la capacidad de la sucursal | Comparación contra `sucursal.capacidad` |
| RN-C5 | Duración entre 15 y 180 minutos | `@Min(15) @Max(180)` |
| RN-C6 | `cupos_disponibles` no puede ser negativo | CHECK en BD + lógica de decremento |

---

## ms-reservas

| # | Regla | Mecanismo |
|---|-------|-----------|
| RN-R1 | El socio debe existir y estar ACTIVO | Feign `SocioClient` |
| RN-R2 | El socio debe tener una membresía VIGENTE | Feign `MembresiaClient.tieneVigente()` |
| RN-R3 | La clase debe existir | Feign `ClaseClient` |
| RN-R4 | La clase no puede haber ocurrido (fechaHora > now) | Check en service |
| RN-R5 | Debe haber cupos disponibles | Check `cuposDisponibles > 0` |
| RN-R6 | Un socio no puede reservar la misma clase dos veces | Unique constraint `(socio_id, clase_id)` + check previo |
| RN-R7 | Al confirmar, se decrementa el cupo en ms-clases | Feign `claseClient.decrementarCupo()` |
| RN-R8 | Al cancelar una reserva CONFIRMADA, se devuelve el cupo | Feign `claseClient.incrementarCupo()` |
| RN-R9 | Solo reservas CONFIRMADAS se pueden marcar como ASISTIDA | Check de estado |

---

## ms-pagos

| # | Regla | Mecanismo |
|---|-------|-----------|
| RN-P1 | El socio debe existir | Feign `SocioClient` |
| RN-P2 | El monto debe ser > 0 | `@DecimalMin("0.0", inclusive=false)` |
| RN-P3 | El método de pago debe ser uno válido | Enum + CHECK |
| RN-P4 | Un pago anulado no se puede anular nuevamente | Check de estado |
| RN-P5 | `fecha_pago` se asigna en el servidor, no se acepta del cliente | En el service |

---

## ms-equipos

| # | Regla | Mecanismo |
|---|-------|-----------|
| RN-E1 | El código interno es único | `existsByCodigoInterno()` |
| RN-E2 | La sucursal asociada debe existir y estar activa | Feign `SucursalClient` |
| RN-E3 | La fecha de adquisición no puede ser futura | `@PastOrPresent` |
| RN-E4 | El estado solo puede ser OPERATIVO, EN_MANTENIMIENTO o DADO_DE_BAJA | Enum + CHECK |

---

## ms-rutinas

| # | Regla | Mecanismo |
|---|-------|-----------|
| RN-RU1 | El socio debe existir y estar ACTIVO | Feign `SocioClient` |
| RN-RU2 | El instructor debe existir y estar activo | Feign `InstructorClient` |
| RN-RU3 | Una rutina debe tener al menos un ejercicio | `@Size(min=1)` en la lista |
| RN-RU4 | Series y repeticiones > 0 | `@Min(1)` |
| RN-RU5 | Descanso entre 0 y 600 segundos | `@Min(0) @Max(600)` |
| RN-RU6 | Duración entre 1 y 52 semanas | `@Min(1) @Max(52)` |
| RN-RU7 | Al eliminar una rutina, se eliminan sus ejercicios en cascada | `orphanRemoval=true` + `ON DELETE CASCADE` |

---

## ms-asistencias

| # | Regla | Mecanismo |
|---|-------|-----------|
| RN-A1 | El socio debe existir y estar ACTIVO | Feign `SocioClient` |
| RN-A2 | La sucursal debe existir y estar activa | Feign `SucursalClient` |
| RN-A3 | Un socio no puede tener dos ingresos abiertos al mismo tiempo | Check `fechaSalida IS NULL` |
| RN-A4 | Para registrar salida, debe existir un ingreso abierto previo | Búsqueda obligatoria |
| RN-A5 | `fecha_ingreso` y `fecha_salida` se asignan server-side | En el service |

---

## ms-sucursales

| # | Regla | Mecanismo |
|---|-------|-----------|
| RN-SU1 | El nombre de la sucursal es único | `existsByNombre()` |
| RN-SU2 | La hora de apertura debe ser anterior a la de cierre | Validación en service + CHECK BD |
| RN-SU3 | La capacidad debe ser positiva (1-500) | `@Min(1) @Max(500)` |
| RN-SU4 | El teléfono debe ser numérico | `@Pattern` |

---

## Tabla de códigos HTTP retornados

| Situación | Código | Excepción |
|-----------|--------|-----------|
| Operación exitosa | 200 OK | - |
| Recurso creado | 201 CREATED | - |
| Eliminación exitosa | 204 NO CONTENT | - |
| Datos inválidos en el request | 400 BAD REQUEST | `MethodArgumentNotValidException` |
| Recurso no encontrado | 404 NOT FOUND | `RecursoNoEncontradoException` |
| Regla de negocio violada | 409 CONFLICT | `ReglaNegocioException` |
| Error inesperado | 500 INTERNAL SERVER ERROR | `Exception` genérico |

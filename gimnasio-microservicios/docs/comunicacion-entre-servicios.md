# Comunicación entre Microservicios

Toda la comunicación se realiza mediante **Spring Cloud OpenFeign** sobre HTTP/REST. Cada microservicio que necesita información de otro declara una interfaz `@FeignClient` apuntando a la URL del servicio remoto.

---

## Diagrama de dependencias

```
                          ┌──────────────────┐
                          │   ms-sucursales  │ (8090) - base
                          └────────▲─────────┘
                                   │
            ┌──────────────────────┼───────────────────┐
            │                      │                   │
   ┌────────┴───────┐    ┌─────────┴────────┐  ┌───────┴────────┐
   │   ms-equipos   │    │     ms-clases    │  │ ms-asistencias │
   │     (8087)     │    │      (8084)      │  │     (8089)     │
   └────────────────┘    └──────┬───────────┘  └───────┬────────┘
                                │                      │
                                │              ┌───────┴────────┐
                                │              │   ms-socios    │◄────┐
                                │              │    (8081)      │     │
                                │              └───────▲────────┘     │
                                │                      │              │
                  ┌─────────────┴────┐                 │              │
                  │   ms-reservas    │─────────────────┤              │
                  │      (8085)      │                 │              │
                  └─────────┬────────┘                 │              │
                            │                          │              │
                            │           ┌──────────────┴──────┐       │
                            └──────────►│   ms-membresias     │───────┘
                                        │      (8082)         │
                                        └─────────────────────┘

   ┌──────────────────┐                ┌──────────────────────┐
   │ ms-instructores  │◄───────────────│      ms-rutinas      │
   │     (8083)       │                │       (8088)         │
   └──────────────────┘                └──────┬───────────────┘
                                              │
                                              └──► ms-socios

   ┌──────────────┐
   │   ms-pagos   │───► ms-socios
   │    (8086)    │
   └──────────────┘
```

---

## Matriz detallada

| Microservicio | Consume de | Para qué |
|---------------|-----------|----------|
| **ms-socios** | — | Sin dependencias (servicio base) |
| **ms-sucursales** | — | Sin dependencias (servicio base) |
| **ms-instructores** | — | Sin dependencias (servicio base) |
| **ms-membresias** | ms-socios | Validar existencia y estado del socio antes de crear membresía |
| **ms-clases** | ms-instructores, ms-sucursales | Validar instructor activo y sucursal con capacidad suficiente |
| **ms-reservas** | ms-socios, ms-membresias, ms-clases | Validar socio activo + membresía vigente + cupo, y decrementar/incrementar cupos |
| **ms-pagos** | ms-socios | Validar que el socio exista al registrar el pago |
| **ms-equipos** | ms-sucursales | Verificar que la sucursal exista y esté activa |
| **ms-rutinas** | ms-socios, ms-instructores | Validar socio activo e instructor activo al asignar rutina |
| **ms-asistencias** | ms-socios, ms-sucursales | Validar socio activo y sucursal operativa al registrar ingreso |

---

## Configuración Feign

### Anotaciones clave

```java
@SpringBootApplication
@EnableFeignClients              // En la clase Application principal
public class XxxxApplication { ... }
```

```java
@FeignClient(name = "ms-socios", url = "${ms-socios.url:http://localhost:8081}")
public interface SocioClient {
    @GetMapping("/api/socios/{id}")
    SocioRespuesta obtenerSocio(@PathVariable("id") Long id);
}
```

### URLs configurables

Cada microservicio cliente expone propiedades para apuntar a los servicios remotos. Por defecto se usan los puertos locales, pero pueden sobreescribirse:

```properties
# application.properties del microservicio cliente
ms-socios.url=http://localhost:8081
ms-membresias.url=http://localhost:8082
ms-instructores.url=http://localhost:8083
ms-clases.url=http://localhost:8084
ms-sucursales.url=http://localhost:8090
```

---

## Manejo de errores en llamadas Feign

Toda llamada remota se envuelve en un `try/catch` que distingue entre:

1. **`FeignException.NotFound`** (HTTP 404 desde el servicio remoto): se traduce a `RecursoNoEncontradoException` → 404 al cliente final.
2. **`FeignException` genérica**: se loguea con SLF4J nivel ERROR y se traduce a `ReglaNegocioException` con mensaje neutro ("No se pudo comunicar con ms-xxxxx"), evitando exponer detalles internos.

```java
try {
    return socioClient.obtenerSocio(socioId);
} catch (FeignException.NotFound e) {
    throw new RecursoNoEncontradoException("Socio con id " + socioId + " no existe");
} catch (FeignException e) {
    log.error("Error consultando ms-socios: {}", e.getMessage());
    throw new ReglaNegocioException("No se pudo comunicar con ms-socios");
}
```

---

## Flujo completo de ejemplo: crear una reserva

1. Cliente → `POST /api/reservas` a **ms-reservas** con `{socioId, claseId}`.
2. **ms-reservas** → Feign → **ms-socios** `GET /api/socios/{id}` → valida que el socio existe y está ACTIVO.
3. **ms-reservas** → Feign → **ms-membresias** `GET /api/membresias/socio/{id}/vigente` → confirma membresía vigente.
4. **ms-reservas** → Feign → **ms-clases** `GET /api/clases/{id}` → valida la clase y revisa cupos.
5. **ms-reservas** persiste la reserva en su propia BD con estado `CONFIRMADA`.
6. **ms-reservas** → Feign → **ms-clases** `PATCH /api/clases/{id}/decrementar-cupo` → reduce cupo en 1.
7. Si el decremento falla, **ms-reservas** elimina la reserva recién creada (compensación) y retorna 409.
8. Respuesta 201 CREATED al cliente con el detalle de la reserva.

Este flujo demuestra:
- Comunicación distribuida coordinada.
- Validación cruzada entre 3 microservicios.
- Manejo de fallos con compensación manual (sin necesidad de Saga ni transacciones distribuidas).

---

## Pruebas con Postman

La colección incluida en `postman/Gimnasio-Microservicios.postman_collection.json` contiene ejemplos de cada endpoint, incluyendo los flujos que ejercitan la comunicación Feign. Para probar:

1. Levantar los 10 microservicios.
2. Crear un socio en ms-socios.
3. Crear un plan en ms-membresias.
4. Crear una membresía vigente para ese socio.
5. Crear una sucursal y un instructor.
6. Crear una clase en ms-clases (esto ejercita Feign a ms-instructores + ms-sucursales).
7. Crear una reserva en ms-reservas (esto ejercita Feign a ms-socios + ms-membresias + ms-clases).

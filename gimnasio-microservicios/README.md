# Sistema de Gestión de Gimnasio - Arquitectura de Microservicios

**Asignatura:** DSY1103 - Desarrollo FullStack 1
**Evaluación:** Parcial 3 - Encargo con Defensa Técnica
**Equipo:** [Completar con nombres y número de equipo]

---

## Descripción del Proyecto

Sistema distribuido para la gestión integral de un gimnasio, construido bajo una arquitectura de **microservicios independientes**. Cada microservicio se encarga de un área funcional específica del dominio, posee su propia base de datos PostgreSQL y se comunica con los demás mediante **Feign Client** sobre endpoints REST.

El sistema cubre desde el registro de socios y la gestión de membresías, hasta el agendamiento de clases, control de asistencia, pagos, rutinas personalizadas y administración de equipamiento y sucursales.

Para esta evaluación se incorporó, sobre la base de la Sumativa 2: **pruebas unitarias e integración** (JUnit5/Mockito/H2), **documentación Swagger/OpenAPI**, un **API Gateway** (Spring Cloud Gateway) que centraliza el enrutamiento, **configuración YAML por perfiles** (`dev`/`docker`) y **despliegue containerizado con Docker**.

---

## Integrantes

| Nombre | Apellido | Rol |
|--------|----------|-----|
| [MAICKEL] | [ROMERO] | [LIDER] |


---

## Microservicios implementados

| # | Microservicio | Puerto | Responsabilidad |
|---|---------------|--------|-----------------|
| 1 | `ms-socios` | 8081 | Gestión de socios del gimnasio (CRUD, datos personales, estado) |
| 2 | `ms-membresias` | 8082 | Planes de membresía y suscripciones activas de socios |
| 3 | `ms-instructores` | 8083 | Registro y administración de instructores |
| 4 | `ms-clases` | 8084 | Catálogo de clases grupales y horarios |
| 5 | `ms-reservas` | 8085 | Reserva de cupos en clases por parte de los socios |
| 6 | `ms-pagos` | 8086 | Registro y consulta de pagos efectuados |
| 7 | `ms-equipos` | 8087 | Inventario de máquinas y equipos del gimnasio |
| 8 | `ms-rutinas` | 8088 | Rutinas de entrenamiento asignadas a socios |
| 9 | `ms-asistencias` | 8089 | Registro de ingresos diarios de socios |
| 10 | `ms-sucursales` | 8090 | Sucursales físicas y su capacidad |
| 11 | `ms-gateway` | **8080** | **API Gateway** - único punto de entrada externo, enruta hacia los 10 anteriores |

---

## API Gateway - rutas principales

Todo el tráfico externo entra por `http://localhost:8080`. El Gateway lo redirige internamente según el prefijo de la ruta (ver definición completa en `ms-gateway/src/main/resources/application.yml`):

```
http://localhost:8080/api/socios/**         -> ms-socios       (8081)
http://localhost:8080/api/membresias/**     -> ms-membresias   (8082)
http://localhost:8080/api/planes/**         -> ms-membresias   (8082)
http://localhost:8080/api/instructores/**   -> ms-instructores (8083)
http://localhost:8080/api/clases/**         -> ms-clases       (8084)
http://localhost:8080/api/reservas/**       -> ms-reservas     (8085)
http://localhost:8080/api/pagos/**          -> ms-pagos        (8086)
http://localhost:8080/api/equipos/**        -> ms-equipos      (8087)
http://localhost:8080/api/rutinas/**        -> ms-rutinas      (8088)
http://localhost:8080/api/asistencias/**    -> ms-asistencias  (8089)
http://localhost:8080/api/sucursales/**     -> ms-sucursales   (8090)
```

Cada ruta aplica un filtro (`AddRequestHeader`) y existe además un **filtro global** (`LoggingGlobalFilter`) que agrega un header de trazabilidad (`X-Gateway-Trace-Id`) y mide el tiempo de respuesta de cada solicitud que pasa por el Gateway.

---

## Documentación Swagger / OpenAPI

Con cada microservicio corriendo (local o vía Docker), su documentación interactiva está disponible en:

| Microservicio | Swagger UI |
|---|---|
| ms-socios | http://localhost:8081/swagger-ui.html |
| ms-membresias | http://localhost:8082/swagger-ui.html |
| ms-instructores | http://localhost:8083/swagger-ui.html |
| ms-clases | http://localhost:8084/swagger-ui.html |
| ms-reservas | http://localhost:8085/swagger-ui.html |
| ms-pagos | http://localhost:8086/swagger-ui.html |
| ms-equipos | http://localhost:8087/swagger-ui.html |
| ms-rutinas | http://localhost:8088/swagger-ui.html |
| ms-asistencias | http://localhost:8089/swagger-ui.html |
| ms-sucursales | http://localhost:8090/swagger-ui.html |

> 🔗 **Completar tras el despliegue remoto:** agregar aquí los links públicos equivalentes (ej. `https://ms-socios-xxxx.up.railway.app/swagger-ui.html`).

---

## Stack Tecnológico

- **Java 17**
- **Spring Boot 3.2.5** / **Spring Cloud 2023.0.1**
- **Spring Data JPA + Hibernate** (persistencia)
- **PostgreSQL** (base de datos por microservicio)
- **Bean Validation (JSR 380)** (validación de DTOs)
- **Spring Cloud OpenFeign** (comunicación entre microservicios)
- **Spring Cloud Gateway** (enrutamiento centralizado, reactivo)
- **springdoc-openapi** (documentación Swagger / OpenAPI 3)
- **JUnit 5 + Mockito + AssertJ** (pruebas unitarias)
- **H2** (pruebas de integración de la capa de persistencia, en memoria)
- **JaCoCo** (cobertura de pruebas, objetivo ≥80% en Service/Repository)
- **Docker + Docker Compose** (despliegue local containerizado)
- **SLF4J** (logging estructurado)
- **Maven** (gestión de dependencias)
- **Postman** (pruebas de integración manuales)

---

## Estructura de carpetas

```
gimnasio-microservicios/
├── README.md
├── docker-compose.yml          <- orquesta los 10 microservicios + 10 Postgres + gateway
├── ms-socios/
├── ms-membresias/
├── ms-instructores/
├── ms-clases/
├── ms-reservas/
├── ms-pagos/
├── ms-equipos/
├── ms-rutinas/
├── ms-asistencias/
├── ms-sucursales/
├── ms-gateway/                  <- API Gateway (Spring Cloud Gateway)
├── docs/
│   ├── modelo-datos.md
│   ├── reglas-negocio.md
│   └── comunicacion-entre-servicios.md
└── postman/
    └── Gimnasio-Microservicios.postman_collection.json
```

Cada microservicio sigue el patrón **CSR (Controller → Service → Repository)** con la siguiente estructura interna:

```
ms-xxxx/
├── pom.xml
├── Dockerfile
└── src/
    ├── main/
    │   ├── java/com/gimnasio/xxxx/
    │   │   ├── XxxxApplication.java   (con @OpenAPIDefinition para Swagger)
    │   │   ├── controller/             (anotado con @Tag/@Operation/@ApiResponse)
    │   │   ├── service/
    │   │   ├── repository/
    │   │   ├── model/
    │   │   ├── dto/
    │   │   ├── exception/
    │   │   └── client/                 (solo si consume otros microservicios)
    │   └── resources/
    │       └── application.yml         (perfiles dev/docker)
    └── test/java/com/gimnasio/xxxx/
        ├── service/                     <- pruebas unitarias (Mockito)
        └── repository/                  <- pruebas de integracion (H2)
```

---

## Configuración y ejecución

### Opción A (recomendada) - Docker Compose, todo con un solo comando

Requisitos: Docker Desktop instalado y corriendo.

```bash
docker compose up --build
```

Esto construye y levanta automáticamente: las 10 bases de datos PostgreSQL
(una por microservicio, en contenedores aislados), los 10 microservicios de
negocio y el API Gateway — todos usando el perfil `docker` de cada
`application.yml`, donde los hostnames se resuelven por el nombre de servicio
de `docker-compose.yml` en vez de `localhost`. El sistema completo queda
disponible en **http://localhost:8080** (vía el Gateway).

```bash
docker compose logs -f ms-socios   # ver logs de un servicio puntual
docker compose down                 # detener y eliminar los contenedores
```

### Opción B - Local manual, cada microservicio desde el IDE/Maven

Requisitos previos:

1. **JDK 17** instalado (`java -version`).
2. **Maven 3.8+** instalado (`mvn -version`).
3. **PostgreSQL 14+** corriendo en `localhost:5432`.
4. **Postman** para pruebas (opcional pero recomendado).

#### 1. Crear las bases de datos

Conéctate a PostgreSQL y ejecuta:

```sql
CREATE DATABASE gimnasio_socios;
CREATE DATABASE gimnasio_membresias;
CREATE DATABASE gimnasio_instructores;
CREATE DATABASE gimnasio_clases;
CREATE DATABASE gimnasio_reservas;
CREATE DATABASE gimnasio_pagos;
CREATE DATABASE gimnasio_equipos;
CREATE DATABASE gimnasio_rutinas;
CREATE DATABASE gimnasio_asistencias;
CREATE DATABASE gimnasio_sucursales;
```

Por defecto cada microservicio usa el usuario `postgres` con contraseña `postgres` (perfil `dev` en su `application.yml`). Si tu instalación es distinta, edita ese archivo.

#### 2. Compilar todos los microservicios

Desde la raíz del proyecto:

```bash
cd ms-socios && mvn clean install -DskipTests && cd ..
cd ms-membresias && mvn clean install -DskipTests && cd ..
cd ms-instructores && mvn clean install -DskipTests && cd ..
cd ms-clases && mvn clean install -DskipTests && cd ..
cd ms-reservas && mvn clean install -DskipTests && cd ..
cd ms-pagos && mvn clean install -DskipTests && cd ..
cd ms-equipos && mvn clean install -DskipTests && cd ..
cd ms-rutinas && mvn clean install -DskipTests && cd ..
cd ms-asistencias && mvn clean install -DskipTests && cd ..
cd ms-sucursales && mvn clean install -DskipTests && cd ..
cd ms-gateway && mvn clean install -DskipTests && cd ..
```

#### 3. Levantar los microservicios

En **terminales separadas**, levanta cada uno (perfil `dev` por defecto, apunta a `localhost`):

```bash
cd ms-socios       && mvn spring-boot:run
cd ms-membresias   && mvn spring-boot:run
cd ms-instructores && mvn spring-boot:run
cd ms-clases       && mvn spring-boot:run
cd ms-reservas     && mvn spring-boot:run
cd ms-pagos        && mvn spring-boot:run
cd ms-equipos      && mvn spring-boot:run
cd ms-rutinas      && mvn spring-boot:run
cd ms-asistencias  && mvn spring-boot:run
cd ms-sucursales   && mvn spring-boot:run
cd ms-gateway      && mvn spring-boot:run
```

Cada microservicio queda escuchando en su puerto asignado (ver tabla más arriba); el Gateway en el 8080.

### Opción C - Despliegue remoto (Railway / Render)

> ⚠️ **Completar tras desplegar:** documentar aquí la URL pública del Gateway y de al menos un microservicio, junto con las variables de entorno configuradas en la plataforma.

### Ejecutar las pruebas y ver la cobertura

```bash
cd ms-socios               # o cualquier otro microservicio
mvn clean test              # unitarias (Mockito) + integración (H2)
mvn clean verify             # además calcula cobertura con JaCoCo (umbral: 80% en service/repository)
# reporte HTML en: target/site/jacoco/index.html
```

### 4. Probar los endpoints

Importa la colección `postman/Gimnasio-Microservicios.postman_collection.json` en Postman, o usa la documentación interactiva Swagger de cada microservicio (ver sección más arriba).

---

## Funcionalidades implementadas

- CRUD completo en los 10 microservicios.
- Validaciones con Bean Validation en todos los DTOs de entrada.
- Manejo centralizado de excepciones con `@ControllerAdvice` por microservicio.
- Logs estructurados con SLF4J en controllers y services.
- Comunicación entre microservicios mediante Feign Client (ver `docs/comunicacion-entre-servicios.md`).
- Reglas de negocio del dominio (ver `docs/reglas-negocio.md`).
- Migraciones SQL iniciales en `src/main/resources/db/migration/V1__init.sql` por microservicio.
- **Pruebas unitarias** (JUnit5 + Mockito + AssertJ) sobre la capa Service de cada microservicio: reglas de negocio, validaciones, manejo de errores remotos (Feign) y compensación/rollback en `ms-reservas`.
- **Pruebas de integración** (`@DataJpaTest` + H2 embebido) sobre la capa Repository: valida las queries reales de Spring Data contra una base de datos relacional, sin mocks.
- **Documentación Swagger/OpenAPI** completa en los 11 controllers (`@Tag`, `@Operation`, `@ApiResponse`, ejemplos JSON) y metadata por microservicio (`@OpenAPIDefinition`).
- **API Gateway** (`ms-gateway`, Spring Cloud Gateway) que centraliza el enrutamiento hacia los 10 microservicios, con filtro global de trazabilidad y filtros por ruta.
- **Configuración YAML por perfiles** (`dev` / `docker`) en los 11 módulos.
- **Despliegue containerizado** con `Dockerfile` multi-stage por módulo y `docker-compose.yml` que orquesta todo el ecosistema.
- **Cobertura de pruebas** medida con JaCoCo (umbral 80% sobre Service/Repository).

---

## Documentación adicional

- [`docs/modelo-datos.md`](docs/modelo-datos.md) – Modelo de datos y relaciones por microservicio.
- [`docs/reglas-negocio.md`](docs/reglas-negocio.md) – Reglas de negocio implementadas.
- [`docs/comunicacion-entre-servicios.md`](docs/comunicacion-entre-servicios.md) – Diagrama de comunicación Feign entre microservicios.

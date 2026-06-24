# Guia de despliegue remoto (Railway / Render)

Esta parte **no la puede hacer un tercero por ustedes**: requiere crear una
cuenta propia del equipo y conectar el repositorio de GitHub. Esta guia deja
preparado todo lo que SI se puede preparar de antemano (Dockerfiles,
variables esperadas, perfil de Spring) para que el proceso de clic-en-clic
sea rapido.

La pauta exige desplegar en **al menos 2 entornos**: ya tienen Docker local
(`docker compose up --build`) cumpliendo el primero. Aqui esta el segundo.

## Opcion recomendada: Railway

Railway detecta automaticamente el `Dockerfile` de cada carpeta, por lo que
no se necesita configuracion adicional de build.

### Pasos

1. Crear cuenta en https://railway.app con la cuenta de GitHub del equipo.
2. **New Project -> Deploy from GitHub repo** -> seleccionar este repositorio.
3. Railway va a intentar desplegar la raiz del repo como un solo servicio.
   Como este es un monorepo con 11 modulos, hay que crear **un servicio por
   microservicio** y, en cada uno, ir a:
   `Settings -> Build -> Root Directory` y apuntarlo a la carpeta
   correspondiente (ej. `ms-socios`, `ms-gateway`, etc.). Railway usara el
   `Dockerfile` que ya existe en esa carpeta.
4. Repetir el paso 3 para los microservicios que decidan desplegar (como
   minimo: `ms-socios` + `ms-gateway` para poder mostrar el flujo completo
   en la defensa; idealmente los 11).
5. Para cada microservicio de **negocio** (no el gateway), agregar una base
   de datos PostgreSQL desde Railway: **New -> Database -> PostgreSQL**.
   Railway expone automaticamente variables `PGHOST`, `PGPORT`, `PGDATABASE`,
   `PGUSER`, `PGPASSWORD` en ese mismo proyecto.
6. En cada microservicio de negocio, ir a **Variables** y agregar:
   ```
   SPRING_PROFILES_ACTIVE=docker
   SPRING_DATASOURCE_URL=jdbc:postgresql://${{PGHOST}}:${{PGPORT}}/${{PGDATABASE}}
   SPRING_DATASOURCE_USERNAME=${{PGUSER}}
   SPRING_DATASOURCE_PASSWORD=${{PGPASSWORD}}
   ```
   (La sintaxis `${{...}}` es la forma en que Railway referencia variables
   de OTRO servicio del mismo proyecto - en este caso, las de la base de
   datos que crearon en el paso 5. Estas 4 variables SOBRESCRIBEN lo que
   defina el perfil `docker` del `application.yml`, asi que no es necesario
   crear un perfil nuevo.)
7. En `ms-gateway`, las rutas necesitan apuntar a la URL publica que Railway
   asigna a cada microservicio (algo como
   `https://ms-socios-production-xxxx.up.railway.app`). La forma mas simple
   y facil de explicar en la defensa es editar directamente
   `ms-gateway/src/main/resources/application.yml`: agregar un perfil nuevo
   (ej. `prod`) debajo de los bloques `dev`/`docker` ya existentes, con esas
   URLs publicas en vez de `http://ms-socios:8081`, y luego activar
   `SPRING_PROFILES_ACTIVE=prod` en las variables de ese servicio en Railway.
8. Verificar que cada servicio quedo "Active" (icono verde) en el dashboard
   de Railway, y que `https://<su-gateway>.up.railway.app/api/socios` responde.

## Alternativa: Render

El flujo es equivalente:

1. Crear cuenta en https://render.com con GitHub.
2. **New -> Web Service** -> conectar el repo -> en "Root Directory" indicar
   la carpeta del microservicio (ej. `ms-socios`) -> Render detecta el
   `Dockerfile` automaticamente (Environment: Docker).
3. **New -> PostgreSQL** para crear una base administrada por Render;
   copiar la "Internal Database URL" que entrega.
4. En el servicio web, pestaña **Environment**, agregar las mismas 4
   variables `SPRING_DATASOURCE_*` que en el paso 6 de Railway, usando los
   datos de la base creada en el paso 3.
5. Repetir para `ms-gateway`, actualizando las URLs de los `*.uri` segun las
   URLs publicas `https://ms-xxxx.onrender.com` que Render asigna a cada
   servicio.

## Checklist para la defensa

- [ ] Al menos 1 microservicio + el Gateway desplegados y respondiendo en una URL publica.
- [ ] Swagger UI accesible remotamente (`https://<url>/swagger-ui.html`).
- [ ] Variables de entorno configuradas SIN contraseñas hardcodeadas en el codigo (todo via variables de la plataforma).
- [ ] README.md actualizado con los links publicos reales (reemplazar los placeholders ⚠️).
- [ ] Poder explicar en la defensa (IE 3.3.6): que variables configuraron, que puertos quedaron expuestos, y como interpretaron/resolvieron algun error de despliegue si lo tuvieron.

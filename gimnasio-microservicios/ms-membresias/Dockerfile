# ===== Etapa 1: build =====
# Se compila el proyecto con Maven dentro de un contenedor que ya trae el
# JDK, para no depender de que Maven/Java esten instalados en la maquina
# que construye la imagen (consistencia entre entornos: "funciona igual
# en mi maquina, en el laboratorio y en el servidor remoto").
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Se descargan las dependencias en una capa separada: si solo cambia el
# codigo fuente (no el pom.xml), Docker reutiliza esta capa cacheada y el
# build siguiente es mucho mas rapido.
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# ===== Etapa 2: runtime =====
# La imagen final solo contiene el JRE (no el JDK ni Maven), por lo que es
# considerablemente mas liviana que si se usara la misma imagen del build.
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]

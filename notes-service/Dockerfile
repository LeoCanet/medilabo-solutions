# syntax=docker/dockerfile:1
# Multi-stage Dockerfile pour Notes Service
# Stage 1: Build avec Maven (Maven natif, cache m2)
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Cache des dépendances
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests dependency:go-offline

# Copie du code source
COPY src ./src

# Build de l'artefact
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests clean package

# Normaliser l'artefact en /app/app.jar (sélection robuste via find)
RUN set -eux; \
    ART="$(find target -maxdepth 1 -type f -name '*.jar' ! -name '*-sources.jar' ! -name '*-javadoc.jar' ! -name '*-plain.jar' ! -name 'original-*.jar' | head -n1)"; \
    test -n "$ART"; \
    cp "$ART" /app/app.jar

# Stage 2: Runtime (image épinglée)
FROM eclipse-temurin:21-jre-jammy

LABEL maintainer="mediscreen-team"
LABEL description="Notes Service - Microservice de gestion des notes médicales"

ENV APP_HOME=/app
ENV APP_USER=mediscreen

# Création utilisateur non-root
RUN groupadd -r "${APP_USER}" && useradd -r -g "${APP_USER}" "${APP_USER}"

# Installer curl pour le healthcheck
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR ${APP_HOME}

# Copier le jar avec les bons droits
COPY --chown=${APP_USER}:${APP_USER} --from=builder /app/app.jar app.jar

# Basculer sur utilisateur non-root
USER ${APP_USER}

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -fsS http://localhost:8082/actuator/health || exit 1

# Exposition du port
EXPOSE 8082

# Point d'entrée
ENTRYPOINT ["java", "-jar", "app.jar"]
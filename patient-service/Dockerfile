# Multi-stage Dockerfile pour Patient Service
# Stage 1: Build avec Maven
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copie des fichiers de configuration Maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .

# Téléchargement des dépendances (cache layer)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copie du code source
COPY src ./src

# Compilation avec support Java 21 preview features
ENV MAVEN_OPTS="--enable-preview"
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime avec JRE
FROM eclipse-temurin:21-jre

# Métadonnées
LABEL maintainer="mediscreen-team"
LABEL description="Patient Service - Microservice de gestion des patients"

# Variables d'environnement
ENV APP_HOME=/app
ENV APP_USER=mediscreen

# Création utilisateur non-root (sécurité)
RUN groupadd -r ${APP_USER} && useradd -r -g ${APP_USER} ${APP_USER}

# Installation curl pour health check
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR ${APP_HOME}

# Copie du JAR depuis le stage builder
COPY --from=builder /app/target/patient-service-*.jar app.jar

# Changement de propriétaire
RUN chown -R ${APP_USER}:${APP_USER} ${APP_HOME}

# Switch vers utilisateur non-root
USER ${APP_USER}

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# Exposition du port
EXPOSE 8081

# Point d'entrée
ENTRYPOINT ["java", "--enable-preview", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
# Mediscreen Microservices Endpoints Documentation

This document summarizes the key endpoints for each microservice in the Mediscreen application.

## 1. Patient Service (Port: 8081)

The Patient Service exposes the core REST API for managing patient data.

*   **Base URL:** `http://localhost:8081`
*   **API Base Path:** `/api/v1/patients`

### API Endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/patients` | Crée un nouveau patient. |
| `GET` | `/api/v1/patients/{id}` | Récupère un patient par son ID. |
| `GET` | `/api/v1/patients` | Récupère tous les patients. |
| `GET` | `/api/v1/patients/summary` | Récupère tous les patients (format résumé). |
| `PUT` | `/api/v1/patients/{id}` | Met à jour complètement un patient. |
| `PATCH` | `/api/v1/patients/{id}` | Met à jour partiellement un patient. |
| `DELETE` | `/api/v1/patients/{id}` | Supprime un patient. |
| `GET` | `/api/v1/patients/search` | Recherche par nom, prénom, nom complet, genre, ville, code postal, téléphone. |
| `GET` | `/api/v1/patients/search/birthdate` | Recherche par date de naissance. |
| `GET` | `/api/v1/patients/search/birthperiod` | Recherche par période de naissance. |
| `GET` | `/api/v1/patients/search/age` | Recherche par âge. |
| `GET` | `/api/v1/patients/recent` | Récupère les derniers patients créés. |
| `GET` | `/api/v1/patients/stats/genre` | Statistiques par genre. |
| `HEAD` | `/api/v1/patients/{id}` | Vérifie si un patient existe. |

### API Documentation (Swagger UI)

*   **URL:** `http://localhost:8081/swagger-ui.html` (or `http://localhost:8081/swagger-ui/index.html`)
    *   *Note: The exact path might vary slightly based on SpringDoc OpenAPI configuration.*

### Monitoring (Spring Boot Actuator)

*   **URL:** `http://localhost:8081/actuator`

## 2. Gateway Service (Port: 8888)

The Gateway Service acts as the single entry point for the application, routing requests to the appropriate microservices.

*   **Base URL:** `http://localhost:8888`

### Routing Rules

| Method | Path (Gateway) | Routes To | Description |
|---|---|---|---|
| `ANY` | `/api/v1/patients/**` | `http://127.0.0.1:8081` | Routes all patient-related API calls to the Patient Service. |

### Monitoring (Spring Boot Actuator)

*   **URL:** `http://localhost:8888/actuator`

## 3. Frontend Service (Port: 8080)

The Frontend Service provides the web-based user interface for the application.

*   **Base URL:** `http://localhost:8080`

### Web Pages / Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/patients` | Affiche la liste de tous les patients (page d'accueil). |
| `GET` | `/patients/add` | Affiche le formulaire d'ajout d'un nouveau patient. |
| `POST` | `/patients/save` | Traite la soumission du formulaire pour la création ou la mise à jour d'un patient. |
| `GET` | `/patients/update/{id}` | Affiche le formulaire de modification d'un patient existant. |
| `GET` | `/patients/delete/{id}` | Supprime un patient. |

### Monitoring (Spring Boot Actuator)

*   **URL:** `http://localhost:8080/actuator`

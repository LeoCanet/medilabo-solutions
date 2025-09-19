# Mediscreen Microservices Endpoints Documentation

Documentation complète des endpoints pour l'architecture microservices Mediscreen (Sprint 1 + Sprint 2).

## Architecture Globale

```
Frontend (8080) → Gateway (8888) → Patient Service (8081) → MySQL (3307)
                                 ↘ Notes Service (8082) → MongoDB (27018)
```

## 1. Patient Service (Port: 8081)

Microservice de gestion des données démographiques des patients avec base MySQL normalisée 3NF.

*   **Base URL:** `http://localhost:8081`
*   **API Base Path:** `/api/v1/patients`
*   **Sécurité:** Basic Auth requis (credentials spécifiques patient)

### API Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/patients` | Récupère tous les patients. |
| `GET` | `/api/v1/patients/{id}` | Récupère un patient par son ID. |
| `POST` | `/api/v1/patients` | Crée un nouveau patient. |
| `PUT` | `/api/v1/patients/{id}` | Met à jour complètement un patient. |

### API Documentation (Swagger UI)

*   **URL:** `http://localhost:8081/swagger-ui.html`

### Monitoring (Spring Boot Actuator)

*   **URL:** `http://localhost:8081/actuator`
*   **Health Check:** `http://localhost:8081/actuator/health`

## 2. Notes Service (Port: 8082) ✅ Sprint 2

Microservice de gestion des notes médicales avec base NoSQL MongoDB.

*   **Base URL:** `http://localhost:8082`
*   **API Base Path:** `/api/v1/notes`
*   **Sécurité:** Basic Auth requis (credentials spécifiques notes)

### API Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/notes/patient/{patientId}` | Récupère toutes les notes d'un patient. |
| `GET` | `/api/v1/notes/{id}` | Récupère une note par son ID. |
| `POST` | `/api/v1/notes` | Crée une nouvelle note médicale. |

### API Documentation (Swagger UI)

*   **URL:** `http://localhost:8082/swagger-ui.html`

### Monitoring (Spring Boot Actuator)

*   **URL:** `http://localhost:8082/actuator`
*   **Health Check:** `http://localhost:8082/actuator/health`

## 3. Gateway Service (Port: 8888)

Service de routage intelligent avec sécurité Basic Auth différenciée par microservice.

*   **Base URL:** `http://localhost:8888`
*   **Sécurité:** Basic Auth frontend requis + injection automatique credentials backend

### Routing Rules avec Tokens Différenciés

| Method | Path (Gateway) | Routes To | Credentials Injectés | Description |
|---|---|---|---|---|
| `ANY` | `/api/v1/patients/**` | `patient-service:8081` | `mediscreen-patient:patient_secure_2024` | Routes vers Patient Service. |
| `ANY` | `/api/v1/notes/**` | `notes-service:8082` | `mediscreen-notes:notes_secure_2024` | Routes vers Notes Service. |

### Filtres de Sécurité

- **Frontend → Gateway:** `mediscreen-frontend:medipass123`
- **Gateway → Patient Service:** Injection automatique credentials patient
- **Gateway → Notes Service:** Injection automatique credentials notes

### Monitoring (Spring Boot Actuator)

*   **URL:** `http://localhost:8888/actuator`
*   **Routes Info:** `http://localhost:8888/actuator/gateway/routes`

## 4. Frontend Service (Port: 8080)

Interface web Thymeleaf avec architecture découplée et pages spécialisées.

*   **Base URL:** `http://localhost:8080`
*   **Authentification:** Automatique via Feign interceptor

### Pages Patients (Sprint 1)

| Method | Path | Description |
|---|---|---|
| `GET` | `/patients` | Liste de tous les patients (page d'accueil). |
| `GET` | `/patients/add` | Formulaire d'ajout d'un nouveau patient. |
| `POST` | `/patients/save` | Traitement création/modification patient. |
| `GET` | `/patients/update/{id}` | Formulaire de modification d'un patient existant. |

### Pages Notes (Sprint 2) ✅

| Method | Path | Description |
|---|---|---|
| `GET` | `/patients/{patientId}/notes` | Page dédiée notes médicales d'un patient. |
| `POST` | `/patients/{patientId}/notes/add` | Ajout d'une nouvelle note médicale. |

### Navigation Architecture

```
/patients (liste) → /patients/{id}/notes (historique + formulaire ajout)
                 ↘ /patients/update/{id} (modification patient)
```

### Monitoring (Spring Boot Actuator)

*   **URL:** `http://localhost:8080/actuator`

## 5. Assessment Service (Port: 8083) ⏳ Sprint 3

Service d'évaluation du risque diabète (en préparation).

*   **Base URL:** `http://localhost:8083`
*   **API Base Path:** `/api/v1/assess`
*   **Sécurité:** Basic Auth requis (credentials spécifiques assessment)

### API Endpoints (Prévus)

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/assess/{patientId}` | Évalue le risque diabète d'un patient. |

## Bases de Données

### MySQL (Patient Service) - Port 3307

*   **Base:** `mediscreen_patients`
*   **Tables:** `patients` (normalisation 3NF)
*   **Accès:** `http://localhost:3307`

### MongoDB (Notes Service) - Port 27018

*   **Base:** `mediscreen_notes`
*   **Collection:** `notes`
*   **Accès:** `mongodb://localhost:27018`

## Tests de Connectivité

### Tests API Direct (avec Basic Auth)

```bash
# Patient Service
curl -u "mediscreen-patient:patient_secure_2024" http://localhost:8081/api/v1/patients

# Notes Service
curl -u "mediscreen-notes:notes_secure_2024" http://localhost:8082/api/v1/notes/patient/1
```

### Tests via Gateway

```bash
# Via Gateway (injection automatique credentials)
curl -u "mediscreen-frontend:medipass123" http://localhost:8888/api/v1/patients
curl -u "mediscreen-frontend:medipass123" http://localhost:8888/api/v1/notes/patient/1
```

### Tests Frontend End-to-End

```bash
# Interface web complète
curl http://localhost:8080/patients
curl http://localhost:8080/patients/1/notes
```

## Configuration Docker

Services déployés via `docker-compose.yml` :

- **mysql-db** (mediscreen-mysql) : Port 3307
- **mongodb** (mediscreen-mongo) : Port 27018
- **patient-service** (mediscreen-patient-service) : Port 8081
- **notes-service** (mediscreen-notes-service) : Port 8082
- **gateway-service** (mediscreen-gateway) : Port 8888
- **frontend-service** (mediscreen-frontend) : Port 8080

### Health Checks Complets

```bash
# Tous les services
curl http://localhost:8080/actuator/health  # Frontend
curl http://localhost:8888/actuator/health  # Gateway
curl http://localhost:8081/actuator/health  # Patient
curl http://localhost:8082/actuator/health  # Notes
```

---

**Architecture microservices complète Sprint 1 + Sprint 2 avec sécurité Basic Auth différenciée et pattern Repository découplé.**

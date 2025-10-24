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
*   **Architecture:** URIs variabilisées pour configuration flexible et tests optimisés

### Configuration URIs Variabilisées

```yaml
# application.yml
mediscreen:
  services:
    patient:
      uri: ${PATIENT_SERVICE_URI:http://patient-service:8081}
    notes:
      uri: ${NOTES_SERVICE_URI:http://notes-service:8082}
    assessment:
      uri: ${ASSESSMENT_SERVICE_URI:http://assessment-service:8083}
```

**Avantages** :
- ✅ Configuration externalisée (comme credentials)
- ✅ Tests Wiremock avec URIs dynamiques
- ✅ Cohérence architecture (credentials + URIs variabilisés)

### Routing Rules avec Tokens Différenciés

| Method | Path (Gateway) | Routes To | Credentials Injectés | Description |
|---|---|---|---|---|
| `ANY` | `/api/v1/patients/**` | `patient-service:8081` | `mediscreen-patient:patientpass123` | Routes vers Patient Service. |
| `ANY` | `/api/v1/notes/**` | `notes-service:8082` | `mediscreen-notes:notespass123` | Routes vers Notes Service. |
| `ANY` | `/api/v1/assess/**` | `assessment-service:8083` | `mediscreen-assessment:assessmentpass123` | Routes vers Assessment Service. |

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

## 5. Assessment Service (Port: 8083) ✅ Sprint 3 TERMINÉ

Service d'évaluation du risque diabète avec algorithme de détection des termes déclencheurs.

*   **Base URL:** `http://localhost:8083`
*   **API Base Path:** `/api/v1/assess`
*   **Sécurité:** Basic Auth requis (credentials spécifiques assessment)
*   **Architecture:** Service orchestrateur (appelle Patient + Notes via Gateway)

### API Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/assess/{patientId}` | Évalue le risque diabète d'un patient (NONE, BORDERLINE, IN_DANGER, EARLY_ONSET). |

### Architecture SRP (Séparation Responsabilités)

**AssessmentService expose 2 méthodes publiques** :

1. **`getAssessmentResponse(Long patientId)`** - Orchestration complète
   - Récupère patient via PatientApiClient (1 seul appel)
   - Récupère notes via NotesApiClient
   - Délègue calcul à assessDiabetesRisk()
   - Construit AssessmentResponse complète
   - **Utilisé par** : Controller pour réponse API

2. **`assessDiabetesRisk(PatientDto patient, List<NoteDto> notes)`** - Calcul pur
   - Combine texte notes
   - Compte termes déclencheurs
   - Délègue calcul risque au DiabetesRiskCalculator
   - Retourne RiskLevel uniquement
   - **Avantage** : Tests unitaires sans mocks API

**Optimisation Green Code** : -50% appels API (getPatientById appelé 1 fois au lieu de 2)

### Algorithme d'Évaluation

**11 termes déclencheurs** : Hémoglobine A1C, Microalbumine, Taille, Poids, Fumeur, Anormal, Cholestérol, Vertige, Rechute, Réaction, Anticorps

**Niveaux de risque** :
- **NONE** : Aucun terme déclencheur
- **BORDERLINE** : 2-5 termes ET patient >30 ans
- **IN_DANGER** : Homme <30 ans (3+ termes) OU Femme <30 ans (4+ termes) OU Patient >30 ans (6-7 termes)
- **EARLY_ONSET** : Homme <30 ans (5+ termes) OU Femme <30 ans (7+ termes) OU Patient >30 ans (8+ termes)

### API Documentation (Swagger UI)

*   **URL:** `http://localhost:8083/swagger-ui.html`

### Monitoring (Spring Boot Actuator)

*   **URL:** `http://localhost:8083/actuator`
*   **Health Check:** `http://localhost:8083/actuator/health`

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
curl -u "mediscreen-patient:patientpass123" http://localhost:8081/api/v1/patients

# Notes Service
curl -u "mediscreen-notes:notespass123" http://localhost:8082/api/v1/notes/patient/1

# Assessment Service
curl -u "mediscreen-assessment:assessmentpass123" http://localhost:8083/api/v1/assess/1
```

### Tests via Gateway

```bash
# Via Gateway (injection automatique credentials)
curl -u "mediscreen-frontend:medipass123" http://localhost:8888/api/v1/patients
curl -u "mediscreen-frontend:medipass123" http://localhost:8888/api/v1/notes/patient/1
curl -u "mediscreen-frontend:medipass123" http://localhost:8888/api/v1/assess/1
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
- **assessment-service** (mediscreen-assessment-service) : Port 8083
- **gateway-service** (mediscreen-gateway) : Port 8888
- **frontend-service** (mediscreen-frontend) : Port 8080

### Health Checks Complets

```bash
# Tous les services
curl http://localhost:8080/actuator/health  # Frontend
curl http://localhost:8888/actuator/health  # Gateway
curl http://localhost:8081/actuator/health  # Patient
curl http://localhost:8082/actuator/health  # Notes
curl http://localhost:8083/actuator/health  # Assessment
```

---

**Architecture microservices complète Sprint 1 + Sprint 2 + Sprint 3 avec :**
- Sécurité Basic Auth différenciée
- Pattern Repository découplé
- URIs variabilisées Gateway
- Architecture SRP AssessmentService (optimisation -50% appels API)

**Dernière mise à jour** : 24 Octobre 2025 - Architecture finale optimisée ✅

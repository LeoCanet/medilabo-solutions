package com.mediscreen.assessmentservice.service;

import com.mediscreen.assessmentservice.client.NotesApiClient;
import com.mediscreen.assessmentservice.client.PatientApiClient;
import com.mediscreen.assessmentservice.dto.AssessmentResponse;
import com.mediscreen.assessmentservice.dto.NoteDto;
import com.mediscreen.assessmentservice.dto.PatientDto;
import com.mediscreen.assessmentservice.enums.RiskLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service d'évaluation du risque diabète (Orchestrateur)
 *
 * Responsabilité UNIQUE : Coordonner les appels aux services externes
 * - Récupère les données patient via PatientApiClient
 * - Récupère les notes médicales via NotesApiClient
 * - Compte les termes déclencheurs via DiabetesTermsService
 * - Délègue le calcul du risque à DiabetesRiskCalculator
 *
 * Architecture : Séparation claire orchestration vs algorithme
 * - Cette classe = Orchestrateur (coordination appels externes)
 * - DiabetesRiskCalculator = Algorithme pur (calcul risque)
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final PatientApiClient patientApiClient;
    private final NotesApiClient notesApiClient;
    private final DiabetesTermsService diabetesTermsService;
    private final DiabetesRiskCalculator riskCalculator;

    /**
     * Récupère l'évaluation complète du risque diabète d'un patient
     * (Méthode utilisée par le controller pour obtenir la réponse complète)
     *
     * Responsabilité : Orchestration des appels API + construction de la réponse
     * - Récupère les données patient et notes via les clients API
     * - Délègue le calcul du risque à assessDiabetesRisk()
     * - Construit la réponse complète AssessmentResponse
     *
     * @param patientId ID du patient
     * @return AssessmentResponse avec toutes les informations patient et le risque
     */
    public AssessmentResponse getAssessmentResponse(Long patientId) {
        log.info("Récupération évaluation complète pour patient ID: {}", patientId);

        // 1. Récupérer les données UNE SEULE FOIS (orchestration)
        PatientDto patient = patientApiClient.getPatientById(patientId);
        List<NoteDto> notes = notesApiClient.getNotesByPatientId(patientId.intValue());
        log.debug("Patient récupéré: {} {}, âge: {}, genre: {}",
                patient.prenom(), patient.nom(), patient.getAge(), patient.genre());
        log.debug("Nombre de notes récupérées: {}", notes.size());

        // 2. Calculer le risque (délégation au calcul pur)
        RiskLevel riskLevel = assessDiabetesRisk(patient, notes);

        // 3. Construire et retourner la réponse complète
        return AssessmentResponse.of(patient, riskLevel);
    }

    /**
     * Calcule le risque diabète d'un patient (Algorithme pur)
     *
     * Responsabilité : Calcul du risque UNIQUEMENT (sans appels API)
     * - Combine les notes en texte
     * - Compte les termes déclencheurs
     * - Délègue le calcul final au DiabetesRiskCalculator
     *
     * Méthode publique pour faciliter les tests unitaires sans mocks API
     *
     * @param patient données du patient (âge, genre)
     * @param notes liste des notes médicales
     * @return niveau de risque calculé
     */
    public RiskLevel assessDiabetesRisk(PatientDto patient, List<NoteDto> notes) {
        log.debug("Calcul risque pour patient: {} {}, âge: {}, genre: {}",
                patient.prenom(), patient.nom(), patient.getAge(), patient.genre());

        // 1. Préparer les données : combiner toutes les notes en texte
        String combinedNotesText = combineNotesText(notes);

        // 2. Compter les termes déclencheurs via le service spécialisé
        int triggerTermsCount = diabetesTermsService.countTriggerTerms(combinedNotesText);
        log.debug("Nombre total de termes déclencheurs: {}", triggerTermsCount);

        // 3. Déléguer le calcul du risque au calculateur spécialisé
        RiskLevel riskLevel = riskCalculator.calculateRisk(
            patient.getAge(),
            patient.isMale(),
            triggerTermsCount
        );
        log.info("Évaluation terminée pour patient {} {} - Risque: {}",
                patient.prenom(), patient.nom(), riskLevel);

        return riskLevel;
    }

    /**
     * Méthode utilitaire : Combine toutes les notes en un seul texte
     *
     * Responsabilité UNIQUE : Transformation de données
     * Prépare les données pour le service de comptage de termes
     *
     * @param notes liste des notes du patient
     * @return texte combiné de toutes les notes (espaces entre chaque note)
     */
    private String combineNotesText(List<NoteDto> notes) {
        return notes.stream()
                .map(NoteDto::note)
                .reduce("", (a, b) -> a + " " + b);
    }
}
package com.mediscreen.assessmentservice.service;

import com.mediscreen.assessmentservice.client.NotesApiClient;
import com.mediscreen.assessmentservice.client.PatientApiClient;
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
     * Évalue le risque diabète d'un patient
     * @param patientId ID du patient
     * @return niveau de risque calculé
     */
    public RiskLevel assessDiabetesRisk(Long patientId) {
        log.info("Début évaluation risque diabète pour patient ID: {}", patientId);

        // 1. Récupérer les informations du patient (âge, genre)
        PatientDto patient = patientApiClient.getPatientById(patientId);
        log.debug("Patient récupéré: {} {}, âge: {}, genre: {}",
                patient.prenom(), patient.nom(), patient.getAge(), patient.genre());

        // 2. Récupérer toutes les notes du patient
        List<NoteDto> notes = notesApiClient.getNotesByPatientId(patientId.intValue());
        log.debug("Nombre de notes récupérées: {}", notes.size());

        // 3. Préparer les données : combiner toutes les notes en texte
        String combinedNotesText = combineNotesText(notes);

        // 4. Compter les termes déclencheurs via le service spécialisé
        int triggerTermsCount = diabetesTermsService.countTriggerTerms(combinedNotesText);
        log.debug("Nombre total de termes déclencheurs: {}", triggerTermsCount);

        // 5. Déléguer le calcul du risque au calculateur spécialisé
        RiskLevel riskLevel = riskCalculator.calculateRisk(
            patient.getAge(),
            patient.isMale(),
            triggerTermsCount
        );
        log.info("Évaluation terminée pour patient ID: {} - Risque: {}", patientId, riskLevel);

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
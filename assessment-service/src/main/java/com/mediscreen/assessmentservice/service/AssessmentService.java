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
 * Service d'évaluation du risque diabète
 *
 * Implémente l'algorithme exact selon les exigences OpenClassrooms :
 * - None: 0 terme déclencheur
 * - Borderline: 2-5 termes ET >30 ans
 * - In Danger: Homme <30 ans (3+ termes) | Femme <30 ans (4+ termes) | >30 ans (6-7 termes)
 * - Early onset: Homme <30 ans (5+ termes) | Femme <30 ans (7+ termes) | >30 ans (8+ termes)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final PatientApiClient patientApiClient;
    private final NotesApiClient notesApiClient;
    private final DiabetesTermsService diabetesTermsService;

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

        // 3. Compter les termes déclencheurs dans toutes les notes
        int triggerTermsCount = countTriggerTermsInNotes(notes);
        log.debug("Nombre total de termes déclencheurs: {}", triggerTermsCount);

        // 4. Appliquer l'algorithme d'évaluation
        RiskLevel riskLevel = calculateRiskLevel(patient, triggerTermsCount);
        log.info("Évaluation terminée pour patient ID: {} - Risque: {}", patientId, riskLevel);

        return riskLevel;
    }

    /**
     * Compte le nombre total de termes déclencheurs dans toutes les notes
     * @param notes liste des notes du patient
     * @return nombre total de termes déclencheurs
     */
    private int countTriggerTermsInNotes(List<NoteDto> notes) {
        return notes.stream()
                .mapToInt(note -> diabetesTermsService.countTriggerTerms(note.note()))
                .sum();
    }

    /**
     * Calcule le niveau de risque selon l'algorithme OpenClassrooms
     * @param patient informations du patient
     * @param triggerTermsCount nombre de termes déclencheurs
     * @return niveau de risque
     */
    private RiskLevel calculateRiskLevel(PatientDto patient, int triggerTermsCount) {
        int age = patient.getAge();
        boolean isMale = patient.isMale();
        boolean isFemale = patient.isFemale();

        log.debug("Calcul risque: âge={}, male={}, termes={}", age, isMale, triggerTermsCount);

        // None: Aucun terme déclencheur
        if (triggerTermsCount == 0) {
            return RiskLevel.NONE;
        }

        // Patients > 30 ans
        if (age > 30) {
            if (triggerTermsCount >= 2 && triggerTermsCount <= 5) {
                return RiskLevel.BORDERLINE;
            } else if (triggerTermsCount >= 6 && triggerTermsCount <= 7) {
                return RiskLevel.IN_DANGER;
            } else if (triggerTermsCount >= 8) {
                return RiskLevel.EARLY_ONSET;
            }
        }
        // Patients <= 30 ans
        else {
            if (isMale) {
                if (triggerTermsCount >= 3 && triggerTermsCount <= 4) {
                    return RiskLevel.IN_DANGER;
                } else if (triggerTermsCount >= 5) {
                    return RiskLevel.EARLY_ONSET;
                }
            } else if (isFemale) {
                if (triggerTermsCount >= 4 && triggerTermsCount <= 6) {
                    return RiskLevel.IN_DANGER;
                } else if (triggerTermsCount >= 7) {
                    return RiskLevel.EARLY_ONSET;
                }
            }
        }

        // Cas par défaut (nombre de termes insuffisant pour déclencher un risque)
        return RiskLevel.NONE;
    }
}
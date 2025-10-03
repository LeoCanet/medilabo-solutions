package com.mediscreen.assessmentservice.dto;

import com.mediscreen.assessmentservice.enums.RiskLevel;

import java.time.LocalDateTime;

/**
 * DTO de réponse pour l'évaluation du risque diabète
 *
 * Contient le résultat de l'évaluation avec toutes les informations
 * nécessaires pour l'affichage frontend.
 */
public record AssessmentResponse(
    Long patientId,
    String patientName,
    int patientAge,
    String patientGender,
    RiskLevel riskLevel,
    String riskDescription,
    LocalDateTime assessmentDate
) {

    /**
     * Constructeur de convenance pour créer une réponse d'évaluation
     * @param patient les informations du patient
     * @param riskLevel le niveau de risque calculé
     * @return AssessmentResponse complète
     */
    public static AssessmentResponse of(PatientDto patient, RiskLevel riskLevel) {
        return new AssessmentResponse(
            patient.id(),
            patient.prenom() + " " + patient.nom(),
            patient.getAge(),
            patient.genre(),
            riskLevel,
            riskLevel.getDescription(),
            LocalDateTime.now()
        );
    }
}
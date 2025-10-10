package com.mediscreen.frontend.dto;

/**
 * DTO Response Assessment pour frontend
 * Résultat de l'évaluation du risque diabète
 */
public record AssessmentResponse(
    Long patientId,
    String patientName,
    int patientAge,
    RiskLevel riskLevel
) {
}

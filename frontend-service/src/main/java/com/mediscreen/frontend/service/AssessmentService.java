package com.mediscreen.frontend.service;

import com.mediscreen.frontend.client.AssessmentApiClient;
import com.mediscreen.frontend.dto.AssessmentResponse;
import com.mediscreen.frontend.exception.AssessmentServiceException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service Assessment frontend (pattern Repository)
 * Abstrait les appels Feign au assessment-service
 * Transforme les exceptions techniques en exceptions métier
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssessmentService {

    private final AssessmentApiClient assessmentApiClient;

    /**
     * Évalue le risque diabète d'un patient
     * @param patientId ID du patient
     * @return résultat de l'évaluation
     * @throws AssessmentServiceException si erreur technique
     */
    public AssessmentResponse assessDiabetesRisk(Long patientId) {
        try {
            log.info("Appel assessment-service pour patient ID={}", patientId);
            AssessmentResponse response = assessmentApiClient.assessDiabetesRisk(patientId);
            log.info("Évaluation reçue pour patient ID={}: risque={}", patientId, response.riskLevel());
            return response;

        } catch (FeignException e) {
            log.error("Erreur Feign lors de l'évaluation patient ID={}: status={}, message={}",
                     patientId, e.status(), e.getMessage());
            throw new AssessmentServiceException(
                "Erreur lors de l'évaluation du risque diabète: " + e.getMessage(),
                e.status(),
                e
            );
        }
    }
}

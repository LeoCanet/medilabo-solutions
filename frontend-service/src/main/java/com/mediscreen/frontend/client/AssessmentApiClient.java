package com.mediscreen.frontend.client;

import com.mediscreen.frontend.config.FeignConfig;
import com.mediscreen.frontend.dto.AssessmentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Client Feign pour Assessment Service
 * Communication via Spring Cloud Gateway avec authentification Basic Auth automatique
 */
@FeignClient(
    name = "assessment-service",
    url = "${assessment.service.url}",
    configuration = FeignConfig.class
)
public interface AssessmentApiClient {

    /**
     * Évalue le risque diabète d'un patient
     * @param patientId ID du patient
     * @return résultat de l'évaluation avec niveau de risque
     */
    @GetMapping("/api/v1/assess/{patientId}")
    AssessmentResponse assessDiabetesRisk(@PathVariable Long patientId);
}

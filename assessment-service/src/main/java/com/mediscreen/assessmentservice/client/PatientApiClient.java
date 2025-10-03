package com.mediscreen.assessmentservice.client;

import com.mediscreen.assessmentservice.dto.PatientDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Client Feign pour communiquer avec Patient Service via Gateway
 *
 * Communication sécurisée via Gateway avec Basic Auth automatique.
 * Utilise les mêmes endpoints que le frontend pour cohérence.
 */
@FeignClient(name = "patient-api", url = "${mediscreen.clients.patient.url}")
public interface PatientApiClient {

    /**
     * Récupère un patient par son ID
     * Endpoint utilisé par l'algorithme d'évaluation pour obtenir l'âge et le genre
     *
     * @param id ID du patient
     * @return PatientDto avec les informations du patient
     */
    @GetMapping("/api/v1/patients/{id}")
    PatientDto getPatientById(@PathVariable("id") Long id);
}
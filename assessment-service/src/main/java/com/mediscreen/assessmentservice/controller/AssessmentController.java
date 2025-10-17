package com.mediscreen.assessmentservice.controller;

import com.mediscreen.assessmentservice.dto.AssessmentResponse;
import com.mediscreen.assessmentservice.service.AssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour l'évaluation du risque diabète
 *
 * Responsabilité UNIQUE : Gestion de la couche HTTP
 * - Réception des requêtes HTTP
 * - Validation des paramètres
 * - Délégation au service métier
 * - Retour des réponses HTTP
 *
 * Architecture : Séparation claire Controller vs Service
 * - Ce controller ne contient AUCUNE logique métier
 * - Toute la logique est déléguée à AssessmentService
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/assess")
@RequiredArgsConstructor
@Tag(name = "Assessment", description = "API d'évaluation du risque diabète")
public class AssessmentController {

    private final AssessmentService assessmentService;

    /**
     * Évalue le risque diabète d'un patient
     *
     * Responsabilité : Gestion HTTP uniquement
     * - Log de la requête
     * - Délégation au service métier
     * - Retour de la réponse HTTP
     *
     * @param patientId ID du patient à évaluer
     * @return AssessmentResponse avec le niveau de risque et les détails
     */
    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Évaluer le risque diabète d'un patient",
               description = "Calcule le niveau de risque diabète selon l'algorithme OpenClassrooms")
    public ResponseEntity<AssessmentResponse> assessPatient(
            @Parameter(description = "ID du patient", required = true)
            @PathVariable Long patientId) {

        log.info("Requête d'évaluation reçue pour patient ID: {}", patientId);

        try {
            // Délégation complète au service (AUCUNE logique métier ici)
            AssessmentResponse response = assessmentService.getAssessmentResponse(patientId);

            log.info("Évaluation réussie pour patient ID: {} - Risque: {}",
                    patientId, response.riskLevel());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de l'évaluation du patient ID: {}", patientId, e);
            throw e;
        }
    }

    /**
     * Endpoint de santé pour vérifier que le service fonctionne
     *
     * @return message de santé
     */
    @GetMapping("/health")
    @Operation(summary = "Vérification de santé du service")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Assessment Service is running");
    }
}
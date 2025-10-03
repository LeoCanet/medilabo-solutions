package com.mediscreen.frontend.controller;

import com.mediscreen.frontend.dto.AssessmentResponse;
import com.mediscreen.frontend.exception.AssessmentServiceException;
import com.mediscreen.frontend.exception.PatientNotFoundException;
import com.mediscreen.frontend.exception.PatientServiceException;
import com.mediscreen.frontend.service.AssessmentService;
import com.mediscreen.frontend.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Contrôleur dédié à l'évaluation du risque diabète
 *
 * Sprint 3 - User Story : Générer rapport diabète
 * Architecture cohérente avec NotesController (page dédiée)
 */
@Controller
@RequestMapping("/patients/{patientId}/assessment")
@RequiredArgsConstructor
@Slf4j
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final PatientService patientService;

    /**
     * User Story Sprint 3 : Afficher l'évaluation du risque diabète
     * Page dédiée cohérente avec l'architecture Notes
     */
    @GetMapping
    public String showAssessment(@PathVariable Long patientId, Model model) {
        log.info("=== ACCES PAGE EVALUATION PATIENT ID={} ===", patientId);

        try {
            // Contexte patient pour breadcrumb et informations
            var patient = patientService.getPatientById(patientId);
            model.addAttribute("patient", patient);
            log.debug("Patient trouvé: {} {}", patient.prenom(), patient.nom());

            // User Story Sprint 3: Évaluation du risque diabète
            AssessmentResponse assessment = assessmentService.assessDiabetesRisk(patientId);
            model.addAttribute("assessment", assessment);
            log.info("Patient ID={} - Risque évalué: {}", patientId, assessment.riskLevel());

            return "patients/assessment";

        } catch (PatientNotFoundException e) {
            log.warn("Patient ID={} non trouvé pour évaluation", patientId);
            return "redirect:/patients?error=patient-not-found";

        } catch (AssessmentServiceException e) {
            log.error("Erreur technique lors de l'évaluation patient ID={}: status={}",
                     patientId, e.getStatusCode(), e);
            model.addAttribute("errorMessage", "Erreur technique lors de l'évaluation: " + e.getMessage());
            return "error";

        } catch (PatientServiceException e) {
            log.error("Erreur technique lors de l'accès au patient ID={}: status={}",
                     patientId, e.getStatusCode(), e);
            return "redirect:/patients?error=technical-error";
        }
    }
}

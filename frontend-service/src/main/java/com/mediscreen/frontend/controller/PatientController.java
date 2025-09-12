package com.mediscreen.frontend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediscreen.frontend.dto.ApiErrorResponse;
import com.mediscreen.frontend.dto.PatientFormDto;
import com.mediscreen.frontend.exception.PatientNotFoundException;
import com.mediscreen.frontend.exception.PatientServiceException;
import com.mediscreen.frontend.exception.PatientValidationException;
import com.mediscreen.frontend.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * Contrôleur web pour la gestion des patients
 *
 * Suppression complète des FeignExceptions (découplage libs)
 * Gestion uniquement d'exceptions Business métier
 * Architecture prête pour changement de librairie HTTP
 * 
 * Ne connais pas Feign : utilise PatientService qui utilise PatientRepository.
 * Facilite les évolutions futures (Feign → WebClient → RestTemplate).
 */
@Controller
@RequestMapping("/patients")
@RequiredArgsConstructor
@Slf4j
public class PatientController {

    private final PatientService patientService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public String listPatients(Model model) {
        model.addAttribute("patients", patientService.getAllPatients());
        return "patients/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("patientFormDto", new PatientFormDto(null, "", "", LocalDate.now(), "", "", "", "", ""));
        model.addAttribute("pageTitle", "Ajouter un patient");
        return "patients/form";
    }

    @PostMapping("/add")
    public String createPatient(@Valid @ModelAttribute("patientFormDto") PatientFormDto patientFormDto, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Ajouter un patient");
            return "patients/form";
        }
        try {
            patientService.savePatient(patientFormDto);
            redirectAttributes.addFlashAttribute("successMessage", "Patient ajouté avec succès !");
            
        } catch (PatientValidationException e) {
            log.warn("Erreur de validation lors de l'ajout du patient: {}", e.getMessage());
            handleValidationException(e, result, model);
            model.addAttribute("pageTitle", "Ajouter un patient");
            return "patients/form";
            
        } catch (PatientServiceException e) {
            log.error("Erreur technique lors de l'ajout du patient: status={}", e.getStatusCode(), e);
            model.addAttribute("errorMessage", "Erreur technique : " + e.getMessage());
            model.addAttribute("pageTitle", "Ajouter un patient");
            return "patients/form";
        }
        return "redirect:/patients";
    }

    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        try {
            model.addAttribute("patientFormDto", patientService.getPatientFormById(id));
            model.addAttribute("pageTitle", "Modifier le patient");
            return "patients/form";
            
        } catch (PatientNotFoundException e) {
            log.warn("Tentative de modification d'un patient inexistant: ID={}", id);
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/patients";
        }
    }

    @PostMapping("/update/{id}")
    public String updatePatient(@PathVariable("id") Long id, @Valid @ModelAttribute("patientFormDto") PatientFormDto patientFormDto, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        log.info("=== DEBUT UPDATE PATIENT ID={} ===", id);
        log.info("FormDTO recu: {}", patientFormDto);
        
        if (result.hasErrors()) {
            log.warn("Erreurs de validation: {}", result.getAllErrors());
            model.addAttribute("pageTitle", "Modifier le patient");
            return "patients/form";
        }
        
        try {
            patientFormDto = patientFormDto.withId(id); // Assure que l'ID est bien celui de l'URL
            log.info("FormDTO avec ID: {}", patientFormDto);
            
            patientService.savePatient(patientFormDto);
            log.info("=== UPDATE REUSSI ===");
            
            // Message de succès via flash attribute
            redirectAttributes.addFlashAttribute("successMessage", "Patient modifié avec succès !");
            
        } catch (PatientNotFoundException e) {
            log.warn("Tentative de modification d'un patient inexistant: ID={}", id);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("pageTitle", "Modifier le patient");
            return "patients/form";
            
        } catch (PatientValidationException e) {
            log.warn("Erreur de validation lors de la modification du patient ID={}: {}", id, e.getMessage());
            handleValidationException(e, result, model);
            model.addAttribute("pageTitle", "Modifier le patient");
            return "patients/form";
            
        } catch (PatientServiceException e) {
            log.error("Erreur technique lors de la modification du patient ID={}: status={}", id, e.getStatusCode(), e);
            model.addAttribute("errorMessage", "Erreur technique : " + e.getMessage());
            model.addAttribute("pageTitle", "Modifier le patient");
            return "patients/form";
            
        } catch (Exception e) {
            log.error("=== ERREUR GENERALE UPDATE ===", e);
            model.addAttribute("errorMessage", "Erreur inattendue: " + e.getMessage());
            model.addAttribute("pageTitle", "Modifier le patient");
            return "patients/form";
        }
        
        return "redirect:/patients";
    }

    /**
     * Gère les exceptions de validation métier
     * 
     * Découplée de FeignException : utilise les détails JSON préservés
     * par la couche Repository lors de la transformation des exceptions.
     */
    private void handleValidationException(PatientValidationException e, BindingResult result, Model model) {
        log.debug("Gestion des erreurs de validation patient: {}", e.getMessage());
        
        String validationDetails = e.getValidationDetails();
        
        // Si on a les détails JSON, on tente de les parser pour afficher les erreurs spécifiques
        if (validationDetails != null && !validationDetails.isBlank()) {
            try {
                ApiErrorResponse errorResponse = objectMapper.readValue(validationDetails, ApiErrorResponse.class);
                
                // Ajout des erreurs de champs spécifiques
                if (errorResponse.getErrors() != null) {
                    errorResponse.getErrors().forEach(fieldError -> {
                        result.addError(new FieldError("patientFormDto", fieldError.getField(), fieldError.getMessage()));
                    });
                }
                
                // Message global si pas d'erreurs de champs
                if (errorResponse.getMessage() != null && result.getGlobalErrorCount() == 0 && result.getFieldErrorCount() == 0) {
                    model.addAttribute("errorMessage", errorResponse.getMessage());
                }
                
            } catch (JsonProcessingException jsonException) {
                log.warn("Impossible de parser les détails de validation JSON", jsonException);
                model.addAttribute("errorMessage", e.getMessage());
            }
        } else {
            // Fallback : message simple
            model.addAttribute("errorMessage", e.getMessage());
        }
    }
}
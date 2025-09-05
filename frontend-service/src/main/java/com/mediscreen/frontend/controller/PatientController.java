package com.mediscreen.frontend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediscreen.frontend.dto.ApiErrorResponse;
import com.mediscreen.frontend.dto.ApiFieldError;
import com.mediscreen.frontend.dto.PatientFormDto;
import com.mediscreen.frontend.service.PatientService;
import feign.FeignException;
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
import java.util.Optional;

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
        } catch (FeignException e) {
            handleFeignException(e, result, model);
            model.addAttribute("pageTitle", "Ajouter un patient");
            return "patients/form";
        }
        return "redirect:/patients";
    }

    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("patientFormDto", patientService.getPatientFormById(id));
        model.addAttribute("pageTitle", "Modifier le patient");
        return "patients/form";
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
            
        } catch (FeignException e) {
            log.error("=== ERREUR FEIGN UPDATE ===", e);
            handleFeignException(e, result, model);
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

    private void handleFeignException(FeignException e, BindingResult result, Model model) {
        log.error("Erreur Feign lors de l'enregistrement du patient: status={}, body={}", e.status(), e.contentUTF8(), e);
        if ((e.status() == 400 || e.status() == 422) && e.contentUTF8() != null && !e.contentUTF8().isBlank()) {
            try {
                ApiErrorResponse errorResponse = objectMapper.readValue(e.contentUTF8(), ApiErrorResponse.class);
                if (errorResponse.getErrors() != null) {
                    errorResponse.getErrors().forEach(fieldError -> {
                        result.addError(new FieldError("patientFormDto", fieldError.getField(), fieldError.getMessage()));
                    });
                }
                if (errorResponse.getMessage() != null && result.getGlobalErrorCount() == 0 && result.getFieldErrorCount() == 0) {
                    model.addAttribute("errorMessage", errorResponse.getMessage());
                }
            } catch (JsonProcessingException jsonException) {
                log.error("Erreur parsing JSON de l'erreur Feign", jsonException);
                model.addAttribute("errorMessage", "Une erreur inattendue est survenue.");
            }
        } else {
            model.addAttribute("errorMessage", "Une erreur technique est survenue: " + e.getMessage());
        }
    }
}
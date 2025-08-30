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

    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("patientFormDto", patientService.getPatientFormById(id));
        model.addAttribute("pageTitle", "Modifier le patient");
        return "patients/form";
    }

    @PostMapping("/save")
    public String savePatient(@Valid @ModelAttribute PatientFormDto patientFormDto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", patientFormDto.id() != null ? "Modifier le patient" : "Ajouter un patient");
            return "patients/form";
        }
        try {
            patientService.savePatient(patientFormDto);
        } catch (FeignException e) {
            log.error("Erreur Feign lors de l'enregistrement du patient: {}", Optional.of(e.status()), e);
            model.addAttribute("pageTitle", patientFormDto.id() != null ? "Modifier le patient" : "Ajouter un patient");

            if ((e.status() == 400 || e.status() == 422) && e.contentUTF8() != null && !e.contentUTF8().isBlank()) {
                try {
                    // Parse la réponse d'erreur normalisée
                    ApiErrorResponse errorResponse = objectMapper.readValue(e.contentUTF8(), ApiErrorResponse.class);

                    if (errorResponse.getErrors() != null && !errorResponse.getErrors().isEmpty()) {
                        for (ApiFieldError error : errorResponse.getErrors()) {
                            String field = error.getField();
                            String message = error.getMessage();
                            if (field != null && message != null) {
                                result.addError(new FieldError(
                                        "patientFormDto",
                                        field,
                                        message
                                ));
                            }
                        }
                    }

                    // Message global éventuel
                    if (errorResponse.getMessage() != null && !errorResponse.getMessage().isBlank()) {
                        model.addAttribute("errorMessage", errorResponse.getMessage());
                    }
                } catch (JsonProcessingException jsonException) {
                    log.error("Erreur lors du parsing de la réponse d'erreur Feign: {}", jsonException.getMessage());
                    model.addAttribute("errorMessage", "Erreur inattendue lors de l'enregistrement: " + jsonException.getMessage());
                }
            } else {
                model.addAttribute("errorMessage", "Erreur lors de l'enregistrement du patient: " + e.status() + " - " + e.getMessage());
            }
            return "patients/form";
        }
        return "redirect:/patients";
    }

}

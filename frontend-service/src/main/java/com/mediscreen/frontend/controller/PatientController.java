package com.mediscreen.frontend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/patients")
@RequiredArgsConstructor
@Slf4j
public class PatientController {

    private final PatientService patientService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public String listPatients(@RequestParam(required = false) String nom, Model model) {
        model.addAttribute("patients", patientService.searchPatients(nom));
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
            log.error("Erreur Feign lors de l'enregistrement du patient: {}", e.status(), e);
            model.addAttribute("pageTitle", patientFormDto.id() != null ? "Modifier le patient" : "Ajouter un patient");

            if (e.status() == 400 && e.contentUTF8() != null) {
                try {
                    // Supposons que le backend renvoie un JSON avec une liste d'erreurs
                    // Exemple de structure d'erreur Spring Boot par défaut:
                    // {"timestamp":"...","status":400,"error":"Bad Request","message":"Validation failed...","errors":[{"field":"telephone","defaultMessage":"Format de téléphone invalide"}, ...]}
                    
                    // Utilisation d'une Map pour parser la réponse JSON
                    Map<String, Object> errorResponse = objectMapper.readValue(e.contentUTF8(), new TypeReference<Map<String, Object>>() {});
                    
                    if (errorResponse.containsKey("errors")) {
                        List<Map<String, String>> errors = (List<Map<String, String>>) errorResponse.get("errors");
                        for (Map<String, String> error : errors) {
                            String field = error.get("field");
                            String defaultMessage = error.get("defaultMessage");
                            if (field != null && defaultMessage != null) {
                                result.addError(new FieldError(
                                        "patientFormDto", // objectName
                                        field,            // field
                                        defaultMessage    // defaultMessage
                                ));
                            }
                        }
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

    @GetMapping("/delete/{id}")
    public String deletePatient(@PathVariable("id") Long id) {
        patientService.deletePatient(id);
        return "redirect:/patients";
    }
}

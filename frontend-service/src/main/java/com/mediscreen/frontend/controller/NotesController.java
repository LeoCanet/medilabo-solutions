package com.mediscreen.frontend.controller;

import com.mediscreen.frontend.dto.NoteCreateDto;
import com.mediscreen.frontend.exception.NoteServiceException;
import com.mediscreen.frontend.exception.NoteValidationException;
import com.mediscreen.frontend.exception.PatientNotFoundException;
import com.mediscreen.frontend.exception.PatientServiceException;
import com.mediscreen.frontend.service.NotesService;
import com.mediscreen.frontend.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Contrôleur dédié à la gestion des notes médicales
 *
 * Architecture découplée :
 * - Page notes séparée du formulaire patient
 * - Navigation claire depuis liste patients
 * - Préparation Sprint 3 (action Évaluation)
 *
 * Réutilise l'architecture existante :
 * - NotesService (pattern Repository découplé)
 * - PatientService (contexte patient)
 * - Exceptions Business (gestion erreurs)
 */
@Controller
@RequestMapping("/patients/{patientId}/notes")
@RequiredArgsConstructor
@Slf4j
public class NotesController {

    private final NotesService notesService;
    private final PatientService patientService;

    /**
     * User Story 1 : Vue historique patient (page dédiée notes)
     */
    @GetMapping
    public String listPatientNotes(@PathVariable Long patientId, Model model) {
        log.info("=== ACCES PAGE NOTES PATIENT ID={} ===", patientId);

        try {
            // Contexte patient pour breadcrumb et informations
            var patient = patientService.getPatientById(patientId);
            model.addAttribute("patient", patient);
            log.debug("Patient trouvé: {} {}", patient.prenom(), patient.nom());

            // User Story 1: Récupération historique notes
            var notes = notesService.getNotesByPatientId(patientId.intValue());
            model.addAttribute("notes", notes);
            model.addAttribute("notesCount", notes.size());
            log.info("Patient ID={} - {} notes trouvées", patientId, notes.size());

            // User Story 2: Préparer DTO pour nouvelle note
            var noteCreateDto = new NoteCreateDto(
                patientId.intValue(),
                patient.prenom() + " " + patient.nom(),
                ""
            );
            model.addAttribute("noteCreateDto", noteCreateDto);

            return "patients/notes";

        } catch (PatientNotFoundException e) {
            log.warn("Patient ID={} non trouvé pour accès notes", patientId);
            return "redirect:/patients?error=patient-not-found";
        } catch (PatientServiceException e) {
            log.error("Erreur technique lors de l'accès au patient ID={}: status={}",
                     patientId, e.getStatusCode(), e);
            return "redirect:/patients?error=technical-error";
        }
    }

    /**
     * User Story 2 : Ajouter note observation (page dédiée)
     */
    @PostMapping("/add")
    public String addNote(@PathVariable Long patientId,
                         @Valid @ModelAttribute("noteCreateDto") NoteCreateDto noteCreateDto,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        log.info("=== AJOUT NOTE PATIENT ID={} ===", patientId);

        if (result.hasErrors()) {
            log.warn("Erreurs de validation note pour patient ID={}: {}", patientId, result.getAllErrors());

            // Recharger les données pour réafficher la page avec erreurs
            try {
                var patient = patientService.getPatientById(patientId);
                model.addAttribute("patient", patient);

                var notes = notesService.getNotesByPatientId(patientId.intValue());
                model.addAttribute("notes", notes);
                model.addAttribute("notesCount", notes.size());

                return "patients/notes";

            } catch (PatientNotFoundException e) {
                log.warn("Patient ID={} non trouvé lors de validation note", patientId);
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                return "redirect:/patients";
            }
        }

        try {
            // Assurer cohérence des données (sécurité)
            var patient = patientService.getPatientById(patientId);
            var noteToCreate = new NoteCreateDto(
                patientId.intValue(),
                patient.prenom() + " " + patient.nom(),
                noteCreateDto.note()
            );

            notesService.createNote(noteToCreate);
            log.info("Note ajoutée avec succès pour patient ID={}", patientId);

            redirectAttributes.addFlashAttribute("successMessage", "Note ajoutée avec succès !");

        } catch (PatientNotFoundException e) {
            log.warn("Patient ID={} non trouvé lors de l'ajout de note", patientId);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/patients";

        } catch (NoteValidationException e) {
            log.warn("Erreur de validation lors de l'ajout de note pour patient ID={}: {}", patientId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur de validation : " + e.getMessage());

        } catch (NoteServiceException e) {
            log.error("Erreur technique lors de l'ajout de note pour patient ID={}: status={}",
                     patientId, e.getStatusCode(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur technique : " + e.getMessage());

        } catch (PatientServiceException e) {
            log.error("Erreur technique patient lors de l'ajout de note pour ID={}: status={}",
                     patientId, e.getStatusCode(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur technique patient : " + e.getMessage());
        }

        // Redirection vers la page notes (même en cas d'erreur pour garder le contexte)
        return "redirect:/patients/" + patientId + "/notes";
    }
}
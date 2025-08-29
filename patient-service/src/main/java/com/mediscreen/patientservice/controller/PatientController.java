package com.mediscreen.patientservice.controller;

import com.mediscreen.patientservice.dto.*;
import com.mediscreen.patientservice.exception.PatientNotFoundException;
import com.mediscreen.patientservice.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Contrôleur REST pour la gestion des patients
 */
@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class PatientController {
    
    private final PatientService patientService;
    
    // === OPÉRATIONS CRUD ===
    
    /**
     * Crée un nouveau patient
     * POST /api/v1/patients
     */
    @PostMapping
    public ResponseEntity<PatientDto> createPatient(@Valid @RequestBody PatientCreateDto patientCreateDto) {
        log.info("Demande de création d'un patient: {} {}", 
                patientCreateDto.prenom(), patientCreateDto.nom());
        
        PatientDto createdPatient = patientService.createPatient(patientCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPatient);
    }
    
    /**
     * Récupère un patient par son ID
     * GET /api/v1/patients/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PatientDto> getPatientById(@PathVariable Long id) {
        log.debug("Demande de récupération du patient avec l'ID: {}", id);
        
        return patientService.getPatientById(id)
                .map(patient -> ResponseEntity.ok().body(patient))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Récupère tous les patients
     * GET /api/v1/patients
     */
    @GetMapping
    public ResponseEntity<List<PatientDto>> getAllPatients() {
        log.debug("Demande de récupération de tous les patients");
        
        List<PatientDto> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }
    
    /**
     * Récupère tous les patients (format résumé)
     * GET /api/v1/patients/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<List<PatientSummaryDto>> getAllPatientsSummary() {
        log.debug("Demande de récupération du résumé des patients");
        
        List<PatientSummaryDto> patients = patientService.getAllPatientsSummary();
        return ResponseEntity.ok(patients);
    }
    
    /**
     * Met à jour complètement un patient
     * PUT /api/v1/patients/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<PatientDto> updatePatient(
            @PathVariable Long id, 
            @Valid @RequestBody PatientDto patientDto) {
        log.info("Demande de mise à jour complète du patient avec l'ID: {}", id);
        
        try {
            PatientDto updatedPatient = patientService.updatePatient(id, patientDto);
            return ResponseEntity.ok(updatedPatient);
        } catch (PatientNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Met à jour partiellement un patient
     * PATCH /api/v1/patients/{id}
     */
    @PatchMapping("/{id}")
    public ResponseEntity<PatientDto> patchPatient(
            @PathVariable Long id, 
            @RequestBody PatientDto patientDto) {
        log.info("Demande de mise à jour partielle du patient avec l'ID: {}", id);
        
        try {
            PatientDto updatedPatient = patientService.patchPatient(id, patientDto);
            return ResponseEntity.ok(updatedPatient);
        } catch (PatientNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Supprime un patient
     * DELETE /api/v1/patients/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        log.info("Demande de suppression du patient avec l'ID: {}", id);
        
        try {
            patientService.deletePatient(id);
            return ResponseEntity.noContent().build();
        } catch (PatientNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // === OPÉRATIONS DE RECHERCHE ===
    
    /**
     * Recherche par nom et prénom
     * GET /api/v1/patients/search?nom=...&prenom=...
     */
    @GetMapping("/search")
    public ResponseEntity<List<PatientDto>> searchPatients(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String prenom,
            @RequestParam(required = false) String nomComplet,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) String codePostal,
            @RequestParam(required = false) String telephone) {
        
        log.debug("Recherche de patients avec les critères: nom={}, prenom={}, nomComplet={}", 
                 nom, prenom, nomComplet);
        
        List<PatientDto> patients = null;
        
        // Pattern matching pour la recherche
        if (nom != null && prenom != null) {
            patients = patientService.findPatientsByNomAndPrenom(nom, prenom);
        } else if (nomComplet != null) {
            patients = patientService.findPatientsByNomComplet(nomComplet);
        } else if (genre != null) {
            patients = patientService.findPatientsByGenre(genre);
        } else if (ville != null) {
            patients = patientService.findPatientsByVille(ville);
        } else if (codePostal != null) {
            patients = patientService.findPatientsByCodePostal(codePostal);
        } else if (telephone != null) {
            return patientService.findPatientByTelephone(telephone)
                    .map(patient -> ResponseEntity.ok(List.of(patient)))
                    .orElse(ResponseEntity.ok(List.of()));
        } else {
            // Aucun critère spécifié, retourne tous les patients
            patients = patientService.getAllPatients();
        }
        
        return ResponseEntity.ok(patients != null ? patients : List.of());
    }
    
    /**
     * Recherche par date de naissance
     * GET /api/v1/patients/search/birthdate?date=2000-01-01
     */
    @GetMapping("/search/birthdate")
    public ResponseEntity<List<PatientDto>> searchByBirthdate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.debug("Recherche de patients nés le: {}", date);
        
        List<PatientDto> patients = patientService.findPatientsByDateNaissance(date);
        return ResponseEntity.ok(patients);
    }
    
    /**
     * Recherche par période de naissance
     * GET /api/v1/patients/search/birthperiod?debut=1990-01-01&fin=2000-12-31
     */
    @GetMapping("/search/birthperiod")
    public ResponseEntity<List<PatientDto>> searchByBirthPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        
        log.debug("Recherche de patients nés entre {} et {}", debut, fin);
        
        List<PatientDto> patients = patientService.findPatientsByDateNaissanceBetween(debut, fin);
        return ResponseEntity.ok(patients);
    }
    
    /**
     * Recherche par âge
     * GET /api/v1/patients/search/age?min=20&max=65
     */
    @GetMapping("/search/age")
    public ResponseEntity<List<PatientDto>> searchByAge(
            @RequestParam int min,
            @RequestParam int max) {
        
        log.debug("Recherche de patients âgés de {} à {} ans", min, max);
        
        List<PatientDto> patients = patientService.findPatientsByAgeBetween(min, max);
        return ResponseEntity.ok(patients);
    }
    
    // === OPÉRATIONS SPÉCIALES ===
    
    /**
     * Récupère les derniers patients créés
     * GET /api/v1/patients/recent
     */
    @GetMapping("/recent")
    public ResponseEntity<List<PatientSummaryDto>> getRecentPatients() {
        log.debug("Demande des derniers patients créés");
        
        List<PatientSummaryDto> recentPatients = patientService.getRecentPatients();
        return ResponseEntity.ok(recentPatients);
    }
    
    /**
     * Statistiques par genre
     * GET /api/v1/patients/stats/genre
     */
    @GetMapping("/stats/genre")
    public ResponseEntity<List<Object[]>> getStatsByGenre() {
        log.debug("Demande des statistiques par genre");
        
        List<Object[]> stats = patientService.getPatientStatsByGenre();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Vérifie si un patient existe
     * HEAD /api/v1/patients/{id}
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkPatientExists(@PathVariable Long id) {
        log.debug("Vérification de l'existence du patient avec l'ID: {}", id);
        
        return patientService.existsById(id) ? 
                ResponseEntity.ok().build() : 
                ResponseEntity.notFound().build();
    }
}
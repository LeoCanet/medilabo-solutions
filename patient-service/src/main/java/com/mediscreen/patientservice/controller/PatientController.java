package com.mediscreen.patientservice.controller;

import com.mediscreen.patientservice.dto.*;
import com.mediscreen.patientservice.exception.PatientNotFoundException;
import com.mediscreen.patientservice.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
     * Met à jour complètement un patient
     * PUT /api/v1/patients/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<PatientDto> updatePatient(
            @PathVariable Long id, 
            @Valid @RequestBody PatientDto patientDto) {
        log.info("Demande de mise à jour complète du patient avec l'ID: {}", id);
        
        PatientDto updatedPatient = patientService.updatePatient(id, patientDto);
        return ResponseEntity.ok(updatedPatient);
    }
}
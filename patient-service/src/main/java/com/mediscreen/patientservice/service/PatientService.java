package com.mediscreen.patientservice.service;

import com.mediscreen.patientservice.dto.*;

import java.util.List;
import java.util.Optional;


/**
 * Interface du service Patient
 * Définit les opérations métier disponibles
 */
public interface PatientService {
    
    // === OPÉRATIONS CRUD ===
    
    /**
     * Crée un nouveau patient
     */
    PatientDto createPatient(PatientCreateDto patientCreateDto);
    
    /**
     * Récupère un patient par son ID
     */
    Optional<PatientDto> getPatientById(Long id);
    
    /**
     * Récupère tous les patients (avec pagination implicite)
     */
    List<PatientDto> getAllPatients();
    
    /**
     * Met à jour un patient existant
     */
    PatientDto updatePatient(Long id, PatientDto patientDto);
}
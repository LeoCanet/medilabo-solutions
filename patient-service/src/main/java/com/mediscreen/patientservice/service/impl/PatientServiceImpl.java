package com.mediscreen.patientservice.service.impl;

import com.mediscreen.patientservice.dto.*;
import com.mediscreen.patientservice.entity.Patient;
import com.mediscreen.patientservice.exception.PatientNotFoundException;
import com.mediscreen.patientservice.mapper.PatientMapper;
import com.mediscreen.patientservice.repository.PatientRepository;
import com.mediscreen.patientservice.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;


/**
 * Implémentation du service Patient
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PatientServiceImpl implements PatientService {
    
    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    
    // === OPÉRATIONS CRUD ===
    
    @Override
    public PatientDto createPatient(PatientCreateDto patientCreateDto) {
        log.debug("Création d'un nouveau patient: {} {}", 
                 patientCreateDto.prenom(), patientCreateDto.nom());
        
        Patient patient = patientMapper.toEntity(patientCreateDto);
        Patient savedPatient = patientRepository.save(patient);
        
        log.info("Patient créé avec l'ID: {}", savedPatient.getId());
        return patientMapper.toDto(savedPatient);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<PatientDto> getPatientById(Long id) {
        log.debug("Recherche du patient avec l'ID: {}", id);
        
        return patientRepository.findById(id)
                .map(patientMapper::toDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PatientDto> getAllPatients() {
        log.debug("Récupération de tous les patients");
        
        List<Patient> patients = patientRepository.findAll();
        return patientMapper.toDtoList(patients);
    }
    
    @Override
    public PatientDto updatePatient(Long id, PatientDto patientDto) {
        log.debug("Mise à jour complète du patient avec l'ID: {}", id);
        
        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient avec l'ID " + id + " non trouvé"));
        
        // Mise à jour complète
        Patient updatedPatient = patientMapper.toEntity(patientDto);
        updatedPatient.setId(existingPatient.getId());
        
        Patient savedPatient = patientRepository.save(updatedPatient);
        
        log.info("Patient avec l'ID {} mis à jour", id);
        return patientMapper.toDto(savedPatient);
    }
}
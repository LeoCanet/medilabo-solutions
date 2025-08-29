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

import java.time.LocalDate;
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
        
        // Vérification des doublons
        if (existsByNomAndPrenomAndDateNaissance(
                patientCreateDto.nom(), 
                patientCreateDto.prenom(), 
                patientCreateDto.dateNaissance())) {
            throw new IllegalArgumentException(
                STR."Un patient avec le nom '\{patientCreateDto.nom()} \{patientCreateDto.prenom()}' " +
                STR."né le \{patientCreateDto.dateNaissance()} existe déjà");
        }
        
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
                .orElseThrow(() -> new PatientNotFoundException(STR."Patient avec l'ID \{id} non trouvé"));
        
        // Mise à jour complète
        Patient updatedPatient = patientMapper.toEntity(patientDto);
        updatedPatient.setId(existingPatient.getId());
        
        Patient savedPatient = patientRepository.save(updatedPatient);
        
        log.info("Patient avec l'ID {} mis à jour", id);
        return patientMapper.toDto(savedPatient);
    }
    
    @Override
    public PatientDto patchPatient(Long id, PatientDto patientDto) {
        log.debug("Mise à jour partielle du patient avec l'ID: {}", id);
        
        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(STR."Patient avec l'ID \{id} non trouvé"));
        
        // Mise à jour partielle (ignore les valeurs null)
        patientMapper.updatePatientFromDto(patientDto, existingPatient);
        
        Patient savedPatient = patientRepository.save(existingPatient);
        
        log.info("Patient avec l'ID {} partiellement mis à jour", id);
        return patientMapper.toDto(savedPatient);
    }
    
    @Override
    public void deletePatient(Long id) {
        log.debug("Suppression du patient avec l'ID: {}", id);
        
        if (!patientRepository.existsById(id)) {
            throw new PatientNotFoundException(STR."Patient avec l'ID \{id} non trouvé");
        }
        
        patientRepository.deleteById(id);
        log.info("Patient avec l'ID {} supprimé", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return patientRepository.existsById(id);
    }
    
    // === OPÉRATIONS DE RECHERCHE ===
    
    @Override
    @Transactional(readOnly = true)
    public List<PatientDto> findPatientsByNomAndPrenom(String nom, String prenom) {
        log.debug("Recherche des patients: {} {}", prenom, nom);
        
        List<Patient> patients = patientRepository.findByNomIgnoreCaseAndPrenomIgnoreCase(nom, prenom);
        return patientMapper.toDtoList(patients);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PatientDto> findPatientsByNomComplet(String nomComplet) {
        log.debug("Recherche des patients par nom complet: {}", nomComplet);
        
        List<Patient> patients = patientRepository.findByNomCompletContaining(nomComplet);
        return patientMapper.toDtoList(patients);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PatientDto> findPatientsByGenre(String genre) {
        log.debug("Recherche des patients par genre: {}", genre);
        
        List<Patient> patients = patientRepository.findByGenre(genre.toUpperCase());
        return patientMapper.toDtoList(patients);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PatientDto> findPatientsByDateNaissance(LocalDate dateNaissance) {
        log.debug("Recherche des patients nés le: {}", dateNaissance);
        
        List<Patient> patients = patientRepository.findByDateNaissance(dateNaissance);
        return patientMapper.toDtoList(patients);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PatientDto> findPatientsByDateNaissanceBetween(LocalDate dateDebut, LocalDate dateFin) {
        log.debug("Recherche des patients nés entre {} et {}", dateDebut, dateFin);
        
        List<Patient> patients = patientRepository.findByDateNaissanceBetween(dateDebut, dateFin);
        return patientMapper.toDtoList(patients);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PatientDto> findPatientsByAgeBetween(int ageMin, int ageMax) {
        log.debug("Recherche des patients âgés de {} à {} ans", ageMin, ageMax);
        
        List<Patient> patients = patientRepository.findByAgeBetween(ageMin, ageMax);
        return patientMapper.toDtoList(patients);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PatientDto> findPatientsByVille(String ville) {
        log.debug("Recherche des patients par ville: {}", ville);
        
        List<Patient> patients = patientRepository.findByAdresseVille(ville);
        return patientMapper.toDtoList(patients);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PatientDto> findPatientsByCodePostal(String codePostal) {
        log.debug("Recherche des patients par code postal: {}", codePostal);
        
        List<Patient> patients = patientRepository.findByAdresseCodePostal(codePostal);
        return patientMapper.toDtoList(patients);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<PatientDto> findPatientByTelephone(String telephone) {
        log.debug("Recherche du patient par téléphone: {}", telephone);
        
        return patientRepository.findByTelephone(telephone)
                .map(patientMapper::toDto);
    }
    
    // === OPÉRATIONS SPÉCIALES ===
    
    @Override
    @Transactional(readOnly = true)
    public List<PatientSummaryDto> getAllPatientsSummary() {
        log.debug("Récupération du résumé de tous les patients");
        
        List<Patient> patients = patientRepository.findAll();
        return patientMapper.toSummaryDtoList(patients);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PatientSummaryDto> getRecentPatients() {
        log.debug("Récupération des derniers patients créés");
        
        List<Patient> recentPatients = patientRepository.findTop10ByOrderByIdDesc();
        return patientMapper.toSummaryDtoList(recentPatients);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getPatientStatsByGenre() {
        log.debug("Récupération des statistiques par genre");
        
        return patientRepository.countByGenre();
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByNomAndPrenomAndDateNaissance(String nom, String prenom, LocalDate dateNaissance) {
        return patientRepository.existsByNomAndPrenomAndDateNaissance(nom, prenom, dateNaissance);
    }
}
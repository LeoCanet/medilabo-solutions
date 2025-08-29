package com.mediscreen.patientservice.service;

import com.mediscreen.patientservice.dto.*;
import java.time.LocalDate;
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
    
    /**
     * Met à jour partiellement un patient
     */
    PatientDto patchPatient(Long id, PatientDto patientDto);
    
    /**
     * Supprime un patient
     */
    void deletePatient(Long id);
    
    /**
     * Vérifie si un patient existe
     */
    boolean existsById(Long id);
    
    // === OPÉRATIONS DE RECHERCHE ===
    
    /**
     * Recherche par nom et prénom
     */
    List<PatientDto> findPatientsByNomAndPrenom(String nom, String prenom);
    
    /**
     * Recherche par nom complet (nom + prénom ou prénom + nom)
     */
    List<PatientDto> findPatientsByNomComplet(String nomComplet);
    
    /**
     * Recherche par genre
     */
    List<PatientDto> findPatientsByGenre(String genre);
    
    /**
     * Recherche par date de naissance
     */
    List<PatientDto> findPatientsByDateNaissance(LocalDate dateNaissance);
    
    /**
     * Recherche par période de naissance
     */
    List<PatientDto> findPatientsByDateNaissanceBetween(LocalDate dateDebut, LocalDate dateFin);
    
    /**
     * Recherche par âge (range)
     */
    List<PatientDto> findPatientsByAgeBetween(int ageMin, int ageMax);
    
    /**
     * Recherche par ville
     */
    List<PatientDto> findPatientsByVille(String ville);
    
    /**
     * Recherche par code postal
     */
    List<PatientDto> findPatientsByCodePostal(String codePostal);
    
    /**
     * Recherche par numéro de téléphone
     */
    Optional<PatientDto> findPatientByTelephone(String telephone);
    
    // === OPÉRATIONS SPÉCIALES ===
    
    /**
     * Récupère la liste des patients (format résumé pour les listes)
     */
    List<PatientSummaryDto> getAllPatientsSummary();
    
    /**
     * Récupère les derniers patients créés
     */
    List<PatientSummaryDto> getRecentPatients();
    
    /**
     * Statistiques par genre
     */
    List<Object[]> getPatientStatsByGenre();
    
    /**
     * Vérifie si un patient existe déjà (évite les doublons)
     */
    boolean existsByNomAndPrenomAndDateNaissance(String nom, String prenom, LocalDate dateNaissance);
}
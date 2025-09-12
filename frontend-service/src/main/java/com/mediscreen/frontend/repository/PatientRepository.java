package com.mediscreen.frontend.repository;

import com.mediscreen.frontend.dto.PatientCreateDto;
import com.mediscreen.frontend.dto.PatientDto;
import com.mediscreen.frontend.exception.PatientNotFoundException;
import com.mediscreen.frontend.exception.PatientServiceException;
import com.mediscreen.frontend.exception.PatientValidationException;
import java.util.List;

/**
 * Interface Repository pour la gestion des patients
 * 
 * Abstraction découplée des librairies externes (Feign, RestTemplate, etc.).
 * Séparation entre la logique métier et la technologie de communication.
 * 
 * Cette interface permet de changer facilement l'implémentation 
 * (Feign → WebClient → RestTemplate) sans impacter le service ou contrôleur.
 */
public interface PatientRepository {
    
    /**
     * Récupère tous les patients
     * 
     * @return Liste des patients
     * @throws PatientServiceException en cas d'erreur technique
     */
    List<PatientDto> findAll();
    
    /**
     * Récupère un patient par son ID
     * 
     * @param id ID du patient
     * @return Patient trouvé
     * @throws PatientNotFoundException si le patient n'existe pas
     * @throws PatientServiceException en cas d'erreur technique
     */
    PatientDto findById(Long id);
    
    /**
     * Crée un nouveau patient
     * 
     * @param patientCreateDto Données du patient à créer
     * @throws PatientValidationException en cas d'erreur de validation
     * @throws PatientServiceException en cas d'erreur technique
     */
    void save(PatientCreateDto patientCreateDto);
    
    /**
     * Met à jour un patient existant
     * 
     * @param id ID du patient à modifier
     * @param patientDto Nouvelles données du patient
     * @throws PatientNotFoundException si le patient n'existe pas
     * @throws PatientValidationException en cas d'erreur de validation
     * @throws PatientServiceException en cas d'erreur technique
     */
    void update(Long id, PatientDto patientDto);
}
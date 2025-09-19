package com.mediscreen.frontend.repository;

import com.mediscreen.frontend.dto.NoteCreateDto;
import com.mediscreen.frontend.dto.NoteDto;
import com.mediscreen.frontend.exception.NoteNotFoundException;
import com.mediscreen.frontend.exception.NoteServiceException;
import com.mediscreen.frontend.exception.NoteValidationException;
import java.util.List;

/**
 * Interface Repository pour la gestion des notes
 * 
 * Abstraction découplée des librairies externes (Feign, RestTemplate, etc.).
 * Séparation entre la logique métier et la technologie de communication.
 * Architecture cohérente avec PatientRepository.
 * 
 * Cette interface permet de changer facilement l'implémentation 
 * (Feign → WebClient → RestTemplate) sans impacter le service ou contrôleur.
 */
public interface NotesRepository {
    
    /**
     * Récupère toutes les notes d'un patient par son ID
     * 
     * @param patId ID du patient
     * @return Liste des notes du patient
     * @throws NoteServiceException en cas d'erreur technique
     */
    List<NoteDto> findByPatientId(Integer patId);
    
    /**
     * Récupère une note par son ID
     * 
     * @param id ID de la note
     * @return Note trouvée
     * @throws NoteNotFoundException si la note n'existe pas
     * @throws NoteServiceException en cas d'erreur technique
     */
    NoteDto findById(String id);
    
    /**
     * Crée une nouvelle note
     * 
     * @param noteCreateDto Données de la note à créer
     * @throws NoteValidationException en cas d'erreur de validation
     * @throws NoteServiceException en cas d'erreur technique
     */
    void save(NoteCreateDto noteCreateDto);
    
    /**
     * Met à jour une note existante
     * 
     * @param id ID de la note à modifier
     * @param noteDto Nouvelles données de la note
     * @throws NoteNotFoundException si la note n'existe pas
     * @throws NoteValidationException en cas d'erreur de validation
     * @throws NoteServiceException en cas d'erreur technique
     */
    void update(String id, NoteDto noteDto);
    
    /**
     * Supprime une note
     * 
     * @param id ID de la note à supprimer
     * @throws NoteNotFoundException si la note n'existe pas
     * @throws NoteServiceException en cas d'erreur technique
     */
    void delete(String id);
}
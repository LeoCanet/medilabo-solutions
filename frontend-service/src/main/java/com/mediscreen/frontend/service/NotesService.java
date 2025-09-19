package com.mediscreen.frontend.service;

import com.mediscreen.frontend.dto.NoteCreateDto;
import com.mediscreen.frontend.dto.NoteDto;
import com.mediscreen.frontend.repository.NotesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service pour la gestion des notes médicales
 * 
 * Architecture découplée : utilise NotesRepository (abstraction)
 * Ne connait pas l'implémentation technique (Feign, RestTemplate, etc.)
 * Cohérent avec PatientService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotesService {

    private final NotesRepository notesRepository;

    /**
     * Récupère toutes les notes d'un patient
     */
    public List<NoteDto> getNotesByPatientId(Integer patId) {
        log.debug("Récupération des notes pour le patient ID: {}", patId);
        return notesRepository.findByPatientId(patId);
    }

    /**
     * Récupère une note par son ID
     */
    public NoteDto getNoteById(String id) {
        log.debug("Récupération de la note ID: {}", id);
        return notesRepository.findById(id);
    }

    /**
     * Crée une nouvelle note
     */
    public void createNote(NoteCreateDto noteCreateDto) {
        log.debug("Création d'une note pour le patient: {} (ID: {})", 
                 noteCreateDto.patient(), noteCreateDto.patId());
        notesRepository.save(noteCreateDto);
        log.info("Note créée avec succès pour le patient: {}", noteCreateDto.patient());
    }

    /**
     * Met à jour une note existante
     */
    public void updateNote(String id, NoteDto noteDto) {
        log.debug("Mise à jour de la note ID: {}", id);
        notesRepository.update(id, noteDto);
        log.info("Note ID: {} mise à jour avec succès", id);
    }

    /**
     * Supprime une note
     */
    public void deleteNote(String id) {
        log.debug("Suppression de la note ID: {}", id);
        notesRepository.delete(id);
        log.info("Note ID: {} supprimée avec succès", id);
    }
}
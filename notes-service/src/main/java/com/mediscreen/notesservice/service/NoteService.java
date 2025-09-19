package com.mediscreen.notesservice.service;

import com.mediscreen.notesservice.dto.*;

import java.util.List;
import java.util.Optional;

/**
 * Interface du service Note
 * Définit les opérations métier disponibles
 */
public interface NoteService {
    
    // === OPÉRATIONS CRUD ===
    
    /**
     * Crée une nouvelle note
     */
    NoteDto createNote(NoteCreateDto noteCreateDto);
    
    /**
     * Récupère une note par son ID
     */
    Optional<NoteDto> getNoteById(String id);
    
    /**
     * Récupère toutes les notes d'un patient par son ID
     */
    List<NoteDto> getNotesByPatientId(Integer patId);
    
    /**
     * Récupère toutes les notes d'un patient par son nom
     */
    List<NoteDto> getNotesByPatientName(String patient);
    
    /**
     * Récupère toutes les notes
     */
    List<NoteDto> getAllNotes();
    
    /**
     * Met à jour une note existante
     */
    NoteDto updateNote(String id, NoteDto noteDto);
    
    /**
     * Supprime une note
     */
    void deleteNote(String id);
}
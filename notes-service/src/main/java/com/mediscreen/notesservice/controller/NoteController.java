package com.mediscreen.notesservice.controller;

import com.mediscreen.notesservice.dto.*;
import com.mediscreen.notesservice.exception.NoteNotFoundException;
import com.mediscreen.notesservice.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Contrôleur REST pour la gestion des notes médicales
 */
@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class NoteController {
    
    private final NoteService noteService;
    
    // === OPÉRATIONS CRUD ===
    
    /**
     * Crée une nouvelle note
     * POST /api/v1/notes
     */
    @PostMapping
    public ResponseEntity<NoteDto> createNote(@Valid @RequestBody NoteCreateDto noteCreateDto) {
        log.info("Demande de création d'une note pour le patient: {} (ID: {})", 
                noteCreateDto.patient(), noteCreateDto.patId());
        
        NoteDto createdNote = noteService.createNote(noteCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNote);
    }
    
    /**
     * Récupère une note par son ID
     * GET /api/v1/notes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<NoteDto> getNoteById(@PathVariable String id) {
        log.debug("Demande de récupération de la note avec l'ID: {}", id);
        
        return noteService.getNoteById(id)
                .map(note -> ResponseEntity.ok().body(note))
                .orElseThrow(() -> new NoteNotFoundException("Note non trouvée avec l'ID: " + id));
    }
    
    /**
     * Récupère toutes les notes d'un patient par son ID
     * GET /api/v1/notes/patient/{patId}
     */
    @GetMapping("/patient/{patId}")
    public ResponseEntity<List<NoteDto>> getNotesByPatientId(@PathVariable Integer patId) {
        log.info("Demande de récupération des notes pour le patient ID: {}", patId);
        
        List<NoteDto> notes = noteService.getNotesByPatientId(patId);
        return ResponseEntity.ok(notes);
    }
    
    /**
     * Récupère toutes les notes d'un patient par son nom
     * GET /api/v1/notes/patient/name/{patient}
     */
    @GetMapping("/patient/name/{patient}")
    public ResponseEntity<List<NoteDto>> getNotesByPatientName(@PathVariable String patient) {
        log.info("Demande de récupération des notes pour le patient: {}", patient);
        
        List<NoteDto> notes = noteService.getNotesByPatientName(patient);
        return ResponseEntity.ok(notes);
    }
    
    /**
     * Récupère toutes les notes
     * GET /api/v1/notes
     */
    @GetMapping
    public ResponseEntity<List<NoteDto>> getAllNotes() {
        log.debug("Demande de récupération de toutes les notes");
        
        List<NoteDto> notes = noteService.getAllNotes();
        return ResponseEntity.ok(notes);
    }
    
    /**
     * Met à jour une note existante
     * PUT /api/v1/notes/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<NoteDto> updateNote(
            @PathVariable String id, 
            @Valid @RequestBody NoteDto noteDto) {
        
        log.info("Demande de mise à jour de la note avec l'ID: {}", id);
        
        NoteDto updatedNote = noteService.updateNote(id, noteDto);
        return ResponseEntity.ok(updatedNote);
    }
    
    /**
     * Supprime une note
     * DELETE /api/v1/notes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable String id) {
        log.info("Demande de suppression de la note avec l'ID: {}", id);
        
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }
    
    // === GESTION DES ERREURS ===
    
    /**
     * Gestion des erreurs NoteNotFoundException
     */
    @ExceptionHandler(NoteNotFoundException.class)
    public ResponseEntity<String> handleNoteNotFound(NoteNotFoundException ex) {
        log.warn("Note non trouvée: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
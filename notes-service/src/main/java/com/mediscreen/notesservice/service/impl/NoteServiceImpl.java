package com.mediscreen.notesservice.service.impl;

import com.mediscreen.notesservice.dto.*;
import com.mediscreen.notesservice.entity.Note;
import com.mediscreen.notesservice.exception.NoteNotFoundException;
import com.mediscreen.notesservice.mapper.NoteMapper;
import com.mediscreen.notesservice.repository.NoteRepository;
import com.mediscreen.notesservice.service.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implémentation du service Note
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NoteServiceImpl implements NoteService {
    
    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;
    
    // === OPÉRATIONS CRUD ===
    
    @Override
    public NoteDto createNote(NoteCreateDto noteCreateDto) {
        log.debug("Création d'une nouvelle note pour le patient ID: {} ({})", 
                 noteCreateDto.patId(), noteCreateDto.patient());
        
        Note note = noteMapper.toEntity(noteCreateDto);
        Note savedNote = noteRepository.save(note);
        
        log.info("Note créée avec l'ID: {} pour le patient {}", 
                savedNote.getId(), noteCreateDto.patient());
        return noteMapper.toDto(savedNote);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<NoteDto> getNoteById(String id) {
        log.debug("Recherche de la note avec l'ID: {}", id);
        
        return noteRepository.findById(id)
                .map(noteMapper::toDto);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NoteDto> getNotesByPatientId(Integer patId) {
        log.debug("Recherche des notes pour le patient ID: {}", patId);
        
        List<Note> notes = noteRepository.findByPatIdOrderByCreatedDateDesc(patId);
        log.info("Trouvé {} note(s) pour le patient ID: {}", notes.size(), patId);
        
        return noteMapper.toDtoList(notes);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NoteDto> getNotesByPatientName(String patient) {
        log.debug("Recherche des notes pour le patient: {}", patient);
        
        List<Note> notes = noteRepository.findByPatientOrderByCreatedDateDesc(patient);
        log.info("Trouvé {} note(s) pour le patient: {}", notes.size(), patient);
        
        return noteMapper.toDtoList(notes);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NoteDto> getAllNotes() {
        log.debug("Récupération de toutes les notes");
        
        List<Note> notes = noteRepository.findAll();
        log.info("Trouvé {} note(s) au total", notes.size());
        
        return noteMapper.toDtoList(notes);
    }
    
    @Override
    public NoteDto updateNote(String id, NoteDto noteDto) {
        log.debug("Mise à jour de la note ID: {}", id);
        
        Note existingNote = noteRepository.findById(id)
                .orElseThrow(() -> new NoteNotFoundException("Note non trouvée avec l'ID: " + id));
        
        noteMapper.updateNoteFromDto(noteDto, existingNote);
        Note updatedNote = noteRepository.save(existingNote);
        
        log.info("Note mise à jour avec succès ID: {}", id);
        return noteMapper.toDto(updatedNote);
    }
    
    @Override
    public void deleteNote(String id) {
        log.debug("Suppression de la note ID: {}", id);
        
        if (!noteRepository.existsById(id)) {
            throw new NoteNotFoundException("Note non trouvée avec l'ID: " + id);
        }
        
        noteRepository.deleteById(id);
        log.info("Note supprimée avec succès ID: {}", id);
    }
}
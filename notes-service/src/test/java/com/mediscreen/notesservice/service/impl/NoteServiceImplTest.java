package com.mediscreen.notesservice.service.impl;

import com.mediscreen.notesservice.dto.NoteCreateDto;
import com.mediscreen.notesservice.dto.NoteDto;
import com.mediscreen.notesservice.entity.Note;
import com.mediscreen.notesservice.exception.NoteNotFoundException;
import com.mediscreen.notesservice.mapper.NoteMapper;
import com.mediscreen.notesservice.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour l'implémentation du service Note {@link NoteServiceImpl}.
 * Utilise Mockito pour simuler les dépendances comme {@link NoteRepository} et {@link NoteMapper},
 * assurant ainsi que seule la logique du service est testée de manière isolée.
 */
@ExtendWith(MockitoExtension.class)
class NoteServiceImplTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private NoteMapper noteMapper;

    @InjectMocks
    private NoteServiceImpl noteService;

    private Note note;
    private NoteDto noteDto;
    private NoteCreateDto noteCreateDto;

    /**
     * Initialisation des objets de test avant chaque méthode de test.
     * Crée des instances de Note, NoteDto et NoteCreateDto pour les scénarios de test.
     */
    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        note = Note.builder()
                .id("507f1f77bcf86cd799439011")
                .patId(1)
                .patient("Test TestNone")
                .note("Le patient déclare qu'il 'se sent très bien' Poids égal ou inférieur au poids recommandé")
                .createdDate(now)
                .build();

        noteDto = NoteDto.of(
                "507f1f77bcf86cd799439011",
                1,
                "Test TestNone",
                "Le patient déclare qu'il 'se sent très bien' Poids égal ou inférieur au poids recommandé",
                now
        );

        noteCreateDto = NoteCreateDto.of(
                1,
                "Test TestNone",
                "Le patient déclare qu'il 'se sent très bien' Poids égal ou inférieur au poids recommandé"
        );
    }

    /**
     * Teste la création réussie d'une note.
     * Vérifie que le service appelle le mapper pour convertir le DTO en entité,
     * sauvegarde l'entité via le repository, puis reconvertit l'entité sauvegardée en DTO.
     */
    @Test
    @DisplayName("createNote - Should create a note successfully")
    void createNote_Success() {
        when(noteMapper.toEntity(noteCreateDto)).thenReturn(note);
        when(noteRepository.save(any(Note.class))).thenReturn(note);
        when(noteMapper.toDto(note)).thenReturn(noteDto);

        NoteDto result = noteService.createNote(noteCreateDto);

        assertThat(result).isNotNull();
        assertThat(result.patient()).isEqualTo("Test TestNone");
        assertThat(result.patId()).isEqualTo(1);
        verify(noteRepository, times(1)).save(any(Note.class));
    }

    /**
     * Teste la récupération d'une note par son ID lorsque la note est trouvée.
     */
    @Test
    @DisplayName("getNoteById - Should return note when found")
    void getNoteById_Found() {
        when(noteRepository.findById("507f1f77bcf86cd799439011")).thenReturn(Optional.of(note));
        when(noteMapper.toDto(note)).thenReturn(noteDto);

        Optional<NoteDto> result = noteService.getNoteById("507f1f77bcf86cd799439011");

        assertThat(result).isPresent();
        assertThat(result.get().patient()).isEqualTo("Test TestNone");
    }

    /**
     * Teste la récupération d'une note par son ID lorsque la note n'est pas trouvée.
     */
    @Test
    @DisplayName("getNoteById - Should return empty when not found")
    void getNoteById_NotFound() {
        when(noteRepository.findById("nonexistent")).thenReturn(Optional.empty());

        Optional<NoteDto> result = noteService.getNoteById("nonexistent");

        assertThat(result).isEmpty();
    }

    /**
     * Teste la récupération des notes par ID patient.
     */
    @Test
    @DisplayName("getNotesByPatientId - Should return notes for patient")
    void getNotesByPatientId_Success() {
        List<Note> notes = Arrays.asList(note);
        List<NoteDto> noteDtos = Arrays.asList(noteDto);

        when(noteRepository.findByPatIdOrderByCreatedDateDesc(1)).thenReturn(notes);
        when(noteMapper.toDtoList(notes)).thenReturn(noteDtos);

        List<NoteDto> result = noteService.getNotesByPatientId(1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).patId()).isEqualTo(1);
    }

    /**
     * Teste la récupération des notes par nom de patient.
     */
    @Test
    @DisplayName("getNotesByPatientName - Should return notes for patient name")
    void getNotesByPatientName_Success() {
        List<Note> notes = Arrays.asList(note);
        List<NoteDto> noteDtos = Arrays.asList(noteDto);

        when(noteRepository.findByPatientOrderByCreatedDateDesc("Test TestNone")).thenReturn(notes);
        when(noteMapper.toDtoList(notes)).thenReturn(noteDtos);

        List<NoteDto> result = noteService.getNotesByPatientName("Test TestNone");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).patient()).isEqualTo("Test TestNone");
    }

    /**
     * Teste la récupération de toutes les notes.
     */
    @Test
    @DisplayName("getAllNotes - Should return all notes")
    void getAllNotes_Success() {
        List<Note> notes = Arrays.asList(note);
        List<NoteDto> noteDtos = Arrays.asList(noteDto);

        when(noteRepository.findAll()).thenReturn(notes);
        when(noteMapper.toDtoList(notes)).thenReturn(noteDtos);

        List<NoteDto> result = noteService.getAllNotes();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).patient()).isEqualTo("Test TestNone");
    }

    /**
     * Teste la mise à jour réussie d'une note existante.
     */
    @Test
    @DisplayName("updateNote - Should update note successfully")
    void updateNote_Success() {
        when(noteRepository.findById("507f1f77bcf86cd799439011")).thenReturn(Optional.of(note));
        when(noteRepository.save(any(Note.class))).thenReturn(note);
        when(noteMapper.toDto(note)).thenReturn(noteDto);

        NoteDto result = noteService.updateNote("507f1f77bcf86cd799439011", noteDto);

        assertThat(result).isNotNull();
        assertThat(result.patient()).isEqualTo("Test TestNone");
        verify(noteMapper, times(1)).updateNoteFromDto(noteDto, note);
        verify(noteRepository, times(1)).save(note);
    }

    /**
     * Teste la mise à jour d'une note lorsque la note n'est pas trouvée.
     */
    @Test
    @DisplayName("updateNote - Should throw NoteNotFoundException when note not found")
    void updateNote_NotFound() {
        when(noteRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThrows(NoteNotFoundException.class, () ->
                noteService.updateNote("nonexistent", noteDto));

        verify(noteRepository, never()).save(any(Note.class));
    }

    /**
     * Teste la suppression réussie d'une note.
     */
    @Test
    @DisplayName("deleteNote - Should delete note successfully")
    void deleteNote_Success() {
        when(noteRepository.existsById("507f1f77bcf86cd799439011")).thenReturn(true);

        noteService.deleteNote("507f1f77bcf86cd799439011");

        verify(noteRepository, times(1)).deleteById("507f1f77bcf86cd799439011");
    }

    /**
     * Teste la suppression d'une note lorsque la note n'existe pas.
     */
    @Test
    @DisplayName("deleteNote - Should throw NoteNotFoundException when note not found")
    void deleteNote_NotFound() {
        when(noteRepository.existsById("nonexistent")).thenReturn(false);

        assertThrows(NoteNotFoundException.class, () ->
                noteService.deleteNote("nonexistent"));

        verify(noteRepository, never()).deleteById(any(String.class));
    }

}
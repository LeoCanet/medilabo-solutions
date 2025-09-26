package com.mediscreen.notesservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mediscreen.notesservice.dto.NoteCreateDto;
import com.mediscreen.notesservice.dto.NoteDto;
import com.mediscreen.notesservice.entity.Note;
import com.mediscreen.notesservice.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour le contrôleur Notes Service.
 *
 * Configuration des tests :
 * - Base de données : TestContainers MongoDB (isolation avec MongoDB réel)
 * - Sécurité : Filtres Spring Security désactivés (@AutoConfigureMockMvc(addFilters = false))
 * - Credentials : Variables factices pour éviter erreurs placeholder Spring Boot
 * - Container : MongoDB 7.0 éphémère pour chaque exécution de test
 *
 * Stratégie de séparation des responsabilités :
 * - Ces tests vérifient UNIQUEMENT la logique métier du contrôleur
 * - La sécurité Basic Auth est testée complètement au niveau du Gateway
 * - Les credentials dans @TestPropertySource ne sont jamais vérifiés (filtres désactivés)
 * - TestContainers garantit isolation complète avec MongoDB réel
 *
 * Conformité OpenClassrooms :
 * Utilise les données des 4 patients de test et valide la conservation du formatage
 * des notes médicales ainsi que la détection des termes déclencheurs diabète.
 */
@SpringBootTest(classes = com.mediscreen.notesservice.NotesServiceApplication.class)
@AutoConfigureMockMvc(addFilters = false)  // Désactive les filtres de sécurité Spring
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "AUTH_USERNAME=test-user",    // Credentials factices pour éviter erreurs placeholder
    "AUTH_PASSWORD=test-pass"     // Ces valeurs ne sont jamais vérifiées en test
})
@Testcontainers
class NoteControllerIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NoteRepository noteRepository;

    private ObjectMapper objectMapper;

    /**
     * Initialise le contexte de test avant chaque exécution.
     * Nettoie la base MongoDB et configure l'ObjectMapper.
     */
    @BeforeEach
    void setUp() {
        noteRepository.deleteAll();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Teste la création d'une note avec des données conformes aux exigences OpenClassrooms.
     */
    @Test
    @DisplayName("Integration - Créer note OpenClassrooms avec termes déclencheurs")
    void createNote_Integration_Success() throws Exception {
        NoteCreateDto noteCreateDto = NoteCreateDto.of(
                1,
                "Test TestBorderline",
                "Le patient déclare qu'il ressent beaucoup de stress au travail Il se plaint également que son audition est anormale dernièrement"
        );

        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patient").value("Test TestBorderline"))
                .andExpect(jsonPath("$.patId").value(1))
                .andExpect(jsonPath("$.note").value(noteCreateDto.note()))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdDate").exists());

        // Vérification en base MongoDB
        List<Note> notes = noteRepository.findAll();
        assertThat(notes).hasSize(1);
        assertThat(notes.get(0).getNote()).contains("anormal");
    }

    /**
     * Teste la récupération des notes par patient ID.
     */
    @Test
    @DisplayName("Integration - Récupérer notes par patient ID")
    void getNotesByPatientId_Integration_Success() throws Exception {
        // Given - Création de notes pour différents patients
        Note note1 = Note.builder()
                .patId(1)
                .patient("Test TestNone")
                .note("Le patient déclare qu'il 'se sent très bien' Poids égal ou inférieur au poids recommandé")
                .createdDate(LocalDateTime.now())
                .build();

        Note note2 = Note.builder()
                .patId(2)
                .patient("Test TestBorderline")
                .note("Le patient déclare qu'il ressent beaucoup de stress au travail")
                .createdDate(LocalDateTime.now())
                .build();

        noteRepository.saveAll(List.of(note1, note2));

        // When & Then
        mockMvc.perform(get("/api/v1/notes/patient/{patId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].patId").value(1))
                .andExpect(jsonPath("$[0].patient").value("Test TestNone"));
    }

    /**
     * Teste la récupération des notes par nom de patient.
     */
    @Test
    @DisplayName("Integration - Récupérer notes par nom patient")
    void getNotesByPatientName_Integration_Success() throws Exception {
        // Given
        Note note = Note.builder()
                .patId(3)
                .patient("Test TestInDanger")
                .note("Le patient déclare qu'il fume depuis peu")
                .createdDate(LocalDateTime.now())
                .build();

        noteRepository.save(note);

        // When & Then
        mockMvc.perform(get("/api/v1/notes/patient/name/{patient}", "Test TestInDanger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].patient").value("Test TestInDanger"))
                .andExpect(jsonPath("$[0].note").value("Le patient déclare qu'il fume depuis peu"));
    }

    /**
     * Teste la mise à jour d'une note existante.
     */
    @Test
    @DisplayName("Integration - Mise à jour note")
    void updateNote_Integration_Success() throws Exception {
        // Given
        Note existingNote = Note.builder()
                .patId(4)
                .patient("Test TestEarlyOnset")
                .note("Note originale")
                .createdDate(LocalDateTime.now())
                .build();

        Note savedNote = noteRepository.save(existingNote);

        NoteDto updateDto = NoteDto.of(
                savedNote.getId(),
                4,
                "Test TestEarlyOnset",
                "Note mise à jour avec termes: Hémoglobine A1C supérieure au niveau recommandé Taille, Poids, Cholestérol, Vertiges et Réaction",
                savedNote.getCreatedDate()
        );

        // When & Then
        mockMvc.perform(put("/api/v1/notes/{id}", savedNote.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedNote.getId()))
                .andExpect(jsonPath("$.note").value(updateDto.note()));

        // Vérification en base
        Note updatedInDb = noteRepository.findById(savedNote.getId()).orElseThrow();
        assertThat(updatedInDb.getNote()).contains("Hémoglobine A1C");
        assertThat(updatedInDb.getNote()).contains("Cholestérol");
    }

    /**
     * Teste la suppression d'une note.
     */
    @Test
    @DisplayName("Integration - Suppression note")
    void deleteNote_Integration_Success() throws Exception {
        // Given
        Note note = Note.builder()
                .patId(1)
                .patient("Test Patient")
                .note("Note à supprimer")
                .createdDate(LocalDateTime.now())
                .build();

        Note savedNote = noteRepository.save(note);

        // When & Then
        mockMvc.perform(delete("/api/v1/notes/{id}", savedNote.getId()))
                .andExpect(status().isNoContent());

        // Vérification en base
        assertThat(noteRepository.findById(savedNote.getId())).isEmpty();
    }

    /**
     * Teste la récupération de toutes les notes avec les 4 patients OpenClassrooms.
     */
    @Test
    @DisplayName("Integration - Toutes les notes des 4 patients OpenClassrooms")
    void getAllNotes_OpenClassroomsData_Success() throws Exception {
        // Given - Notes des 4 patients de test OpenClassrooms
        List<Note> openClassroomsNotes = List.of(
                Note.builder()
                        .patId(1)
                        .patient("Test TestNone")
                        .note("Le patient déclare qu'il 'se sent très bien' Poids égal ou inférieur au poids recommandé")
                        .createdDate(LocalDateTime.now())
                        .build(),
                Note.builder()
                        .patId(2)
                        .patient("Test TestBorderline")
                        .note("Le patient déclare qu'il ressent beaucoup de stress au travail Il se plaint également que son audition est anormale dernièrement")
                        .createdDate(LocalDateTime.now())
                        .build(),
                Note.builder()
                        .patId(3)
                        .patient("Test TestInDanger")
                        .note("Le patient déclare qu'il fume depuis peu")
                        .createdDate(LocalDateTime.now())
                        .build(),
                Note.builder()
                        .patId(4)
                        .patient("Test TestEarlyOnset")
                        .note("Le patient déclare qu'il lui est devenu difficile de monter les escaliers Il se plaint également d'être essoufflé Tests de laboratoire indiquant que les anticorps sont élevés Réaction aux médicaments")
                        .createdDate(LocalDateTime.now())
                        .build()
        );

        noteRepository.saveAll(openClassroomsNotes);

        // When & Then
        mockMvc.perform(get("/api/v1/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4));

        // Vérification en base MongoDB
        List<Note> notesInDb = noteRepository.findAll();
        assertThat(notesInDb).hasSize(4);
    }

    /**
     * Teste la gestion des erreurs - note non trouvée.
     */
    @Test
    @DisplayName("Integration - Note non trouvée")
    void getNoteById_NotFound_Integration() throws Exception {
        mockMvc.perform(get("/api/v1/notes/{id}", "nonexistent"))
                .andExpect(status().isNotFound());
    }

    /**
     * Teste la validation des données d'entrée.
     */
    @Test
    @DisplayName("Integration - Validation données invalides")
    void createNote_InvalidData_Integration() throws Exception {
        NoteCreateDto invalidDto = NoteCreateDto.of(
                null, // patId null
                "", // patient vide
                "" // note vide
        );

        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Teste le formatage original conservé dans les notes (exigence OpenClassrooms).
     */
    @Test
    @DisplayName("Integration - Conservation formatage original")
    void createNote_PreserveFormatting_Integration() throws Exception {
        NoteCreateDto noteWithFormatting = NoteCreateDto.of(
                3,
                "Test TestInDanger",
                "Le patient déclare qu'il fume depuis peu\n\nLe patient déclare qu'il est fumeur et qu'il a cessé de fumer l'année dernière\nIl se plaint également de crises d'apnée respiratoire anormales\n\nTests de laboratoire indiquant un taux de cholestérol LDL élevé"
        );

        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteWithFormatting)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.note").value(noteWithFormatting.note()));

        // Vérification que le formatage est conservé en base
        List<Note> notes = noteRepository.findAll();
        assertThat(notes.get(0).getNote()).contains("\n\n");
        assertThat(notes.get(0).getNote()).contains("cholestérol");
    }
}
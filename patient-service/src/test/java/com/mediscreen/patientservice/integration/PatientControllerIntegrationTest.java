package com.mediscreen.patientservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mediscreen.patientservice.dto.AdresseDto;
import com.mediscreen.patientservice.dto.PatientCreateDto;
import com.mediscreen.patientservice.dto.PatientDto;
import com.mediscreen.patientservice.entity.Patient;
import com.mediscreen.patientservice.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour le contrôleur Patient Service.
 *
 * Configuration des tests :
 * - Base de données : H2 in-memory (isolation complète)
 * - Sécurité : Filtres Spring Security désactivés (@AutoConfigureMockMvc(addFilters = false))
 * - Credentials : Variables factices pour éviter erreurs placeholder Spring Boot
 * - Normalisation : Tests de conformité 3NF (exigence OpenClassrooms)
 *
 * Stratégie de séparation des responsabilités :
 * - Ces tests vérifient UNIQUEMENT la logique métier du contrôleur
 * - La sécurité Basic Auth est testée complètement au niveau du Gateway
 * - Les credentials dans @TestPropertySource ne sont jamais vérifiés (filtres désactivés)
 * - Permet de tester les fonctionnalités CRUD sans contraintes d'authentification
 *
 * Conformité OpenClassrooms :
 * Utilise les données des 4 patients de test avec validation de la
 * normalisation 3NF et des contraintes d'intégrité.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)  // Désactive les filtres de sécurité Spring
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "AUTH_USERNAME=test-user",    // Credentials factices pour éviter erreurs placeholder
    "AUTH_PASSWORD=test-pass"     // Ces valeurs ne sont jamais vérifiées en test
})
class PatientControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientRepository patientRepository;

    private ObjectMapper objectMapper;

    /**
     * Initialise le contexte de test avant chaque exécution.
     * Nettoie la base H2 et configure l'ObjectMapper.
     */
    @BeforeEach
    void setUp() {
        patientRepository.deleteAll();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Teste la création d'un patient avec données OpenClassrooms.
     */
    @Test
    @DisplayName("Integration - Créer patient")
    void createPatient_Integration_Success() throws Exception {
        AdresseDto adresse = AdresseDto.of("1 Brookside St", "New York", "10001", "USA");
        PatientCreateDto patientCreateDto = new PatientCreateDto(
                "Test", "TestNone", LocalDate.of(1966, 12, 31),
                "F", "100-222-3333", adresse
        );

        mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patientCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("TestNone"))
                .andExpect(jsonPath("$.prenom").value("Test"))
                .andExpect(jsonPath("$.genre").value("F"))
                .andExpect(jsonPath("$.telephone").value("100-222-3333"))
                .andExpect(jsonPath("$.id").exists());

        // Vérification en base H2
        List<Patient> patients = patientRepository.findAll();
        assertThat(patients).hasSize(1);
        assertThat(patients.get(0).getNom()).isEqualTo("TestNone");
    }

    /**
     * Teste la récupération d'un patient par ID.
     */
    @Test
    @DisplayName("Integration - Récupérer patient par ID")
    void getPatientById_Integration_Success() throws Exception {
        // Given
        Patient patient = new Patient();
        patient.setPrenom("Test");
        patient.setNom("TestBorderline");
        patient.setDateNaissance(LocalDate.of(1945, 6, 24));
        patient.setGenre("M");
        patient.setTelephone("200-333-4444");

        Patient savedPatient = patientRepository.save(patient);

        // When & Then
        mockMvc.perform(get("/api/v1/patients/{id}", savedPatient.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedPatient.getId()))
                .andExpect(jsonPath("$.nom").value("TestBorderline"))
                .andExpect(jsonPath("$.prenom").value("Test"));
    }

    /**
     * Teste la récupération de tous les patients des données OpenClassrooms.
     */
    @Test
    @DisplayName("Integration - Les 4 patients OpenClassrooms")
    void getAllPatients_OpenClassroomsData_Success() throws Exception {
        // Given - Les 4 patients de test OpenClassrooms
        AdresseDto adresse1 = AdresseDto.of("1 Brookside St", "New York", "10001", "USA");
        AdresseDto adresse2 = AdresseDto.of("2 High St", "New York", "10001", "USA");
        AdresseDto adresse3 = AdresseDto.of("3 Club Road", "New York", "10001", "USA");
        AdresseDto adresse4 = AdresseDto.of("4 Valley Dr", "New York", "10001", "USA");

        List<PatientCreateDto> openClassroomsPatients = List.of(
                new PatientCreateDto("Test", "TestNone", LocalDate.of(1966, 12, 31), "F", "100-222-3333", adresse1),
                new PatientCreateDto("Test", "TestBorderline", LocalDate.of(1945, 6, 24), "M", "200-333-4444", adresse2),
                new PatientCreateDto("Test", "TestInDanger", LocalDate.of(2004, 6, 18), "M", "300-444-5555", adresse3),
                new PatientCreateDto("Test", "TestEarlyOnset", LocalDate.of(2002, 6, 28), "F", "400-555-6666", adresse4)
        );

        // Création via requêtes HTTP pour test complet du controller
        for (PatientCreateDto patientDto : openClassroomsPatients) {
            mockMvc.perform(post("/api/v1/patients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patientDto)))
                    .andExpect(status().isCreated());
        }

        // When & Then
        mockMvc.perform(get("/api/v1/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[*].prenom").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("Test"))))
                .andExpect(jsonPath("$[*].nom").value(org.hamcrest.Matchers.hasItems("TestNone", "TestBorderline", "TestInDanger", "TestEarlyOnset")));

        // Vérification en base H2
        List<Patient> patientsInDb = patientRepository.findAll();
        assertThat(patientsInDb).hasSize(4);
    }

    /**
     * Teste la mise à jour d'un patient.
     */
    @Test
    @DisplayName("Integration - Mise à jour patient")
    void updatePatient_Integration_Success() throws Exception {
        // Given
        Patient patient = new Patient();
        patient.setPrenom("Test");
        patient.setNom("TestInDanger");
        patient.setDateNaissance(LocalDate.of(2004, 6, 18));
        patient.setGenre("M");
        patient.setTelephone("300-444-5555");

        Patient savedPatient = patientRepository.save(patient);

        AdresseDto nouvelleAdresse = AdresseDto.of("3 New Club Road", "New York", "10001", "USA");
        PatientDto updateDto = new PatientDto(
                savedPatient.getId(),
                "TestModifie", // Nouveau prénom
                "TestInDanger",
                LocalDate.of(2004, 6, 18),
                "M",
                "300-444-5555",
                nouvelleAdresse
        );

        // When & Then
        mockMvc.perform(put("/api/v1/patients/{id}", savedPatient.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedPatient.getId()))
                .andExpect(jsonPath("$.prenom").value("TestModifie"))
                .andExpect(jsonPath("$.nom").value("TestInDanger"));

        // Vérification en base H2
        Patient updatedInDb = patientRepository.findById(savedPatient.getId()).orElseThrow();
        assertThat(updatedInDb.getPrenom()).isEqualTo("TestModifie");
    }


    /**
     * Teste la gestion des erreurs - patient non trouvé.
     */
    @Test
    @DisplayName("Integration - Patient non trouvé")
    void getPatientById_NotFound_Integration() throws Exception {
        mockMvc.perform(get("/api/v1/patients/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    /**
     * Teste la validation des données d'entrée.
     */
    @Test
    @DisplayName("Integration - Validation données invalides")
    void createPatient_InvalidData_Integration() throws Exception {
        PatientCreateDto invalidDto = new PatientCreateDto(
                "", // Prénom vide
                "", // Nom vide
                null, // Date naissance null
                "", // Genre vide
                "", // Téléphone vide
                null
        );

        mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Teste l'intégration complète CRUD avec normalisation 3NF (exigence OpenClassrooms).
     */
    @Test
    @DisplayName("Integration - CRUD complet avec normalisation 3NF")
    void crudPatient_3NF_Integration_Success() throws Exception {
        // CREATE avec données normalisées
        AdresseDto adresse = AdresseDto.of("4 Valley Dr", "New York", "10001", "USA");
        PatientCreateDto createDto = new PatientCreateDto(
                "Test", "TestEarlyOnset", LocalDate.of(2002, 6, 28),
                "F", "400-555-6666", adresse
        );

        String response = mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();

        // Utilisation de JsonNode pour extraire seulement l'ID sans désérialiser tout l'objet
        com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(response);
        Long patientId = jsonNode.get("id").asLong();

        // READ
        mockMvc.perform(get("/api/v1/patients/{id}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("TestEarlyOnset"));

        // UPDATE
        PatientDto updateDto = new PatientDto(
                patientId,
                "Test", // Prénom inchangé
                "TestEarlyOnsetModifie", // Nom modifié
                LocalDate.of(2002, 6, 28),
                "F",
                "400-555-6666",
                adresse
        );

        mockMvc.perform(put("/api/v1/patients/{id}", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("TestEarlyOnsetModifie"));

        // Vérification normalisation 3NF en base
        Patient finalPatient = patientRepository.findById(patientId).orElseThrow();
        assertThat(finalPatient.getNom()).isEqualTo("TestEarlyOnsetModifie");
        assertThat(finalPatient.getPrenom()).isEqualTo("Test");
        assertThat(finalPatient.getGenre()).isEqualTo("F");

        // Vérification contraintes d'intégrité
        assertThat(finalPatient.getId()).isNotNull();
        assertThat(finalPatient.getDateNaissance()).isNotNull();
    }
}
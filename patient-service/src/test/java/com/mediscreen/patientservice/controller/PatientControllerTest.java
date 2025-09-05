package com.mediscreen.patientservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mediscreen.patientservice.dto.PatientCreateDto;
import com.mediscreen.patientservice.dto.PatientDto;
import com.mediscreen.patientservice.exception.PatientNotFoundException;
import com.mediscreen.patientservice.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

/**
 * Classe de test pour {@link PatientController}.
 * Utilise {@link WebMvcTest} pour se concentrer sur les composants Spring MVC,
 * permettant de tester les endpoints REST du contrôleur de manière isolée.
 * La configuration automatique de la sécurité est exclue pour simplifier les tests
 * de la logique du contrôleur sans nécessiter d'authentification.
 */
@WebMvcTest(controllers = PatientController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc; // Permet de simuler des requêtes HTTP vers le contrôleur.

    @MockBean
    private PatientService patientService; // Mock du service pour isoler le test du contrôleur.

    private ObjectMapper objectMapper; // Utilisé pour la conversion d'objets Java en JSON et vice-versa.

    private PatientDto patientDto;
    private PatientCreateDto patientCreateDto;

    /**
     * Initialisation des objets de test avant chaque méthode de test.
     * Configure un ObjectMapper pour la sérialisation/désérialisation JSON
     * et initialise des DTOs de patient pour les scénarios de test.
     */
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Nécessaire pour gérer LocalDate

        patientDto = new PatientDto(
                1L, "Test", "TestNone", LocalDate.of(1966, 12, 31),
                "F", "100-222-3333", null
        );

        patientCreateDto = new PatientCreateDto(
                "Test", "TestNone", LocalDate.of(1966, 12, 31),
                "F", "100-222-3333", null
        );
    }

    /**
     * Teste la création réussie d'un patient.
     * Vérifie que le contrôleur retourne un statut HTTP 201 (Created)
     * et que le patient créé est correctement renvoyé en JSON.
     */
    @Test
    @DisplayName("createPatient - Should return 201 CREATED on success")
    void createPatient_Success() throws Exception {
        // Simule le comportement du service : retourne un patientDto lors de la création
        when(patientService.createPatient(any(PatientCreateDto.class))).thenReturn(patientDto);

        // Exécute une requête POST et vérifie la réponse
        mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patientCreateDto)))
                .andExpect(status().isCreated()) // Attend un statut 201
                .andExpect(jsonPath("$.nom").value("TestNone")); // Vérifie le nom dans la réponse JSON
    }

    /**
     * Teste la gestion des entrées invalides lors de la création d'un patient.
     * Vérifie que le contrôleur retourne un statut HTTP 400 (Bad Request)
     * lorsque les données du DTO de création sont invalides.
     */
    @Test
    @DisplayName("createPatient - Should return 400 BAD REQUEST for invalid input")
    void createPatient_InvalidInput() throws Exception {
        // Crée un DTO invalide avec des champs vides pour déclencher la validation
        PatientCreateDto invalidDto = new PatientCreateDto(
                "", "", null, "", "", null
        );

        // Exécute une requête POST avec le DTO invalide et vérifie la réponse
        mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest()); // Attend un statut 400
    }

    /**
     * Teste la récupération d'un patient par son ID lorsque le patient est trouvé.
     * Vérifie que le contrôleur retourne un statut HTTP 200 (OK)
     * et que le patient est correctement renvoyé en JSON.
     */
    @Test
    @DisplayName("getPatientById - Should return 200 OK when patient found")
    void getPatientById_Found() throws Exception {
        // Simule le comportement du service : retourne un Optional contenant le patientDto
        when(patientService.getPatientById(1L)).thenReturn(Optional.of(patientDto));

        // Exécute une requête GET et vérifie la réponse
        mockMvc.perform(get("/api/v1/patients/{id}", 1L))
                .andExpect(status().isOk()) // Attend un statut 200
                .andExpect(jsonPath("$.nom").value("TestNone")); // Vérifie le nom dans la réponse JSON
    }

    /**
     * Teste la récupération d'un patient par son ID lorsque le patient n'est pas trouvé.
     * Vérifie que le contrôleur retourne un statut HTTP 404 (Not Found).
     */
    @Test
    @DisplayName("getPatientById - Should return 404 NOT FOUND when patient not found")
    void getPatientById_NotFound() throws Exception {
        // Simule le comportement du service : retourne un Optional vide
        when(patientService.getPatientById(2L)).thenReturn(Optional.empty());

        // Exécute une requête GET et vérifie la réponse
        mockMvc.perform(get("/api/v1/patients/{id}", 2L))
                .andExpect(status().isNotFound()); // Attend un statut 404
    }

    /**
     * Teste la récupération de tous les patients lorsque la liste n'est pas vide.
     * Vérifie que le contrôleur retourne un statut HTTP 200 (OK)
     * et que la liste des patients est correctement renvoyée en JSON.
     */
    @Test
    @DisplayName("getAllPatients - Should return 200 OK with list of patients")
    void getAllPatients_Success() throws Exception {
        // Simule le comportement du service : retourne une liste de patientDto
        when(patientService.getAllPatients()).thenReturn(Arrays.asList(patientDto));

        // Exécute une requête GET et vérifie la réponse
        mockMvc.perform(get("/api/v1/patients"))
                .andExpect(status().isOk()) // Attend un statut 200
                .andExpect(jsonPath("$[0].nom").value("TestNone")); // Vérifie le nom du premier patient
    }

    /**
     * Teste la récupération de tous les patients lorsque la liste est vide.
     * Vérifie que le contrôleur retourne un statut HTTP 200 (OK)
     * et qu'une liste JSON vide est renvoyée.
     */
    @Test
    @DisplayName("getAllPatients - Should return 200 OK with empty list when no patients")
    void getAllPatients_EmptyList() throws Exception {
        // Simule le comportement du service : retourne une liste vide
        when(patientService.getAllPatients()).thenReturn(Collections.emptyList());

        // Exécute une requête GET et vérifie la réponse
        mockMvc.perform(get("/api/v1/patients"))
                .andExpect(status().isOk()) // Attend un statut 200
                .andExpect(jsonPath("$.length()").value(0)); // Vérifie que la liste est vide
    }

    /**
     * Teste la mise à jour réussie d'un patient.
     * Vérifie que le contrôleur retourne un statut HTTP 200 (OK)
     * et que le patient mis à jour est correctement renvoyé en JSON.
     */
    @Test
    @DisplayName("updatePatient - Should return 200 OK on success")
    void updatePatient_Success() throws Exception {
        // Simule le comportement du service : retourne le patientDto mis à jour
        when(patientService.updatePatient(eq(1L), any(PatientDto.class))).thenReturn(patientDto);

        // Exécute une requête PUT et vérifie la réponse
        mockMvc.perform(put("/api/v1/patients/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patientDto)))
                .andExpect(status().isOk()) // Attend un statut 200
                .andExpect(jsonPath("$.nom").value("TestNone")); // Vérifie le nom dans la réponse JSON
    }

    /**
     * Teste la mise à jour d'un patient lorsque le patient n'est pas trouvé.
     * Vérifie que le contrôleur retourne un statut HTTP 404 (Not Found).
     */
    @Test
    @DisplayName("updatePatient - Should return 404 NOT FOUND when patient not found")
    void updatePatient_NotFound() throws Exception {
        // Simule le comportement du service : lance une PatientNotFoundException
        when(patientService.updatePatient(eq(2L), any(PatientDto.class)))
                .thenThrow(new PatientNotFoundException("Patient not found"));

        // Exécute une requête PUT et vérifie la réponse
        mockMvc.perform(put("/api/v1/patients/{id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patientDto)))
                .andExpect(status().isNotFound()); // Attend un statut 404
    }

    /**
     * Teste la gestion des entrées invalides lors de la mise à jour d'un patient.
     * Vérifie que le contrôleur retourne un statut HTTP 400 (Bad Request)
     * lorsque les données du DTO de mise à jour sont invalides.
     */
    @Test
    @DisplayName("updatePatient - Should return 400 BAD REQUEST for invalid input")
    void updatePatient_InvalidInput() throws Exception {
        // Crée un DTO invalide avec des champs vides pour déclencher la validation
        PatientDto invalidDto = new PatientDto(
                1L, "", "", null, "", "", null
        );

        // Exécute une requête PUT avec le DTO invalide et vérifie la réponse
        mockMvc.perform(put("/api/v1/patients/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest()); // Attend un statut 400
    }
}

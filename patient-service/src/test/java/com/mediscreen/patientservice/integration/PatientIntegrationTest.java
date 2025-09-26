package com.mediscreen.patientservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mediscreen.patientservice.dto.*;
import java.util.Optional;
import com.mediscreen.patientservice.entity.Patient;
import com.mediscreen.patientservice.repository.PatientRepository;
import com.mediscreen.patientservice.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests d'intégration simples pour le PatientService
 * Utilise H2 in-memory (bonnes pratiques Spring Boot)
 * MySQL réservé pour la production (conformité OpenClassrooms)
 */
@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@TestPropertySource(properties = {
    "AUTH_USERNAME=test-user",
    "AUTH_PASSWORD=test-pass"
})
class PatientIntegrationTest {
    
    @Autowired
    private PatientService patientService;
    
    @Autowired
    private PatientRepository patientRepository;
    
    /**
     * Initialise le contexte de test avant chaque exécution.
     * Nettoie la base de données H2 en mémoire pour assurer l'indépendance des tests.
     */
    @BeforeEach
    void setUp() {
        // Nettoie la base avant chaque test
        patientRepository.deleteAll();
    }
    
    /**
     * Teste la création d'un patient avec des données conformes aux exigences d'OpenClassrooms.
     * Vérifie que le patient est correctement créé et enregistré en base de données.
     */
    @Test
    @DisplayName("Integration - Creer patient OpenClassrooms")
    void createPatient_Integration_Success() {
        // Given - Donnees conformes OpenClassrooms
        AdresseDto adresse = AdresseDto.of("1 Brookside St", "New York", "10001", "USA");
        PatientCreateDto patientCreateDto = new PatientCreateDto(
                "Test", "TestNone", LocalDate.of(1966, 12, 31),
                "F", "100-222-3333", adresse
        );
        
        // When
        PatientDto result = patientService.createPatient(patientCreateDto);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.nom()).isEqualTo("TestNone");
        assertThat(result.prenom()).isEqualTo("Test");
        assertThat(result.telephone()).isEqualTo("100-222-3333");
        
        // Verification en base H2
        List<Patient> patients = patientRepository.findAll();
        assertThat(patients).hasSize(1);
        assertThat(patients.get(0).getNom()).isEqualTo("TestNone");
    }
    
    /**
     * Teste les opérations CRUD (Create, Read, Update) pour un patient.
     * Vérifie la persistance et la modification des données patient en base H2.
     */
    @Test
    @DisplayName("Integration - CRUD patient avec H2")
    void crudPatient_Integration_Success() {
        // CREATE
        AdresseDto adresse = AdresseDto.of("2 High St", "New York", "10001", "USA");
        PatientCreateDto createDto = new PatientCreateDto(
                "Test", "TestBorderline", LocalDate.of(1945, 6, 24),
                "M", "200-333-4444", adresse
        );
        
        PatientDto createdPatient = patientService.createPatient(createDto);
        Long patientId = createdPatient.id();
        
        // READ
        Optional<PatientDto> foundPatient = patientService.getPatientById(patientId);
        assertThat(foundPatient).isPresent();
        assertThat(foundPatient.get().nom()).isEqualTo("TestBorderline");
        
        // UPDATE
        PatientDto updateDto = new PatientDto(
                patientId, "TestModifie", "TestBorderline", LocalDate.of(1945, 6, 24),
                "M", "200-333-4444", adresse
        );
        
        PatientDto updatedPatient = patientService.updatePatient(patientId, updateDto);
        assertThat(updatedPatient.prenom()).isEqualTo("TestModifie");
        
        // Verification en base H2
        Optional<Patient> inDb = patientRepository.findById(patientId);
        assertThat(inDb).isPresent();
        assertThat(inDb.get().getPrenom()).isEqualTo("TestModifie");
    }
    
    /**
     * Teste la récupération de tous les patients après la création des 4 patients de test OpenClassrooms.
     * Vérifie que la liste retournée contient les patients attendus et que les données sont correctes.
     */
    @Test
    @DisplayName("Integration - Les 4 patients OpenClassrooms")
    void getAllPatients_OpenClassroomsData_Success() {
        // Given - Creation des 4 patients de test OpenClassrooms
        AdresseDto adresse1 = AdresseDto.of("1 Brookside St", "New York", "10001", "USA");
        AdresseDto adresse2 = AdresseDto.of("2 High St", "New York", "10001", "USA");
        AdresseDto adresse3 = AdresseDto.of("3 Club Road", "New York", "10001", "USA");
        AdresseDto adresse4 = AdresseDto.of("4 Valley Dr", "New York", "10001", "USA");
        
        List<PatientCreateDto> openClassroomsPatients = List.of(
                new PatientCreateDto("Test", "TestNone", LocalDate.of(1966, 12, 31),
                        "F", "100-222-3333", adresse1),
                new PatientCreateDto("Test", "TestBorderline", LocalDate.of(1945, 6, 24),
                        "M", "200-333-4444", adresse2),
                new PatientCreateDto("Test", "TestInDanger", LocalDate.of(2004, 6, 18),
                        "M", "300-444-5555", adresse3),
                new PatientCreateDto("Test", "TestEarlyOnset", LocalDate.of(2002, 6, 28),
                        "F", "400-555-6666", adresse4)
        );
        
        // Creation des patients
        for (PatientCreateDto patientDto : openClassroomsPatients) {
            patientService.createPatient(patientDto);
        }
        
        // When
        List<PatientDto> result = patientService.getAllPatients();
        
        // Then
        assertThat(result).hasSize(4);
        assertThat(result.stream().map(PatientDto::prenom))
                .allMatch(prenom -> prenom.equals("Test"));
        assertThat(result.stream().map(PatientDto::nom))
                .containsExactlyInAnyOrder("TestNone", "TestBorderline", "TestInDanger", "TestEarlyOnset");
        
        // Verification en base H2
        List<Patient> patientsInDb = patientRepository.findAll();
        assertThat(patientsInDb).hasSize(4);
    }
    
    /**
     * Teste le scénario où un patient n'est pas trouvé par son ID.
     * Vérifie qu'un Optional vide est retourné dans ce cas.
     */
    @Test
    @DisplayName("Integration - Patient non trouve")
    void getPatient_NotFound_Integration() {
        // When
        Optional<PatientDto> result = patientService.getPatientById(999L);
        
        // Then
        assertThat(result).isEmpty();
    }

}
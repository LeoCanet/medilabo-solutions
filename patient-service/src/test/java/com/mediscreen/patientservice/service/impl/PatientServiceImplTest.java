package com.mediscreen.patientservice.service.impl;

import com.mediscreen.patientservice.dto.PatientCreateDto;
import com.mediscreen.patientservice.dto.PatientDto;
import com.mediscreen.patientservice.entity.Patient;
import com.mediscreen.patientservice.exception.PatientNotFoundException;
import com.mediscreen.patientservice.mapper.PatientMapper;
import com.mediscreen.patientservice.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour l'implémentation du service patient {@link PatientServiceImpl}.
 * Utilise Mockito pour simuler les dépendances comme {@link PatientRepository} et {@link PatientMapper},
 * assurant ainsi que seule la logique du service est testée de manière isolée.
 */
@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository; // Mock du repository pour contrôler le comportement de la base de données.

    @Mock
    private PatientMapper patientMapper; // Mock du mapper pour contrôler les conversions DTO/Entité.

    @InjectMocks
    private PatientServiceImpl patientService; // Instance du service à tester, avec les mocks injectés.

    private Patient patient;
    private PatientDto patientDto;
    private PatientCreateDto patientCreateDto;

    /**
     * Initialisation des objets de test avant chaque méthode de test.
     * Crée des instances de Patient, PatientDto et PatientCreateDto pour les scénarios de test.
     */
    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId(1L);
        patient.setPrenom("Test");
        patient.setNom("TestNone");
        patient.setDateNaissance(LocalDate.of(1966, 12, 31));
        patient.setGenre("F");
        patient.setTelephone("100-222-3333");

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
     * Vérifie que le service appelle le mapper pour convertir le DTO en entité,
     * sauvegarde l'entité via le repository, puis reconvertit l'entité sauvegardée en DTO.
     */
    @Test
    @DisplayName("createPatient - Should create a patient successfully")
    void createPatient_Success() {
        // Configure les mocks pour simuler le flux de création
        when(patientMapper.toEntity(patientCreateDto)).thenReturn(patient);
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);
        when(patientMapper.toDto(patient)).thenReturn(patientDto);

        // Appelle la méthode du service à tester
        PatientDto result = patientService.createPatient(patientCreateDto);

        // Vérifie le résultat et les interactions avec les mocks
        assertThat(result).isNotNull();
        assertThat(result.nom()).isEqualTo("TestNone");
        verify(patientRepository, times(1)).save(any(Patient.class)); // Vérifie que save a été appelé une fois
    }

    /**
     * Teste la récupération d'un patient par son ID lorsque le patient est trouvé.
     * Vérifie que le service appelle le repository et le mapper pour retourner le DTO du patient.
     */
    @Test
    @DisplayName("getPatientById - Should return patient when found")
    void getPatientById_Found() {
        // Configure les mocks pour simuler la recherche d'un patient existant
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientMapper.toDto(patient)).thenReturn(patientDto);

        // Appelle la méthode du service à tester
        Optional<PatientDto> result = patientService.getPatientById(1L);

        // Vérifie le résultat
        assertThat(result).isPresent();
        assertThat(result.get().nom()).isEqualTo("TestNone");
    }

    /**
     * Teste la récupération d'un patient par son ID lorsque le patient n'est pas trouvé.
     * Vérifie que le service retourne un Optional vide.
     */
    @Test
    @DisplayName("getPatientById - Should return empty when not found")
    void getPatientById_NotFound() {
        // Configure le mock pour simuler un patient non trouvé
        when(patientRepository.findById(2L)).thenReturn(Optional.empty());

        // Appelle la méthode du service à tester
        Optional<PatientDto> result = patientService.getPatientById(2L);

        // Vérifie le résultat
        assertThat(result).isEmpty();
    }

    /**
     * Teste la récupération de tous les patients lorsque la base de données contient des patients.
     * Vérifie que le service appelle le repository et le mapper pour retourner une liste de DTOs.
     */
    @Test
    @DisplayName("getAllPatients - Should return all patients")
    void getAllPatients_Success() {
        // Crée une liste de patients et de DTOs pour la simulation
        List<Patient> patients = Arrays.asList(patient, new Patient());
        List<PatientDto> patientDtos = Arrays.asList(patientDto, new PatientDto(
                2L, "Test2", "TestTwo", LocalDate.of(1990, 1, 1),
                "M", "222-333-4444", null
        ));

        // Configure les mocks
        when(patientRepository.findAll()).thenReturn(patients);
        when(patientMapper.toDtoList(patients)).thenReturn(patientDtos);

        // Appelle la méthode du service à tester
        List<PatientDto> result = patientService.getAllPatients();

        // Vérifie le résultat
        assertThat(result).hasSize(2);
        assertThat(result.get(0).nom()).isEqualTo("TestNone");
    }

    /**
     * Teste la récupération de tous les patients lorsque la base de données est vide.
     * Vérifie que le service retourne une liste vide de DTOs.
     */
    @Test
    @DisplayName("getAllPatients - Should return empty list when no patients")
    void getAllPatients_EmptyList() {
        // Configure les mocks pour simuler une base de données vide
        when(patientRepository.findAll()).thenReturn(Arrays.asList());
        when(patientMapper.toDtoList(anyList())).thenReturn(Arrays.asList());

        // Appelle la méthode du service à tester
        List<PatientDto> result = patientService.getAllPatients();

        // Vérifie le résultat
        assertThat(result).isEmpty();
    }

    /**
     * Teste la mise à jour réussie d'un patient existant.
     * Vérifie que le service trouve le patient, le met à jour via le mapper,
     * le sauvegarde et retourne le DTO mis à jour.
     */
    @Test
    @DisplayName("updatePatient - Should update patient successfully")
    void updatePatient_Success() {
        // Configure les mocks pour simuler la mise à jour
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientMapper.toEntity(patientDto)).thenReturn(patient);
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);
        when(patientMapper.toDto(patient)).thenReturn(patientDto);

        // Appelle la méthode du service à tester
        PatientDto result = patientService.updatePatient(1L, patientDto);

        // Vérifie le résultat et les interactions
        assertThat(result).isNotNull();
        assertThat(result.nom()).isEqualTo("TestNone");
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    /**
     * Teste la mise à jour d'un patient lorsque le patient n'est pas trouvé.
     * Vérifie que le service lève une {@link PatientNotFoundException}
     * et qu'aucune sauvegarde n'est effectuée.
     */
    @Test
    @DisplayName("updatePatient - Should throw PatientNotFoundException when patient not found")
    void updatePatient_NotFound() {
        // Configure le mock pour simuler un patient non trouvé
        when(patientRepository.findById(2L)).thenReturn(Optional.empty());

        // Vérifie que l'exception est levée
        assertThrows(PatientNotFoundException.class, () -> patientService.updatePatient(2L, patientDto));
        verify(patientRepository, never()).save(any(Patient.class)); // Vérifie qu'aucune sauvegarde n'a eu lieu
    }
}


    

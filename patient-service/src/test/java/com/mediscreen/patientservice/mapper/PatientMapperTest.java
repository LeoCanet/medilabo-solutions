package com.mediscreen.patientservice.mapper;

import com.mediscreen.patientservice.dto.PatientCreateDto;
import com.mediscreen.patientservice.dto.PatientDto;
import com.mediscreen.patientservice.entity.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires pour l'interface {@link PatientMapper}.
 * Vérifie le bon fonctionnement des méthodes de mappage entre les entités Patient et les DTOs correspondants.
 */
class PatientMapperTest {

    private PatientMapper patientMapper; // Instance du mapper à tester.

    /**
     * Initialisation du mapper avant chaque test.
     * Utilise MapStruct's Mappers.getMapper pour obtenir une instance du mapper.
     */
    @BeforeEach
    void setUp() {
        patientMapper = Mappers.getMapper(PatientMapper.class);
    }

    /**
     * Teste la conversion d'une entité {@link Patient} en {@link PatientDto}.
     * Vérifie que tous les champs pertinents sont correctement mappés.
     */
    @Test
    @DisplayName("toDto - Should map Patient entity to PatientDto")
    void toDto_ShouldMapPatientEntityToPatientDto() {
        // Crée une entité Patient pour le test
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setPrenom("Test");
        patient.setNom("TestNone");
        patient.setDateNaissance(LocalDate.of(1966, 12, 31));
        patient.setGenre("F");
        patient.setTelephone("100-222-3333");

        // Effectue le mappage
        PatientDto dto = patientMapper.toDto(patient);

        // Vérifie que le DTO n'est pas nul et que les champs sont correctement mappés
        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(patient.getId());
        assertThat(dto.prenom()).isEqualTo(patient.getPrenom());
        assertThat(dto.nom()).isEqualTo(patient.getNom());
        assertThat(dto.dateNaissance()).isEqualTo(patient.getDateNaissance());
        assertThat(dto.genre()).isEqualTo(patient.getGenre());
        assertThat(dto.telephone()).isEqualTo(patient.getTelephone());
    }

    /**
     * Teste la conversion d'une liste d'entités {@link Patient} en une liste de {@link PatientDto}.
     * Vérifie que la taille de la liste et les IDs des éléments sont corrects.
     */
    @Test
    @DisplayName("toDtoList - Should map list of Patient entities to list of PatientDto")
    void toDtoList_ShouldMapListOfPatientEntitiesToListOfPatientDto() {
        // Crée deux entités Patient pour la liste
        Patient patient1 = new Patient();
        patient1.setId(1L);
        patient1.setPrenom("Test1");
        patient1.setNom("TestNone1");

        Patient patient2 = new Patient();
        patient2.setId(2L);
        patient2.setPrenom("Test2");
        patient2.setNom("TestNone2");

        // Effectue le mappage de la liste
        List<Patient> patients = Arrays.asList(patient1, patient2);
        List<PatientDto> dtos = patientMapper.toDtoList(patients);

        // Vérifie que la liste de DTOs n'est pas nulle, a la bonne taille et que les IDs sont corrects
        assertThat(dtos).isNotNull().hasSize(2);
        assertThat(dtos.get(0).id()).isEqualTo(patient1.getId());
        assertThat(dtos.get(1).id()).isEqualTo(patient2.getId());
    }

    /**
     * Teste la conversion d'un {@link PatientCreateDto} en une entité {@link Patient}.
     * Vérifie que les champs sont correctement mappés et que l'ID est nul (pour une nouvelle entité).
     */
    @Test
    @DisplayName("toEntity - Should map PatientCreateDto to Patient entity")
    void toEntity_ShouldMapPatientCreateDtoToPatientEntity() {
        // Crée un DTO de création de patient
        PatientCreateDto createDto = new PatientCreateDto(
                "New", "Patient", LocalDate.of(2000, 1, 1),
                "M", "555-123-4567", null
        );

        // Effectue le mappage
        Patient entity = patientMapper.toEntity(createDto);

        // Vérifie que l'entité n'est pas nulle, que l'ID est nul et que les autres champs sont mappés
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isNull(); // L'ID doit être nul pour une nouvelle entité
        assertThat(entity.getPrenom()).isEqualTo(createDto.prenom());
        assertThat(entity.getNom()).isEqualTo(createDto.nom());
        assertThat(entity.getDateNaissance()).isEqualTo(createDto.dateNaissance());
        assertThat(entity.getGenre()).isEqualTo(createDto.genre());
        assertThat(entity.getTelephone()).isEqualTo(createDto.telephone());
    }

    /**
     * Teste la conversion d'un {@link PatientDto} en une entité {@link Patient} pour une mise à jour.
     * Vérifie que tous les champs, y compris l'ID, sont correctement mappés.
     */
    @Test
    @DisplayName("toEntity - Should map PatientDto to Patient entity for update")
    void toEntity_ShouldMapPatientDtoToPatientEntityForUpdate() {
        // Crée un DTO de patient pour la mise à jour
        PatientDto patientDto = new PatientDto(
                1L, "Updated", "Name", LocalDate.of(1980, 5, 10),
                "F", "111-222-3333", null
        );

        // Effectue le mappage
        Patient entity = patientMapper.toEntity(patientDto);

        // Vérifie que l'entité n'est pas nulle et que les champs sont correctement mappés
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(patientDto.id());
        assertThat(entity.getPrenom()).isEqualTo(patientDto.prenom());
        assertThat(entity.getNom()).isEqualTo(patientDto.nom());
        assertThat(entity.getDateNaissance()).isEqualTo(patientDto.dateNaissance());
        assertThat(entity.getGenre()).isEqualTo(patientDto.genre());
        assertThat(entity.getTelephone()).isEqualTo(patientDto.telephone());
    }

    /**
     * Teste la mise à jour d'une entité {@link Patient} existante à partir d'un {@link PatientDto}.
     * Vérifie que les champs non nuls du DTO mettent à jour l'entité, tandis que les champs nuls sont ignorés.
     * Ceci est crucial pour la stratégie `nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE`.
     */
    @Test
    @DisplayName("updatePatientFromDto - Should update existing Patient entity from PatientDto ignoring nulls")
    void updatePatientFromDto_ShouldUpdateExistingPatientEntityFromPatientDtoIgnoringNulls() {
        // Crée une entité Patient existante avec des valeurs initiales
        Patient existingPatient = new Patient();
        existingPatient.setId(1L);
        existingPatient.setPrenom("Original");
        existingPatient.setNom("Name");
        existingPatient.setDateNaissance(LocalDate.of(1990, 1, 1));
        existingPatient.setGenre("M");
        existingPatient.setTelephone("123-456-7890");

        // Crée un DTO de mise à jour avec certains champs nuls
        PatientDto updateDto = new PatientDto(
                1L, "Updated", null, null, // Nom et DateNaissance sont nuls dans le DTO
                "F", "987-654-3210", null
        );

        // Effectue la mise à jour de l'entité existante
        patientMapper.updatePatientFromDto(updateDto, existingPatient);

        // Vérifie que l'entité a été mise à jour correctement, en ignorant les champs nuls du DTO
        assertThat(existingPatient).isNotNull();
        assertThat(existingPatient.getId()).isEqualTo(1L); // L'ID doit rester le même
        assertThat(existingPatient.getPrenom()).isEqualTo("Updated");
        assertThat(existingPatient.getNom()).isEqualTo("Name"); // Doit rester l'original car nul dans le DTO
        assertThat(existingPatient.getDateNaissance()).isEqualTo(LocalDate.of(1990, 1, 1)); // Doit rester l'original
        assertThat(existingPatient.getGenre()).isEqualTo("F");
        assertThat(existingPatient.getTelephone()).isEqualTo("987-654-3210");
    }
}


    

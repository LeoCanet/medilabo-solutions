package com.mediscreen.assessmentservice.service;

import com.mediscreen.assessmentservice.client.NotesApiClient;
import com.mediscreen.assessmentservice.client.PatientApiClient;
import com.mediscreen.assessmentservice.dto.AdresseDto;
import com.mediscreen.assessmentservice.dto.NoteDto;
import com.mediscreen.assessmentservice.dto.PatientDto;
import com.mediscreen.assessmentservice.enums.RiskLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires pour AssessmentService
 * Teste l'algorithme d'évaluation du risque diabète avec des mocks
 */
@DisplayName("Tests unitaires - AssessmentService")
@ExtendWith(MockitoExtension.class)
class AssessmentServiceTest {

    @Mock
    private PatientApiClient patientApiClient;

    @Mock
    private NotesApiClient notesApiClient;

    @Mock
    private DiabetesTermsService diabetesTermsService;

    @InjectMocks
    private AssessmentService assessmentService;

    private PatientDto patientMale30;
    private PatientDto patientFemale30;
    private PatientDto patientMaleYoung;
    private PatientDto patientFemaleYoung;
    private PatientDto patientOld;

    @BeforeEach
    void setUp() {
        // Patient homme 30 ans exactement
        patientMale30 = new PatientDto(
                1L,
                "Test",
                "Male30",
                LocalDate.now().minusYears(30),
                "M",
                "111-222-3333",
                new AdresseDto("1 Test St", null, null, null)
        );

        // Patient femme 30 ans exactement
        patientFemale30 = new PatientDto(
                2L,
                "Test",
                "Female30",
                LocalDate.now().minusYears(30),
                "F",
                "222-333-4444",
                new AdresseDto("2 Test St", null, null, null)
        );

        // Patient homme 25 ans (jeune)
        patientMaleYoung = new PatientDto(
                3L,
                "Test",
                "MaleYoung",
                LocalDate.now().minusYears(25),
                "M",
                "333-444-5555",
                new AdresseDto("3 Test St", null, null, null)
        );

        // Patient femme 25 ans (jeune)
        patientFemaleYoung = new PatientDto(
                4L,
                "Test",
                "FemaleYoung",
                LocalDate.now().minusYears(25),
                "F",
                "444-555-6666",
                new AdresseDto("4 Test St", null, null, null)
        );

        // Patient 65 ans (âgé)
        patientOld = new PatientDto(
                5L,
                "Test",
                "Old",
                LocalDate.now().minusYears(65),
                "M",
                "555-666-7777",
                new AdresseDto("5 Test St", null, null, null)
        );
    }

    // ========== TESTS NONE (0 terme) ==========

    @Test
    @DisplayName("Devrait retourner NONE quand 0 terme déclencheur")
    void shouldReturnNoneWhenZeroTriggerTerms() {
        // Given
        when(patientApiClient.getPatientById(1L)).thenReturn(patientMale30);
        when(notesApiClient.getNotesByPatientId(anyInt())).thenReturn(List.of());
        when(diabetesTermsService.countTriggerTerms(any())).thenReturn(0);

        // When
        RiskLevel risk = assessmentService.assessDiabetesRisk(1L);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.NONE);
    }

    // ========== TESTS BORDERLINE (>30 ans, 2-5 termes) ==========

    @Test
    @DisplayName("Devrait retourner BORDERLINE pour patient >30 ans avec 2 termes")
    void shouldReturnBorderlineWhenOver30With2Terms() {
        // Given
        when(patientApiClient.getPatientById(5L)).thenReturn(patientOld);
        when(notesApiClient.getNotesByPatientId(anyInt())).thenReturn(List.of(
                new NoteDto("1", 5, "Test Old", "Note avec anormal", LocalDateTime.now())
        ));
        when(diabetesTermsService.countTriggerTerms(any())).thenReturn(2);

        // When
        RiskLevel risk = assessmentService.assessDiabetesRisk(5L);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.BORDERLINE);
    }

    @Test
    @DisplayName("Devrait retourner BORDERLINE pour patient >30 ans avec 5 termes")
    void shouldReturnBorderlineWhenOver30With5Terms() {
        // Given
        when(patientApiClient.getPatientById(5L)).thenReturn(patientOld);
        when(notesApiClient.getNotesByPatientId(anyInt())).thenReturn(List.of(
                new NoteDto("1", 5, "Test Old", "Note", LocalDateTime.now())
        ));
        when(diabetesTermsService.countTriggerTerms(any())).thenReturn(5);

        // When
        RiskLevel risk = assessmentService.assessDiabetesRisk(5L);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.BORDERLINE);
    }

    // ========== TESTS IN_DANGER ==========

    @Test
    @DisplayName("Devrait retourner IN_DANGER pour homme <30 ans avec 3 termes")
    void shouldReturnInDangerForMaleUnder30With3Terms() {
        // Given
        when(patientApiClient.getPatientById(3L)).thenReturn(patientMaleYoung);
        when(notesApiClient.getNotesByPatientId(anyInt())).thenReturn(List.of(
                new NoteDto("1", 3, "Test MaleYoung", "Note", LocalDateTime.now())
        ));
        when(diabetesTermsService.countTriggerTerms(any())).thenReturn(3);

        // When
        RiskLevel risk = assessmentService.assessDiabetesRisk(3L);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.IN_DANGER);
    }

    @Test
    @DisplayName("Devrait retourner IN_DANGER pour homme <30 ans avec 4 termes")
    void shouldReturnInDangerForMaleUnder30With4Terms() {
        // Given
        when(patientApiClient.getPatientById(3L)).thenReturn(patientMaleYoung);
        when(notesApiClient.getNotesByPatientId(anyInt())).thenReturn(List.of(
                new NoteDto("1", 3, "Test MaleYoung", "Note", LocalDateTime.now())
        ));
        when(diabetesTermsService.countTriggerTerms(any())).thenReturn(4);

        // When
        RiskLevel risk = assessmentService.assessDiabetesRisk(3L);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.IN_DANGER);
    }

    @Test
    @DisplayName("Devrait retourner IN_DANGER pour femme <30 ans avec 4 termes")
    void shouldReturnInDangerForFemaleUnder30With4Terms() {
        // Given
        when(patientApiClient.getPatientById(4L)).thenReturn(patientFemaleYoung);
        when(notesApiClient.getNotesByPatientId(anyInt())).thenReturn(List.of(
                new NoteDto("1", 4, "Test FemaleYoung", "Note", LocalDateTime.now())
        ));
        when(diabetesTermsService.countTriggerTerms(any())).thenReturn(4);

        // When
        RiskLevel risk = assessmentService.assessDiabetesRisk(4L);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.IN_DANGER);
    }

    @Test
    @DisplayName("Devrait retourner IN_DANGER pour femme <30 ans avec 6 termes")
    void shouldReturnInDangerForFemaleUnder30With6Terms() {
        // Given
        when(patientApiClient.getPatientById(4L)).thenReturn(patientFemaleYoung);
        when(notesApiClient.getNotesByPatientId(anyInt())).thenReturn(List.of(
                new NoteDto("1", 4, "Test FemaleYoung", "Note", LocalDateTime.now())
        ));
        when(diabetesTermsService.countTriggerTerms(any())).thenReturn(6);

        // When
        RiskLevel risk = assessmentService.assessDiabetesRisk(4L);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.IN_DANGER);
    }

    @Test
    @DisplayName("Devrait retourner IN_DANGER pour patient >30 ans avec 6 termes")
    void shouldReturnInDangerForOver30With6Terms() {
        // Given
        when(patientApiClient.getPatientById(5L)).thenReturn(patientOld);
        when(notesApiClient.getNotesByPatientId(anyInt())).thenReturn(List.of(
                new NoteDto("1", 5, "Test Old", "Note", LocalDateTime.now())
        ));
        when(diabetesTermsService.countTriggerTerms(any())).thenReturn(6);

        // When
        RiskLevel risk = assessmentService.assessDiabetesRisk(5L);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.IN_DANGER);
    }

    @Test
    @DisplayName("Devrait retourner IN_DANGER pour patient >30 ans avec 7 termes")
    void shouldReturnInDangerForOver30With7Terms() {
        // Given
        when(patientApiClient.getPatientById(5L)).thenReturn(patientOld);
        when(notesApiClient.getNotesByPatientId(anyInt())).thenReturn(List.of(
                new NoteDto("1", 5, "Test Old", "Note", LocalDateTime.now())
        ));
        when(diabetesTermsService.countTriggerTerms(any())).thenReturn(7);

        // When
        RiskLevel risk = assessmentService.assessDiabetesRisk(5L);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.IN_DANGER);
    }

    // ========== TESTS EARLY_ONSET ==========

    @Test
    @DisplayName("Devrait retourner EARLY_ONSET pour homme <30 ans avec 5 termes")
    void shouldReturnEarlyOnsetForMaleUnder30With5Terms() {
        // Given
        when(patientApiClient.getPatientById(3L)).thenReturn(patientMaleYoung);
        when(notesApiClient.getNotesByPatientId(anyInt())).thenReturn(List.of(
                new NoteDto("1", 3, "Test MaleYoung", "Note", LocalDateTime.now())
        ));
        when(diabetesTermsService.countTriggerTerms(any())).thenReturn(5);

        // When
        RiskLevel risk = assessmentService.assessDiabetesRisk(3L);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.EARLY_ONSET);
    }

    @Test
    @DisplayName("Devrait retourner EARLY_ONSET pour femme <30 ans avec 7 termes")
    void shouldReturnEarlyOnsetForFemaleUnder30With7Terms() {
        // Given
        when(patientApiClient.getPatientById(4L)).thenReturn(patientFemaleYoung);
        when(notesApiClient.getNotesByPatientId(anyInt())).thenReturn(List.of(
                new NoteDto("1", 4, "Test FemaleYoung", "Note", LocalDateTime.now())
        ));
        when(diabetesTermsService.countTriggerTerms(any())).thenReturn(7);

        // When
        RiskLevel risk = assessmentService.assessDiabetesRisk(4L);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.EARLY_ONSET);
    }

    @Test
    @DisplayName("Devrait retourner EARLY_ONSET pour patient >30 ans avec 8 termes")
    void shouldReturnEarlyOnsetForOver30With8Terms() {
        // Given
        when(patientApiClient.getPatientById(5L)).thenReturn(patientOld);
        when(notesApiClient.getNotesByPatientId(anyInt())).thenReturn(List.of(
                new NoteDto("1", 5, "Test Old", "Note", LocalDateTime.now())
        ));
        when(diabetesTermsService.countTriggerTerms(any())).thenReturn(8);

        // When
        RiskLevel risk = assessmentService.assessDiabetesRisk(5L);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.EARLY_ONSET);
    }

    @Test
    @DisplayName("Devrait retourner EARLY_ONSET pour patient >30 ans avec 10 termes")
    void shouldReturnEarlyOnsetForOver30With10Terms() {
        // Given
        when(patientApiClient.getPatientById(5L)).thenReturn(patientOld);
        when(notesApiClient.getNotesByPatientId(anyInt())).thenReturn(List.of(
                new NoteDto("1", 5, "Test Old", "Note", LocalDateTime.now())
        ));
        when(diabetesTermsService.countTriggerTerms(any())).thenReturn(10);

        // When
        RiskLevel risk = assessmentService.assessDiabetesRisk(5L);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.EARLY_ONSET);
    }

    // ========== TESTS CAS LIMITES ==========

    @Test
    @DisplayName("Devrait retourner NONE pour homme <30 ans avec 2 termes (insuffisant)")
    void shouldReturnNoneForMaleUnder30With2Terms() {
        // Given
        when(patientApiClient.getPatientById(3L)).thenReturn(patientMaleYoung);
        when(notesApiClient.getNotesByPatientId(anyInt())).thenReturn(List.of(
                new NoteDto("1", 3, "Test MaleYoung", "Note", LocalDateTime.now())
        ));
        when(diabetesTermsService.countTriggerTerms(any())).thenReturn(2);

        // When
        RiskLevel risk = assessmentService.assessDiabetesRisk(3L);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.NONE);
    }

    @Test
    @DisplayName("Devrait retourner NONE pour femme <30 ans avec 3 termes (insuffisant)")
    void shouldReturnNoneForFemaleUnder30With3Terms() {
        // Given
        when(patientApiClient.getPatientById(4L)).thenReturn(patientFemaleYoung);
        when(notesApiClient.getNotesByPatientId(anyInt())).thenReturn(List.of(
                new NoteDto("1", 4, "Test FemaleYoung", "Note", LocalDateTime.now())
        ));
        when(diabetesTermsService.countTriggerTerms(any())).thenReturn(3);

        // When
        RiskLevel risk = assessmentService.assessDiabetesRisk(4L);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.NONE);
    }

    @Test
    @DisplayName("Devrait retourner NONE pour patient >30 ans avec 1 terme (insuffisant)")
    void shouldReturnNoneForOver30With1Term() {
        // Given
        when(patientApiClient.getPatientById(5L)).thenReturn(patientOld);
        when(notesApiClient.getNotesByPatientId(anyInt())).thenReturn(List.of(
                new NoteDto("1", 5, "Test Old", "Note", LocalDateTime.now())
        ));
        when(diabetesTermsService.countTriggerTerms(any())).thenReturn(1);

        // When
        RiskLevel risk = assessmentService.assessDiabetesRisk(5L);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.NONE);
    }
}

package com.mediscreen.assessmentservice.service;

import com.mediscreen.assessmentservice.client.NotesApiClient;
import com.mediscreen.assessmentservice.client.PatientApiClient;
import com.mediscreen.assessmentservice.dto.AdresseDto;
import com.mediscreen.assessmentservice.dto.NoteDto;
import com.mediscreen.assessmentservice.dto.PatientDto;
import com.mediscreen.assessmentservice.enums.RiskLevel;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires AssessmentService (Orchestration)
 *
 * Focus : Coordination des appels aux services externes
 * Architecture : Séparation responsabilités (recommandations mentor)
 * - Tests algorithme → DiabetesRiskCalculatorTest (18 tests SANS mocks)
 * - Tests orchestration → AssessmentServiceTest (ces tests)
 *
 * Avantages :
 * - Tests orchestration simples et ciblés
 * - Mock du calculateur (algorithme testé ailleurs)
 * - Validation coordination services
 */
@DisplayName("Tests unitaires - AssessmentService (Orchestration)")
@ExtendWith(MockitoExtension.class)
class AssessmentServiceTest {

    @Mock
    private PatientApiClient patientApiClient;

    @Mock
    private NotesApiClient notesApiClient;

    @Mock
    private DiabetesTermsService diabetesTermsService;

    @Mock
    private DiabetesRiskCalculator riskCalculator;

    @InjectMocks
    private AssessmentService assessmentService;

    @Test
    @DisplayName("Orchestration : Doit coordonner tous les services et retourner le risque calculé")
    void shouldOrchestrateAllServicesAndReturnCalculatedRisk() {
        // Given
        Long patientId = 1L;
        PatientDto patient = new PatientDto(
                1L,
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "M",
                "111-222-3333",
                new AdresseDto("1 Test St", null, null, null)
        );

        List<NoteDto> notes = List.of(
                new NoteDto("1", 1, "Note 1", "Fumeur", LocalDateTime.now()),
                new NoteDto("2", 1, "Note 2", "Cholestérol", LocalDateTime.now())
        );

        // Mock des appels externes
        when(patientApiClient.getPatientById(patientId)).thenReturn(patient);
        when(notesApiClient.getNotesByPatientId(1)).thenReturn(notes);
        when(diabetesTermsService.countTriggerTerms(anyString())).thenReturn(2);
        when(riskCalculator.calculateRisk(anyInt(), anyBoolean(), eq(2)))
                .thenReturn(RiskLevel.BORDERLINE);

        // When
        RiskLevel result = assessmentService.assessDiabetesRisk(patientId);

        // Then
        assertThat(result).isEqualTo(RiskLevel.BORDERLINE);

        // Vérification orchestration complète dans le bon ordre
        verify(patientApiClient).getPatientById(patientId);
        verify(notesApiClient).getNotesByPatientId(1);
        verify(diabetesTermsService).countTriggerTerms(anyString());
        verify(riskCalculator).calculateRisk(
                patient.getAge(),
                patient.isMale(),
                2
        );
    }

    @Test
    @DisplayName("Orchestration : Doit combiner toutes les notes avant comptage termes")
    void shouldCombineAllNotesBeforeCountingTerms() {
        // Given
        Long patientId = 2L;
        PatientDto patient = new PatientDto(
                2L,
                "Jane",
                "Smith",
                LocalDate.of(1985, 6, 15),
                "F",
                "222-333-4444",
                null
        );

        List<NoteDto> notes = List.of(
                new NoteDto("1", 2, "Note 1", "Fumeur anormal", LocalDateTime.now()),
                new NoteDto("2", 2, "Note 2", "Cholestérol réaction", LocalDateTime.now()),
                new NoteDto("3", 2, "Note 3", "Vertige", LocalDateTime.now())
        );

        when(patientApiClient.getPatientById(patientId)).thenReturn(patient);
        when(notesApiClient.getNotesByPatientId(2)).thenReturn(notes);
        when(diabetesTermsService.countTriggerTerms(" Fumeur anormal Cholestérol réaction Vertige"))
                .thenReturn(5);
        when(riskCalculator.calculateRisk(anyInt(), anyBoolean(), eq(5)))
                .thenReturn(RiskLevel.BORDERLINE);

        // When
        RiskLevel result = assessmentService.assessDiabetesRisk(patientId);

        // Then
        assertThat(result).isEqualTo(RiskLevel.BORDERLINE);

        // Vérification combinaison notes (reduce ajoute espace au début)
        verify(diabetesTermsService).countTriggerTerms(" Fumeur anormal Cholestérol réaction Vertige");
    }

    @Test
    @DisplayName("Orchestration : Doit passer les bonnes données au calculateur (âge, genre, termes)")
    void shouldPassCorrectDataToCalculator() {
        // Given
        Long patientId = 3L;
        PatientDto patientYoungMale = new PatientDto(
                3L,
                "Young",
                "Male",
                LocalDate.now().minusYears(25), // 25 ans
                "M",
                "333-444-5555",
                null
        );

        when(patientApiClient.getPatientById(patientId)).thenReturn(patientYoungMale);
        when(notesApiClient.getNotesByPatientId(3)).thenReturn(List.of());
        when(diabetesTermsService.countTriggerTerms(anyString())).thenReturn(4);
        when(riskCalculator.calculateRisk(25, true, 4))
                .thenReturn(RiskLevel.IN_DANGER);

        // When
        RiskLevel result = assessmentService.assessDiabetesRisk(patientId);

        // Then
        assertThat(result).isEqualTo(RiskLevel.IN_DANGER);

        // Vérification paramètres exacts passés au calculateur
        verify(riskCalculator).calculateRisk(
                25,      // âge exact
                true,    // isMale
                4        // termes comptés
        );
    }

    @Test
    @DisplayName("Orchestration : Doit gérer patient sans notes (liste vide)")
    void shouldHandlePatientWithoutNotes() {
        // Given
        Long patientId = 4L;
        PatientDto patient = new PatientDto(
                4L,
                "No",
                "Notes",
                LocalDate.of(1980, 3, 10),
                "F",
                "444-555-6666",
                null
        );

        when(patientApiClient.getPatientById(patientId)).thenReturn(patient);
        when(notesApiClient.getNotesByPatientId(4)).thenReturn(List.of()); // Aucune note
        when(diabetesTermsService.countTriggerTerms("")).thenReturn(0);
        when(riskCalculator.calculateRisk(anyInt(), anyBoolean(), eq(0)))
                .thenReturn(RiskLevel.NONE);

        // When
        RiskLevel result = assessmentService.assessDiabetesRisk(patientId);

        // Then
        assertThat(result).isEqualTo(RiskLevel.NONE);

        // Vérification appels même avec liste vide
        verify(notesApiClient).getNotesByPatientId(4);
        verify(diabetesTermsService).countTriggerTerms("");
        verify(riskCalculator).calculateRisk(patient.getAge(), patient.isMale(), 0);
    }
}

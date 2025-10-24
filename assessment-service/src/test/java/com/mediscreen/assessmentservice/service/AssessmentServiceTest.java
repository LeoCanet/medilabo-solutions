package com.mediscreen.assessmentservice.service;

import com.mediscreen.assessmentservice.client.NotesApiClient;
import com.mediscreen.assessmentservice.client.PatientApiClient;
import com.mediscreen.assessmentservice.dto.AdresseDto;
import com.mediscreen.assessmentservice.dto.AssessmentResponse;
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
 * Tests unitaires AssessmentService
 *
 * Focus : Tests du calcul de risque SANS mocks des clients API
 * Architecture : Séparation responsabilités
 * - assessDiabetesRisk(PatientDto, List<NoteDto>) = Calcul pur (tests sans API clients)
 * - getAssessmentResponse(Long) = Orchestration (tests avec mocks API)
 *
 * Avantages :
 * - Tests calcul plus simples (pas de mocks API nécessaires)
 * - Tests orchestration valident appels API et construction réponse
 * - Meilleure séparation des préoccupations
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

    @Mock
    private DiabetesRiskCalculator riskCalculator;

    @InjectMocks
    private AssessmentService assessmentService;

    @Test
    @DisplayName("Calcul risque : Doit combiner notes, compter termes et calculer risque")
    void shouldCombineNotesCountTermsAndCalculateRisk() {
        // Given
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

        // Mock du comptage de termes et calcul risque (pas d'API clients)
        when(diabetesTermsService.countTriggerTerms(anyString())).thenReturn(2);
        when(riskCalculator.calculateRisk(anyInt(), anyBoolean(), eq(2)))
                .thenReturn(RiskLevel.BORDERLINE);

        // When
        RiskLevel result = assessmentService.assessDiabetesRisk(patient, notes);

        // Then
        assertThat(result).isEqualTo(RiskLevel.BORDERLINE);

        // Vérification calcul sans appels API
        verify(diabetesTermsService).countTriggerTerms(anyString());
        verify(riskCalculator).calculateRisk(
                patient.getAge(),
                patient.isMale(),
                2
        );

        // Vérification que les API clients ne sont PAS appelés
        verifyNoInteractions(patientApiClient);
        verifyNoInteractions(notesApiClient);
    }

    @Test
    @DisplayName("Calcul risque : Doit combiner toutes les notes avant comptage termes")
    void shouldCombineAllNotesBeforeCountingTerms() {
        // Given
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

        when(diabetesTermsService.countTriggerTerms(" Fumeur anormal Cholestérol réaction Vertige"))
                .thenReturn(5);
        when(riskCalculator.calculateRisk(anyInt(), anyBoolean(), eq(5)))
                .thenReturn(RiskLevel.BORDERLINE);

        // When
        RiskLevel result = assessmentService.assessDiabetesRisk(patient, notes);

        // Then
        assertThat(result).isEqualTo(RiskLevel.BORDERLINE);

        // Vérification combinaison notes (reduce ajoute espace au début)
        verify(diabetesTermsService).countTriggerTerms(" Fumeur anormal Cholestérol réaction Vertige");
    }

    @Test
    @DisplayName("Calcul risque : Doit passer les bonnes données au calculateur (âge, genre, termes)")
    void shouldPassCorrectDataToCalculator() {
        // Given
        PatientDto patientYoungMale = new PatientDto(
                3L,
                "Young",
                "Male",
                LocalDate.now().minusYears(25), // 25 ans
                "M",
                "333-444-5555",
                null
        );

        List<NoteDto> notes = List.of();

        when(diabetesTermsService.countTriggerTerms(anyString())).thenReturn(4);
        when(riskCalculator.calculateRisk(25, true, 4))
                .thenReturn(RiskLevel.IN_DANGER);

        // When
        RiskLevel result = assessmentService.assessDiabetesRisk(patientYoungMale, notes);

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
    @DisplayName("Calcul risque : Doit gérer patient sans notes (liste vide)")
    void shouldHandlePatientWithoutNotes() {
        // Given
        PatientDto patient = new PatientDto(
                4L,
                "No",
                "Notes",
                LocalDate.of(1980, 3, 10),
                "F",
                "444-555-6666",
                null
        );

        List<NoteDto> notes = List.of(); // Aucune note

        when(diabetesTermsService.countTriggerTerms("")).thenReturn(0);
        when(riskCalculator.calculateRisk(anyInt(), anyBoolean(), eq(0)))
                .thenReturn(RiskLevel.NONE);

        // When
        RiskLevel result = assessmentService.assessDiabetesRisk(patient, notes);

        // Then
        assertThat(result).isEqualTo(RiskLevel.NONE);

        // Vérification appels même avec liste vide
        verify(diabetesTermsService).countTriggerTerms("");
        verify(riskCalculator).calculateRisk(patient.getAge(), patient.isMale(), 0);
    }

    @Test
    @DisplayName("Orchestration : Doit construire AssessmentResponse en appelant API UNE SEULE FOIS")
    void shouldBuildCompleteAssessmentResponseWithSingleApiCall() {
        // Given
        Long patientId = 5L;
        PatientDto patient = new PatientDto(
                5L,
                "Test",
                "TestBorderline",
                LocalDate.of(1945, 6, 24), // > 30 ans
                "M",
                "200-333-4444",
                new AdresseDto("2 High St", null, null, null)
        );

        List<NoteDto> notes = List.of(
                new NoteDto("1", 5, "Note 1", "Anormal", LocalDateTime.now()),
                new NoteDto("2", 5, "Note 2", "Réaction", LocalDateTime.now())
        );

        // Mock des appels API (orchestration)
        when(patientApiClient.getPatientById(patientId)).thenReturn(patient);
        when(notesApiClient.getNotesByPatientId(5)).thenReturn(notes);
        when(diabetesTermsService.countTriggerTerms(anyString())).thenReturn(2);
        when(riskCalculator.calculateRisk(anyInt(), anyBoolean(), eq(2)))
                .thenReturn(RiskLevel.BORDERLINE);

        // When
        AssessmentResponse response = assessmentService.getAssessmentResponse(patientId);

        // Then - Vérification réponse complète construite
        assertThat(response).isNotNull();
        assertThat(response.patientId()).isEqualTo(5L);
        assertThat(response.patientName()).isEqualTo("Test TestBorderline");
        assertThat(response.patientAge()).isEqualTo(patient.getAge());
        assertThat(response.patientGender()).isEqualTo("M");
        assertThat(response.riskLevel()).isEqualTo(RiskLevel.BORDERLINE);
        assertThat(response.riskDescription()).isEqualTo(RiskLevel.BORDERLINE.getDescription());
        assertThat(response.assessmentDate()).isNotNull();

        // Vérification CRITIQUE : API appelée UNE SEULE FOIS (plus de double appel)
        verify(patientApiClient, times(1)).getPatientById(patientId);
        verify(notesApiClient, times(1)).getNotesByPatientId(5);
        verify(diabetesTermsService, times(1)).countTriggerTerms(anyString());
        verify(riskCalculator, times(1)).calculateRisk(anyInt(), anyBoolean(), eq(2));
    }
}

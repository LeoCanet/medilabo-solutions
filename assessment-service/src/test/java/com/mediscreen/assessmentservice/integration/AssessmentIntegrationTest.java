package com.mediscreen.assessmentservice.integration;

import com.mediscreen.assessmentservice.client.NotesApiClient;
import com.mediscreen.assessmentservice.client.PatientApiClient;
import com.mediscreen.assessmentservice.dto.AdresseDto;
import com.mediscreen.assessmentservice.dto.AssessmentResponse;
import com.mediscreen.assessmentservice.dto.NoteDto;
import com.mediscreen.assessmentservice.dto.PatientDto;
import com.mediscreen.assessmentservice.enums.RiskLevel;
import com.mediscreen.assessmentservice.service.AssessmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * Tests d'intégration pour l'évaluation du risque diabète
 * Valide les 4 cas obligatoires OpenClassrooms avec données de test exactes
 *
 * Architecture testée :
 * - getAssessmentResponse(Long) = Orchestration complète (API mockées)
 * - Services réels : DiabetesTermsService + DiabetesRiskCalculator
 * - Validation flux complet avec résultats OpenClassrooms attendus
 */
@SpringBootTest
@TestPropertySource(properties = {
        "mediscreen.auth.username=test-assessment",
        "mediscreen.auth.password=test-pass",
        "AUTH_USERNAME=test-user",
        "AUTH_PASSWORD=test-pass"
})
@DisplayName("Tests intégration - 4 cas OpenClassrooms obligatoires")
class AssessmentIntegrationTest {

    @Autowired
    private AssessmentService assessmentService;

    @MockBean
    private PatientApiClient patientApiClient;

    @MockBean
    private NotesApiClient notesApiClient;

    // ========== PATIENT 1 : TestNone ==========
    private PatientDto patient1None;
    private List<NoteDto> patient1Notes;

    // ========== PATIENT 2 : TestBorderline ==========
    private PatientDto patient2Borderline;
    private List<NoteDto> patient2Notes;

    // ========== PATIENT 3 : TestInDanger ==========
    private PatientDto patient3InDanger;
    private List<NoteDto> patient3Notes;

    // ========== PATIENT 4 : TestEarlyOnset ==========
    private PatientDto patient4EarlyOnset;
    private List<NoteDto> patient4Notes;

    @BeforeEach
    void setUp() {
        setupPatient1None();
        setupPatient2Borderline();
        setupPatient3InDanger();
        setupPatient4EarlyOnset();
    }

    /**
     * PATIENT 1 : TestNone
     * - Nom: TestNone
     * - Prénom: Test
     * - Date naissance: 1966-12-31 (58 ans, donc >30)
     * - Genre: F
     * - Notes: "Le patient déclare qu'il 'se sent très bien' Poids égal ou inférieur au poids recommandé"
     * - Termes déclencheurs UNIQUES: 1 (Poids apparaît 2 fois mais compte 1)
     * - ATTENDU: NONE (1 seul terme unique)
     */
    private void setupPatient1None() {
        patient1None = new PatientDto(
                1L,
                "Test",
                "TestNone",
                LocalDate.of(1966, 12, 31),
                "F",
                "100-222-3333",
                new AdresseDto("1 Brookside St", null, null, null)
        );

        patient1Notes = List.of(
                new NoteDto(
                        "1",
                        1,
                        "Test TestNone",
                        "Le patient déclare qu'il 'se sent très bien' Poids égal ou inférieur au poids recommandé",
                        LocalDateTime.now()
                )
        );
    }

    /**
     * PATIENT 2 : TestBorderline
     * - Nom: TestBorderline
     * - Prénom: Test
     * - Date naissance: 1945-06-24 (79 ans, donc >30)
     * - Genre: M
     * - Notes: 2 notes avec "anormal" (2 fois) et "réaction" (1 fois)
     * - Termes déclencheurs UNIQUES: 2 (anormal + réaction)
     * - ATTENDU: BORDERLINE (2 termes entre 2-5 ET >30 ans)
     */
    private void setupPatient2Borderline() {
        patient2Borderline = new PatientDto(
                2L,
                "Test",
                "TestBorderline",
                LocalDate.of(1945, 6, 24),
                "M",
                "200-333-4444",
                new AdresseDto("2 High St", null, null, null)
        );

        patient2Notes = List.of(
                new NoteDto(
                        "2a",
                        2,
                        "Test TestBorderline",
                        "Le patient déclare qu'il ressent beaucoup de stress au travail " +
                        "Il se plaint également que son audition est anormale dernièrement",
                        LocalDateTime.now().minusMonths(6)
                ),
                new NoteDto(
                        "2b",
                        2,
                        "Test TestBorderline",
                        "Le patient déclare avoir fait une réaction aux médicaments au cours des 3 derniers mois " +
                        "Il remarque également que son audition continue d'être anormale",
                        LocalDateTime.now()
                )
        );
    }

    /**
     * PATIENT 3 : TestInDanger
     * - Nom: TestInDanger
     * - Prénom: Test
     * - Date naissance: 2004-06-18 (20 ans, donc <30)
     * - Genre: M
     * - Notes: 2 notes avec "fumeur" (2 fois), "anormal" (1 fois), "cholestérol" (1 fois)
     * - Termes déclencheurs UNIQUES: 3 (fumeur + anormal + cholestérol)
     * - ATTENDU: IN_DANGER (homme <30 ans avec 3 termes, seuil 3+)
     */
    private void setupPatient3InDanger() {
        patient3InDanger = new PatientDto(
                3L,
                "Test",
                "TestInDanger",
                LocalDate.of(2004, 6, 18),
                "M",
                "300-444-5555",
                new AdresseDto("3 Club Road", null, null, null)
        );

        patient3Notes = List.of(
                new NoteDto(
                        "3a",
                        3,
                        "Test TestInDanger",
                        "Le patient déclare qu'il fume depuis peu",
                        LocalDateTime.now().minusMonths(12)
                ),
                new NoteDto(
                        "3b",
                        3,
                        "Test TestInDanger",
                        "Le patient déclare qu'il est fumeur et qu'il a cessé de fumer l'année dernière " +
                        "Il se plaint également de crises d'apnée respiratoire anormales " +
                        "Tests de laboratoire indiquant un taux de cholestérol LDL élevé",
                        LocalDateTime.now()
                )
        );
    }

    /**
     * PATIENT 4 : TestEarlyOnset
     * - Nom: TestEarlyOnset
     * - Prénom: Test
     * - Date naissance: 2002-06-28 (22 ans, donc <30)
     * - Genre: F
     * - Notes: 4 notes avec termes déclencheurs UNIQUES:
     *   * "Anticorps" (1 occurrence comptée 1 fois)
     *   * "Réaction" (2 occurrences comptées 1 fois)
     *   * "Fumeur" (1 occurrence comptée 1 fois)
     *   * "Hémoglobine A1C" (1 occurrence comptée 1 fois)
     *   * "Taille" (1 occurrence comptée 1 fois)
     *   * "Poids" (1 occurrence comptée 1 fois)
     *   * "Cholestérol" (1 occurrence comptée 1 fois)
     *   * "Vertige" (1 occurrence comptée 1 fois)
     * - Termes déclencheurs UNIQUES: 8 total
     * - ATTENDU: EARLY_ONSET (femme <30 ans avec 8 termes, seuil 7+)
     */
    private void setupPatient4EarlyOnset() {
        patient4EarlyOnset = new PatientDto(
                4L,
                "Test",
                "TestEarlyOnset",
                LocalDate.of(2002, 6, 28),
                "F",
                "400-555-6666",
                new AdresseDto("4 Valley Dr", null, null, null)
        );

        patient4Notes = List.of(
                new NoteDto(
                        "4a",
                        4,
                        "Test TestEarlyOnset",
                        "Le patient déclare qu'il lui est devenu difficile de monter les escaliers " +
                        "Il se plaint également d'être essoufflé " +
                        "Tests de laboratoire indiquant que les anticorps sont élevés " +
                        "Réaction aux médicaments",
                        LocalDateTime.now().minusMonths(9)
                ),
                new NoteDto(
                        "4b",
                        4,
                        "Test TestEarlyOnset",
                        "Le patient déclare qu'il a mal au dos lorsqu'il reste assis pendant longtemps",
                        LocalDateTime.now().minusMonths(6)
                ),
                new NoteDto(
                        "4c",
                        4,
                        "Test TestEarlyOnset",
                        "Le patient déclare avoir commencé à fumer depuis peu " +
                        "Hémoglobine A1C supérieure au niveau recommandé",
                        LocalDateTime.now().minusMonths(3)
                ),
                new NoteDto(
                        "4d",
                        4,
                        "Test TestEarlyOnset",
                        "Taille, Poids, Cholestérol, Vertige et Réaction",
                        LocalDateTime.now()
                )
        );
    }

    // ========== TESTS DES 4 CAS OPENCLASSROOMS ==========

    @Test
    @DisplayName("CAS 1 OpenClassrooms - Patient TestNone devrait retourner NONE (1 terme unique 'Poids')")
    void testCase1_TestNone_ShouldReturnNone() {
        // Given
        when(patientApiClient.getPatientById(1L)).thenReturn(patient1None);
        when(notesApiClient.getNotesByPatientId(1)).thenReturn(patient1Notes);

        // When
        AssessmentResponse response = assessmentService.getAssessmentResponse(1L);

        // Then
        assertThat(response.riskLevel()).isEqualTo(RiskLevel.NONE);
        assertThat(response.patientAge()).isGreaterThan(30);
        assertThat(response.patientName()).isEqualTo("Test TestNone");
    }

    @Test
    @DisplayName("CAS 2 OpenClassrooms - Patient TestBorderline devrait retourner BORDERLINE (2 termes uniques, >30 ans)")
    void testCase2_TestBorderline_ShouldReturnBorderline() {
        // Given
        when(patientApiClient.getPatientById(2L)).thenReturn(patient2Borderline);
        when(notesApiClient.getNotesByPatientId(2)).thenReturn(patient2Notes);

        // When
        AssessmentResponse response = assessmentService.getAssessmentResponse(2L);

        // Then
        assertThat(response.riskLevel()).isEqualTo(RiskLevel.BORDERLINE);
        assertThat(response.patientAge()).isGreaterThan(30);
        assertThat(response.patientName()).isEqualTo("Test TestBorderline");
    }

    @Test
    @DisplayName("CAS 3 OpenClassrooms - Patient TestInDanger devrait retourner IN_DANGER (3 termes uniques, homme <30 ans)")
    void testCase3_TestInDanger_ShouldReturnInDanger() {
        // Given
        when(patientApiClient.getPatientById(3L)).thenReturn(patient3InDanger);
        when(notesApiClient.getNotesByPatientId(3)).thenReturn(patient3Notes);

        // When
        AssessmentResponse response = assessmentService.getAssessmentResponse(3L);

        // Then
        assertThat(response.riskLevel()).isEqualTo(RiskLevel.IN_DANGER);
        assertThat(response.patientAge()).isLessThanOrEqualTo(30);
        assertThat(response.patientGender()).isEqualTo("M");
        assertThat(response.patientName()).isEqualTo("Test TestInDanger");
    }

    @Test
    @DisplayName("CAS 4 OpenClassrooms - Patient TestEarlyOnset devrait retourner EARLY_ONSET (8 termes uniques, femme <30 ans)")
    void testCase4_TestEarlyOnset_ShouldReturnEarlyOnset() {
        // Given
        when(patientApiClient.getPatientById(4L)).thenReturn(patient4EarlyOnset);
        when(notesApiClient.getNotesByPatientId(4)).thenReturn(patient4Notes);

        // When
        AssessmentResponse response = assessmentService.getAssessmentResponse(4L);

        // Then
        assertThat(response.riskLevel()).isEqualTo(RiskLevel.EARLY_ONSET);
        assertThat(response.patientAge()).isLessThanOrEqualTo(30);
        assertThat(response.patientGender()).isEqualTo("F");
        assertThat(response.patientName()).isEqualTo("Test TestEarlyOnset");
    }

    // ========== TEST COMPLET DES 4 CAS EN UNE FOIS ==========

    @Test
    @DisplayName("VALIDATION COMPLETE - Les 4 cas OpenClassrooms retournent les résultats attendus")
    void testAllOpenClassroomsCases() {
        // Given - Mock tous les patients
        when(patientApiClient.getPatientById(1L)).thenReturn(patient1None);
        when(notesApiClient.getNotesByPatientId(1)).thenReturn(patient1Notes);

        when(patientApiClient.getPatientById(2L)).thenReturn(patient2Borderline);
        when(notesApiClient.getNotesByPatientId(2)).thenReturn(patient2Notes);

        when(patientApiClient.getPatientById(3L)).thenReturn(patient3InDanger);
        when(notesApiClient.getNotesByPatientId(3)).thenReturn(patient3Notes);

        when(patientApiClient.getPatientById(4L)).thenReturn(patient4EarlyOnset);
        when(notesApiClient.getNotesByPatientId(4)).thenReturn(patient4Notes);

        // When - Évaluer les 4 patients avec orchestration complète
        AssessmentResponse response1 = assessmentService.getAssessmentResponse(1L);
        AssessmentResponse response2 = assessmentService.getAssessmentResponse(2L);
        AssessmentResponse response3 = assessmentService.getAssessmentResponse(3L);
        AssessmentResponse response4 = assessmentService.getAssessmentResponse(4L);

        // Then - Vérifier les résultats attendus
        assertThat(response1.riskLevel())
                .as("Patient 1 (TestNone) devrait être NONE")
                .isEqualTo(RiskLevel.NONE);

        assertThat(response2.riskLevel())
                .as("Patient 2 (TestBorderline) devrait être BORDERLINE")
                .isEqualTo(RiskLevel.BORDERLINE);

        assertThat(response3.riskLevel())
                .as("Patient 3 (TestInDanger) devrait être IN_DANGER")
                .isEqualTo(RiskLevel.IN_DANGER);

        assertThat(response4.riskLevel())
                .as("Patient 4 (TestEarlyOnset) devrait être EARLY_ONSET")
                .isEqualTo(RiskLevel.EARLY_ONSET);
    }
}

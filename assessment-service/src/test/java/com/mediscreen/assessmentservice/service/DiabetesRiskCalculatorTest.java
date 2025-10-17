package com.mediscreen.assessmentservice.service;

import com.mediscreen.assessmentservice.enums.RiskLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires DiabetesRiskCalculator (algorithme pur)
 *
 * AUCUN mock nécessaire - Tests simples et rapides
 * Entrées simples (âge, genre, termes) → Sortie (RiskLevel)
 *
 * Avantages :
 * - Tests ultra-rapides (pas de contexte Spring, pas de mocks)
 * - Lisibilité parfaite
 * - Couverture exhaustive facile
 */
@DisplayName("Tests unitaires - DiabetesRiskCalculator (algorithme pur)")
class DiabetesRiskCalculatorTest {

    private DiabetesRiskCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new DiabetesRiskCalculator(); // ✅ Pas de mocks !
    }

    // ========== TESTS NONE (0 terme) ==========

    @Test
    @DisplayName("NONE : 0 terme déclencheur")
    void shouldReturnNoneWhenZeroTerms() {
        // When
        RiskLevel risk = calculator.calculateRisk(50, true, 0);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.NONE);
    }

    // ========== TESTS BORDERLINE (>30 ans, 2-5 termes) ==========

    @Test
    @DisplayName("BORDERLINE : Patient >30 ans avec 2 termes")
    void shouldReturnBorderlineForOver30With2Terms() {
        // When
        RiskLevel risk = calculator.calculateRisk(65, true, 2);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.BORDERLINE);
    }

    @Test
    @DisplayName("BORDERLINE : Patient >30 ans avec 5 termes")
    void shouldReturnBorderlineForOver30With5Terms() {
        // When
        RiskLevel risk = calculator.calculateRisk(65, true, 5);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.BORDERLINE);
    }

    // ========== TESTS IN_DANGER ==========

    @Test
    @DisplayName("IN_DANGER : Homme <=30 ans avec 3 termes")
    void shouldReturnInDangerForMaleUnder30With3Terms() {
        // When
        RiskLevel risk = calculator.calculateRisk(25, true, 3);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.IN_DANGER);
    }

    @Test
    @DisplayName("IN_DANGER : Homme <=30 ans avec 4 termes")
    void shouldReturnInDangerForMaleUnder30With4Terms() {
        // When
        RiskLevel risk = calculator.calculateRisk(25, true, 4);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.IN_DANGER);
    }

    @Test
    @DisplayName("IN_DANGER : Femme <=30 ans avec 4 termes")
    void shouldReturnInDangerForFemaleUnder30With4Terms() {
        // When
        RiskLevel risk = calculator.calculateRisk(25, false, 4);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.IN_DANGER);
    }

    @Test
    @DisplayName("IN_DANGER : Femme <=30 ans avec 6 termes")
    void shouldReturnInDangerForFemaleUnder30With6Terms() {
        // When
        RiskLevel risk = calculator.calculateRisk(25, false, 6);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.IN_DANGER);
    }

    @Test
    @DisplayName("IN_DANGER : Patient >30 ans avec 6 termes")
    void shouldReturnInDangerForOver30With6Terms() {
        // When
        RiskLevel risk = calculator.calculateRisk(65, true, 6);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.IN_DANGER);
    }

    @Test
    @DisplayName("IN_DANGER : Patient >30 ans avec 7 termes")
    void shouldReturnInDangerForOver30With7Terms() {
        // When
        RiskLevel risk = calculator.calculateRisk(65, true, 7);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.IN_DANGER);
    }

    // ========== TESTS EARLY_ONSET ==========

    @Test
    @DisplayName("EARLY_ONSET : Homme <=30 ans avec 5 termes")
    void shouldReturnEarlyOnsetForMaleUnder30With5Terms() {
        // When
        RiskLevel risk = calculator.calculateRisk(25, true, 5);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.EARLY_ONSET);
    }

    @Test
    @DisplayName("EARLY_ONSET : Femme <=30 ans avec 7 termes")
    void shouldReturnEarlyOnsetForFemaleUnder30With7Terms() {
        // When
        RiskLevel risk = calculator.calculateRisk(25, false, 7);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.EARLY_ONSET);
    }

    @Test
    @DisplayName("EARLY_ONSET : Patient >30 ans avec 8 termes")
    void shouldReturnEarlyOnsetForOver30With8Terms() {
        // When
        RiskLevel risk = calculator.calculateRisk(65, true, 8);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.EARLY_ONSET);
    }

    @Test
    @DisplayName("EARLY_ONSET : Patient >30 ans avec 10 termes")
    void shouldReturnEarlyOnsetForOver30With10Terms() {
        // When
        RiskLevel risk = calculator.calculateRisk(65, true, 10);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.EARLY_ONSET);
    }

    // ========== TESTS CAS LIMITES ==========

    @Test
    @DisplayName("NONE : Homme <=30 ans avec 2 termes (insuffisant)")
    void shouldReturnNoneForMaleUnder30With2Terms() {
        // When
        RiskLevel risk = calculator.calculateRisk(25, true, 2);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.NONE);
    }

    @Test
    @DisplayName("NONE : Femme <=30 ans avec 3 termes (insuffisant)")
    void shouldReturnNoneForFemaleUnder30With3Terms() {
        // When
        RiskLevel risk = calculator.calculateRisk(25, false, 3);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.NONE);
    }

    @Test
    @DisplayName("NONE : Patient >30 ans avec 1 terme (insuffisant)")
    void shouldReturnNoneForOver30With1Term() {
        // When
        RiskLevel risk = calculator.calculateRisk(65, true, 1);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.NONE);
    }

    @Test
    @DisplayName("Cas limite : Exactement 30 ans doit utiliser règles <=30")
    void shouldUseYoungRulesFor30YearsOld() {
        // When - Homme de 30 ans avec 3 termes
        RiskLevel risk = calculator.calculateRisk(30, true, 3);

        // Then - Doit appliquer règles <=30 ans (homme: 3+ termes = IN_DANGER)
        assertThat(risk).isEqualTo(RiskLevel.IN_DANGER);
    }

    @Test
    @DisplayName("Cas limite : Femme <=30 ans avec exactement 7 termes (EARLY_ONSET)")
    void shouldReturnEarlyOnsetForFemaleUnder30WithExactly7Terms() {
        // When
        RiskLevel risk = calculator.calculateRisk(23, false, 7);

        // Then
        assertThat(risk).isEqualTo(RiskLevel.EARLY_ONSET);
    }
}

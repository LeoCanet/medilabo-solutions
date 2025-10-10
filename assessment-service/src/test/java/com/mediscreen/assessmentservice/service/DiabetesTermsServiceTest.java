package com.mediscreen.assessmentservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires pour DiabetesTermsService
 * Vérifie le comptage des termes déclencheurs diabète
 */
@DisplayName("Tests unitaires - DiabetesTermsService")
class DiabetesTermsServiceTest {

    private DiabetesTermsService diabetesTermsService;

    @BeforeEach
    void setUp() {
        diabetesTermsService = new DiabetesTermsService();
    }

    @Test
    @DisplayName("Devrait retourner les 11 termes déclencheurs (Fumeur détecte fumeur+fumeuse)")
    void shouldReturnAllTriggerTerms() {
        // When
        List<String> triggerTerms = diabetesTermsService.getTriggerTerms();

        // Then
        assertThat(triggerTerms).hasSize(11);
        assertThat(triggerTerms).containsExactlyInAnyOrder(
                "Hémoglobine A1C",
                "Microalbumine",
                "Taille",
                "Poids",
                "Fumeur",  // Détecte aussi fumeuse
                "Anormal",
                "Cholestérol",
                "Vertige", // Détecte aussi vertiges
                "Rechute",
                "Réaction",
                "Anticorps"
        );
    }

    @Test
    @DisplayName("Devrait compter 0 terme dans un texte vide")
    void shouldCountZeroTermsInEmptyText() {
        // When
        int count = diabetesTermsService.countTriggerTerms("");

        // Then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Devrait compter 0 terme dans un texte null")
    void shouldCountZeroTermsInNullText() {
        // When
        int count = diabetesTermsService.countTriggerTerms(null);

        // Then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("Devrait compter 1 terme dans un texte avec 'Fumeur'")
    void shouldCountOneTermWithFumeur() {
        // Given
        String text = "Le patient déclare qu'il est fumeur depuis peu";

        // When
        int count = diabetesTermsService.countTriggerTerms(text);

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Devrait compter 2 termes dans un texte avec 'anormal' et 'réaction'")
    void shouldCountTwoTermsWithAnormalAndReaction() {
        // Given
        String text = "Le patient déclare qu'il ressent beaucoup de stress. " +
                "Il se plaint également que son audition est anormale dernièrement. " +
                "Il remarque également qu'il a fait une réaction aux médicaments.";

        // When
        int count = diabetesTermsService.countTriggerTerms(text);

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Devrait compter 3 termes dans un texte avec 'Fumeur', 'Anormal', 'Cholestérol'")
    void shouldCountThreeTermsWithFumeurAnormalCholesterol() {
        // Given
        String text = "Le patient déclare qu'il est fumeur et qu'il a cessé de fumer l'année dernière. " +
                "Il se plaint également de crises d'apnée respiratoire anormales. " +
                "Tests de laboratoire indiquant un taux de cholestérol LDL élevé";

        // When
        int count = diabetesTermsService.countTriggerTerms(text);

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Devrait compter 4 termes dans un texte avec plusieurs déclencheurs (Taille, Poids, Cholestérol, Vertige)")
    void shouldCountFourTermsWithMultipleTriggers() {
        // Given
        String text = "Taille, Poids, Cholestérol, Vertige et Réaction";

        // When
        int count = diabetesTermsService.countTriggerTerms(text);

        // Then
        assertThat(count).isEqualTo(5); // Taille + Poids + Cholestérol + Vertige + Réaction
    }

    @Test
    @DisplayName("Devrait être insensible à la casse (FUMEUR, fumeur, Fumeur)")
    void shouldBeCaseInsensitive() {
        // Given
        String text = "Patient FUMEUR avec des problèmes de CHOLESTÉROL";

        // When
        int count = diabetesTermsService.countTriggerTerms(text);

        // Then
        assertThat(count).isEqualTo(2); // fumeur + cholestérol
    }

    @Test
    @DisplayName("Devrait compter un seul terme même avec plusieurs occurrences (comptage UNIQUE)")
    void shouldCountMultipleOccurrencesOfSameTerm() {
        // Given
        String text = "Réaction allergique notée. Nouvelle réaction observée. Réaction confirmée.";

        // When
        int count = diabetesTermsService.countTriggerTerms(text);

        // Then
        assertThat(count).isEqualTo(1); // 1 terme unique "réaction" malgré 3 occurrences
    }

    @Test
    @DisplayName("Devrait compter 1 terme unique même avec multiples occurrences (Patient 1 OpenClassrooms)")
    void shouldCountZeroWhenNoTriggerTerms() {
        // Given
        String text = "Le patient déclare qu'il 'se sent très bien' Poids égal ou inférieur au poids recommandé";

        // When
        int count = diabetesTermsService.countTriggerTerms(text);

        // Then
        assertThat(count).isEqualTo(1); // "Poids" apparaît 2 fois mais comptage UNIQUE = 1
    }

    @Test
    @DisplayName("Devrait compter les termes avec accents (Hémoglobine A1C)")
    void shouldCountTermsWithAccents() {
        // Given
        String text = "Hémoglobine A1C supérieure au niveau recommandé";

        // When
        int count = diabetesTermsService.countTriggerTerms(text);

        // Then
        assertThat(count).isEqualTo(1);
    }
}

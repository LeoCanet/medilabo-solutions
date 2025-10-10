package com.mediscreen.assessmentservice.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Service pour la gestion des termes déclencheurs diabète
 *
 * Contient les 12 termes exacts selon les exigences OpenClassrooms.
 * Fournit les méthodes pour détecter ces termes dans les notes médicales.
 */
@Service
public class DiabetesTermsService {

    /**
     * Les 12 termes déclencheurs obligatoires OpenClassrooms
     * Note: "Fumeur" détecte automatiquement fumeur/fumeuse/fumer via contains("fum")
     * Note: "Vertige" détecte automatiquement vertige/vertiges
     * Note: "Anormal" détecte automatiquement anormal/anormale/anormaux/anormales
     */
    private static final List<String> TRIGGER_TERMS = Arrays.asList(
        "Hémoglobine A1C",
        "Microalbumine",
        "Taille",
        "Poids",
        "Fumeur",      // Détecte aussi: fume, fumer, fumeur, fumeuse
        "Anormal",     // Détecte aussi: anormal, anormale, anormales, anormaux
        "Cholestérol",
        "Vertige",     // Détecte aussi: vertige, vertiges
        "Rechute",
        "Réaction",
        "Anticorps"
    );

    /**
     * Retourne la liste des termes déclencheurs
     * @return Liste immuable des termes déclencheurs
     */
    public List<String> getTriggerTerms() {
        return List.copyOf(TRIGGER_TERMS);
    }

    /**
     * Compte le nombre de termes déclencheurs DIFFÉRENTS dans un texte
     * Gère les variantes grammaticales (singulier/pluriel, formes verbales)
     * @param text le texte à analyser
     * @return nombre de termes déclencheurs uniques trouvés
     */
    public int countTriggerTerms(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        String lowerText = text.toLowerCase();
        return (int) TRIGGER_TERMS.stream()
                .filter(term -> matchesTerm(lowerText, term))
                .count();
    }

    /**
     * Vérifie si un terme déclencheur est présent dans le texte
     * Gère les variantes grammaticales pour une détection robuste
     */
    private boolean matchesTerm(String text, String term) {
        String lowerTerm = term.toLowerCase();

        // Cas spéciaux avec variantes grammaticales
        if (lowerTerm.equals("fumeur")) {
            // Détecte: fumeur, fumeuse, fume, fumer, fumé, fumée
            return text.contains("fum");
        }
        if (lowerTerm.equals("anormal")) {
            // Détecte: anormal, anormale, anormales, anormaux
            return text.contains("anormal");
        }
        if (lowerTerm.equals("vertige")) {
            // Détecte: vertige, vertiges
            return text.contains("vertige");
        }

        // Cas général: recherche exacte (insensible à la casse)
        return text.contains(lowerTerm);
    }
}
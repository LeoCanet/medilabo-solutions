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
     * (insensibles à la casse lors de la recherche)
     */
    private static final List<String> TRIGGER_TERMS = Arrays.asList(
        "Hémoglobine A1C",
        "Microalbumine",
        "Taille",
        "Poids",
        "Fumeur",
        "Fumeuse",
        "Anormal",
        "Cholestérol",
        "Vertiges",
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
     * Compte le nombre de termes déclencheurs dans un texte
     * @param text le texte à analyser
     * @return nombre de termes déclencheurs trouvés
     */
    public int countTriggerTerms(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        String lowerText = text.toLowerCase();
        return (int) TRIGGER_TERMS.stream()
                .mapToLong(term -> countOccurrences(lowerText, term.toLowerCase()))
                .sum();
    }

    /**
     * Compte le nombre d'occurrences d'un terme dans un texte
     * @param text le texte où chercher
     * @param term le terme à chercher
     * @return nombre d'occurrences
     */
    private long countOccurrences(String text, String term) {
        if (text == null || term == null || text.isEmpty() || term.isEmpty()) {
            return 0;
        }

        int count = 0;
        int index = 0;
        while ((index = text.indexOf(term, index)) != -1) {
            count++;
            index += term.length();
        }
        return count;
    }
}
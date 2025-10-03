package com.mediscreen.assessmentservice.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * DTO Patient pour Assessment Service
 *
 * Simplifié par rapport au frontend - ne contient que les données
 * nécessaires pour l'évaluation du risque diabète (âge, genre).
 * Utilise le même format que les autres services pour cohérence.
 */
public record PatientDto(
    Long id,
    String prenom,
    String nom,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate dateNaissance,
    String genre,
    String telephone,
    AdresseDto adresse
) {

    /**
     * Calcule l'âge du patient à partir de la date de naissance
     * @return âge en années
     */
    public int getAge() {
        if (dateNaissance == null) {
            return 0;
        }
        return LocalDate.now().getYear() - dateNaissance.getYear();
    }

    /**
     * Vérifie si le patient est un homme
     * @return true si le genre est M/Masculin
     */
    public boolean isMale() {
        return genre != null &&
               (genre.equalsIgnoreCase("M") ||
                genre.equalsIgnoreCase("Masculin") ||
                genre.equalsIgnoreCase("Male"));
    }

    /**
     * Vérifie si le patient est une femme
     * @return true si le genre est F/Féminin
     */
    public boolean isFemale() {
        return genre != null &&
               (genre.equalsIgnoreCase("F") ||
                genre.equalsIgnoreCase("Féminin") ||
                genre.equalsIgnoreCase("Female"));
    }
}
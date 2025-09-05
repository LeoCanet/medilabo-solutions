package com.mediscreen.patientservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO Patient avec Record
 * Utilisé pour les échanges API REST
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PatientDto(
    Long id,

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 50, message = "Le prénom ne peut pas dépasser 50 caractères")
    String prenom,

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
    String nom,

    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    LocalDate dateNaissance,

    @NotNull(message = "Le genre est obligatoire")
    @Pattern(regexp = "[MF]", message = "Le genre doit être M ou F")
    String genre,

    @Size(max = 15, message = "Le téléphone ne peut pas dépasser 15 caractères")
    @Pattern(regexp = "^[0-9\\-+\\s]*$", message = "Format de téléphone invalide")
    String telephone,

    AdresseDto adresse
) {

    /**
     * Constructeur compact avec validation
     */
    public PatientDto {
        if (dateNaissance != null && dateNaissance.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La date de naissance ne peut pas être dans le futur");
        }
    }

    /**
     * Factory method pour créer un patient simple
     */
    public static PatientDto of(String prenom, String nom, LocalDate dateNaissance, String genre) {
        return new PatientDto(null, prenom, nom, dateNaissance, genre, null, null);
    }

    /**
     * Factory method pour créer un patient complet
     */
    public static PatientDto of(String prenom, String nom, LocalDate dateNaissance,
                               String genre, String telephone, AdresseDto adresse) {
        return new PatientDto(null, prenom, nom, dateNaissance, genre, telephone, adresse);
    }

    /**
     * Nom complet avec String Templates
     */
    public String getNomComplet() {
        return String.format("%s %s", prenom, nom);
    }

    /**
     * Description patient avec pattern matching
     */
    public String getDescription() {
        return switch (genre) {
            case "M" -> String.format("Monsieur %s", getNomComplet());
            case "F" -> String.format("Madame %s", getNomComplet());
            default -> getNomComplet();
        };
    }
}
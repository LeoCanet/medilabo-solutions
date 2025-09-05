package com.mediscreen.patientservice.dto;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;

/**
 * DTO pour la création de patient (sans ID)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PatientCreateDto(
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
     * Validation avec pattern matching
     */
    public PatientCreateDto {
        if (dateNaissance != null && dateNaissance.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La date de naissance doit être dans le passé");
        }
        
        
    }
}

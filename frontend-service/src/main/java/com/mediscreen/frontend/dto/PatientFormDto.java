package com.mediscreen.frontend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record PatientFormDto(
    Long id,

    @NotBlank(message = "Le nom de famille est obligatoire")
    @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
    String nom,

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 50, message = "Le prénom ne peut pas dépasser 50 caractères")
    String prenom,

    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate dateNaissance,

    @NotBlank(message = "Le genre est obligatoire")
    String genre,

    @Pattern(
        regexp = "^$|^(?:\\+33|0)[1-9](?:[ .-]?\\d{2}){4}$|^\\d{3}-\\d{3}-\\d{4}$",
        message = "Le téléphone doit être un numéro valide (ex: 0612345678, +33612345678, ou 100-222-3333)"
    )
    @Size(max = 15, message = "Le téléphone ne peut pas dépasser 15 caractères")
    String telephone,

    @NotBlank(message = "La rue est obligatoire")
    @Size(max = 100, message = "La rue ne peut pas dépasser 100 caractères")
    String rue,

    @Size(max = 50, message = "La ville ne peut pas dépasser 50 caractères")
    String ville,

    @Pattern(
        regexp = "^$|^\\d{5}$",
        message = "Le code postal doit contenir 5 chiffres"
    )
    @Size(max = 10, message = "Le code postal ne peut pas dépasser 10 caractères")
    String codePostal
) {
    public PatientFormDto withId(Long newId) {
        return new PatientFormDto(newId, this.nom, this.prenom, this.dateNaissance, this.genre, this.telephone, this.rue, this.ville, this.codePostal);
    }
}

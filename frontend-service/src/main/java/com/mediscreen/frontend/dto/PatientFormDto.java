package com.mediscreen.frontend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record PatientFormDto(
    Long id,

    @NotBlank(message = "Le nom de famille est obligatoire")
    String nom,

    @NotBlank(message = "Le prénom est obligatoire")
    String prenom,

    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate dateDeNaissance,

    @NotBlank(message = "Le genre est obligatoire")
    String genre,

    String telephone,

    @NotBlank(message = "La rue est obligatoire")
    String rue,

    String ville,

    String codePostal
) {}

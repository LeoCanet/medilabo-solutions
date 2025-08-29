package com.mediscreen.frontend.dto;

/**
 * DTO pour la création de patient, miroir du backend.
 */
public record PatientCreateDto(
    String prenom,
    String nom,
    java.time.LocalDate dateNaissance,
    String genre,
    String telephone,
    AdresseDto adresse
) {}

package com.mediscreen.frontend.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record PatientDto(
    Long id,
    String nom,
    String prenom,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate dateDeNaissance,
    String genre,
    String telephone,
    AdresseDto adresse
) {}

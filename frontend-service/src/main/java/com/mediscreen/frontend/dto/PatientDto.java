package com.mediscreen.frontend.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record PatientDto(
    Long id,
    String prenom,
    String nom,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate dateNaissance,
    String genre,
    String telephone,
    AdresseDto adresse
) {}

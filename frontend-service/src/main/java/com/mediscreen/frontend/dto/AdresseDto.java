package com.mediscreen.frontend.dto;

import jakarta.validation.constraints.NotBlank;

public record AdresseDto(
    @NotBlank(message = "La rue ne peut pas être vide")
    String rue,
    String ville,
    String codePostal
) {}

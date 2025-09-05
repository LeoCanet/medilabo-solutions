package com.mediscreen.frontend.dto;

import jakarta.validation.constraints.NotBlank;

public record AdresseDto(
    Long id,
    String rue,
    String ville,
    String codePostal,
    String pays
) {}

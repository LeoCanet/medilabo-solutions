package com.mediscreen.frontend.dto;

import jakarta.validation.constraints.*;

/**
 * DTO pour la création de note depuis le frontend
 */
public record NoteCreateDto(
    @NotNull(message = "L'ID du patient est obligatoire")
    @Positive(message = "L'ID du patient doit être positif")
    Integer patId,
    
    @NotBlank(message = "Le nom du patient est obligatoire")
    @Size(max = 100, message = "Le nom du patient ne peut pas dépasser 100 caractères")
    String patient,
    
    @NotBlank(message = "Le contenu de la note est obligatoire")
    @Size(max = 5000, message = "La note ne peut pas dépasser 5000 caractères")
    String note
) {}
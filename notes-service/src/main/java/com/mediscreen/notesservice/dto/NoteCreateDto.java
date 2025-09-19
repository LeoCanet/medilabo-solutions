package com.mediscreen.notesservice.dto;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO pour la création de note (sans ID ni date)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
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
) {
    
    /**
     * Factory method pour créer une note simple
     */
    public static NoteCreateDto of(Integer patId, String patient, String note) {
        return new NoteCreateDto(patId, patient, note);
    }
}
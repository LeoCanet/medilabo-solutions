package com.mediscreen.frontend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * DTO Note pour le frontend
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NoteDto(
    String id,

    @NotNull(message = "L'ID du patient est obligatoire")
    @Positive(message = "L'ID du patient doit être positif")
    Integer patId,

    @NotBlank(message = "Le nom du patient est obligatoire")
    @Size(max = 100, message = "Le nom du patient ne peut pas dépasser 100 caractères")
    String patient,

    @NotBlank(message = "Le contenu de la note est obligatoire")
    @Size(max = 5000, message = "La note ne peut pas dépasser 5000 caractères")
    String note,

    LocalDateTime createdDate
) {
    
    /**
     * Aperçu de la note (premiers 100 caractères)
     */
    public String getPreview() {
        if (note == null || note.length() <= 100) {
            return note;
        }
        return note.substring(0, 97) + "...";
    }
}
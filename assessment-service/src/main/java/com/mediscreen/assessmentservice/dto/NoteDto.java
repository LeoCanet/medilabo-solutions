package com.mediscreen.assessmentservice.dto;

import java.time.LocalDateTime;

/**
 * DTO Note pour Assessment Service
 *
 * Simplifié par rapport au frontend - contient uniquement les données
 * nécessaires pour l'algorithme d'évaluation du risque diabète.
 * Le contenu de la note sera analysé pour détecter les termes déclencheurs.
 */
public record NoteDto(
    String id,
    Integer patId,
    String patient,
    String note,
    LocalDateTime createdDate
) {

    /**
     * Vérifie si la note contient un terme spécifique (insensible à la casse)
     * @param terme le terme à rechercher
     * @return true si le terme est trouvé dans la note
     */
    public boolean containsTerm(String terme) {
        if (note == null || terme == null) {
            return false;
        }
        return note.toLowerCase().contains(terme.toLowerCase());
    }
}
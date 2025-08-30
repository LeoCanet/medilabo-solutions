package com.mediscreen.frontend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Représente la réponse d'erreur standard renvoyée par le backoffice.
 * Exemple attendu:
 * {
 *   "message": "Validation failed",
 *   "errors": [
 *     {"field":"telephone","message":"Format de téléphone invalide"}
 *   ]
 * }
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {
    private String message;
    private List<ApiFieldError> errors;
}

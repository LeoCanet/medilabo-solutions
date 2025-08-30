package com.mediscreen.patientservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Réponse d'erreur standard renvoyée par l'API patient-service.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {
    private String message;
    private List<ApiFieldError> errors;
}

package com.mediscreen.patientservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Représente une erreur de validation liée à un champ spécifique.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiFieldError {
    private String field;
    private String message;
}

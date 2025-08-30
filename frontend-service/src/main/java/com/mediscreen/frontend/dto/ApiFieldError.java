package com.mediscreen.frontend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("message")
    @JsonAlias({"defaultMessage"})
    private String message;
}

package com.mediscreen.patientservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.Period;

/**
 * DTO résumé patient pour les listes
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PatientSummaryDto(
    Long id,
    String prenom,
    String nom,
    LocalDate dateNaissance,
    String genre,
    Integer age
) {
    
    /**
     * Constructeur avec calcul automatique de l'âge
     */
    public PatientSummaryDto(Long id, String prenom, String nom, 
                            LocalDate dateNaissance, String genre) {
        this(id, prenom, nom, dateNaissance, genre, 
             dateNaissance != null ? 
                Period.between(dateNaissance, LocalDate.now()).getYears() : null);
    }
    
    /**
     * Nom complet avec String Templates
     */
    public String getNomComplet() {
        return STR."\{prenom} \{nom}";
    }
}
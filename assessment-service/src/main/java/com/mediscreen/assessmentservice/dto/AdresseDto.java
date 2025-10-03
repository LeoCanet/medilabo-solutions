package com.mediscreen.assessmentservice.dto;

/**
 * DTO Adresse pour Assessment Service
 *
 * Cohérent avec le format des autres services.
 * Simplifié car non utilisé pour l'algorithme de risque diabète.
 */
public record AdresseDto(
    String numero,
    String rue,
    String ville,
    String codePostal
) {}
package com.mediscreen.patientservice.dto;

import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO Adresse avec Record
 * Immutable, thread-safe et optimisé
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdresseDto(
    Long id,
    
    @Size(max = 100, message = "L'adresse ne peut pas dépasser 100 caractères")
    String rue,
    
    @Size(max = 50, message = "La ville ne peut pas dépasser 50 caractères")
    String ville,
    
    @Size(max = 10, message = "Le code postal ne peut pas dépasser 10 caractères")
    String codePostal,
    
    @Size(max = 50, message = "Le pays ne peut pas dépasser 50 caractères")
    String pays
) {
    
    /**
     * Constructeur compact avec validation
     */
    public AdresseDto {
        if (rue != null && rue.length() > 100) {
            throw new IllegalArgumentException("L'adresse ne peut pas dépasser 100 caractères");
        }
    }
    
    /**
     * Factory method pour créer une adresse complète
     */
    public static AdresseDto of(String rue, String ville, String codePostal, String pays) {
        return new AdresseDto(null, rue, ville, codePostal, pays);
    }
    
    /**
     * Retourne l'adresse formatée avec String Templates
     */
    public String getAdresseComplete() {
        String address = "";
        address += (rue != null ? rue : "");
        address += (ville != null ? (address.isEmpty() ? "" : ", ") + ville : "");
        address += (codePostal != null ? (address.isEmpty() ? "" : " ") + codePostal : "");
        address += (pays != null ? (address.isEmpty() ? "" : ", ") + pays : "");
        return address.trim().replaceAll("^,\\s*", "");
    }
}
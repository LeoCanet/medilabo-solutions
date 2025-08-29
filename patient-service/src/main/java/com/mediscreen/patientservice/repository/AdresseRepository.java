package com.mediscreen.patientservice.repository;

import com.mediscreen.patientservice.entity.Adresse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité Adresse
 */
@Repository
public interface AdresseRepository extends JpaRepository<Adresse, Long> {
    
    /**
     * Recherche les adresses par ville
     */
    List<Adresse> findByVilleIgnoreCase(String ville);
    
    /**
     * Recherche les adresses par code postal
     */
    List<Adresse> findByCodePostal(String codePostal);
    
    /**
     * Recherche les adresses par pays
     */
    List<Adresse> findByPaysIgnoreCase(String pays);
    
    /**
     * Recherche une adresse complète existante pour éviter les doublons
     */
    @Query("SELECT a FROM Adresse a WHERE " +
           "LOWER(COALESCE(a.rue, '')) = LOWER(COALESCE(:rue, '')) AND " +
           "LOWER(COALESCE(a.ville, '')) = LOWER(COALESCE(:ville, '')) AND " +
           "LOWER(COALESCE(a.codePostal, '')) = LOWER(COALESCE(:codePostal, '')) AND " +
           "LOWER(COALESCE(a.pays, '')) = LOWER(COALESCE(:pays, ''))")
    Optional<Adresse> findByAdresseComplete(
        @Param("rue") String rue,
        @Param("ville") String ville,
        @Param("codePostal") String codePostal,
        @Param("pays") String pays
    );
    
    /**
     * Recherche les adresses par ville et code postal
     */
    @Query("SELECT a FROM Adresse a WHERE " +
           "LOWER(a.ville) = LOWER(:ville) AND a.codePostal = :codePostal")
    List<Adresse> findByVilleAndCodePostal(
        @Param("ville") String ville, 
        @Param("codePostal") String codePostal
    );
}
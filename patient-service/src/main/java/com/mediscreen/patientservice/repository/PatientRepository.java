package com.mediscreen.patientservice.repository;

import com.mediscreen.patientservice.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité Patient
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    
    /**
     * Recherche par nom et prénom (ignore la casse)
     */
    List<Patient> findByNomIgnoreCaseAndPrenomIgnoreCase(String nom, String prenom);
    
    /**
     * Recherche par nom (ignore la casse)
     */
    List<Patient> findByNomIgnoreCaseContaining(String nom);
    
    /**
     * Recherche par prénom (ignore la casse)
     */
    List<Patient> findByPrenomIgnoreCaseContaining(String prenom);
    
    /**
     * Recherche par genre
     */
    List<Patient> findByGenre(String genre);
    
    /**
     * Recherche par date de naissance
     */
    List<Patient> findByDateNaissance(LocalDate dateNaissance);
    
    /**
     * Recherche par période de naissance
     */
    List<Patient> findByDateNaissanceBetween(LocalDate dateDebut, LocalDate dateFin);
    
    /**
     * Recherche par numéro de téléphone
     */
    Optional<Patient> findByTelephone(String telephone);
    
    /**
     * Recherche par ville (via l'adresse)
     */
    @Query("SELECT p FROM Patient p JOIN p.adresse a WHERE LOWER(a.ville) = LOWER(:ville)")
    List<Patient> findByAdresseVille(@Param("ville") String ville);
    
    /**
     * Recherche par code postal (via l'adresse)
     */
    @Query("SELECT p FROM Patient p JOIN p.adresse a WHERE a.codePostal = :codePostal")
    List<Patient> findByAdresseCodePostal(@Param("codePostal") String codePostal);
    
    /**
     * Recherche avancée par nom complet (nom + prénom)
     */
    @Query("SELECT p FROM Patient p WHERE " +
           "LOWER(CONCAT(p.prenom, ' ', p.nom)) LIKE LOWER(CONCAT('%', :nomComplet, '%')) OR " +
           "LOWER(CONCAT(p.nom, ' ', p.prenom)) LIKE LOWER(CONCAT('%', :nomComplet, '%'))")
    List<Patient> findByNomCompletContaining(@Param("nomComplet") String nomComplet);
    
    /**
     * Vérification d'existence d'un patient avec nom, prénom et date de naissance
     * (pour éviter les doublons)
     */
    @Query("SELECT COUNT(p) > 0 FROM Patient p WHERE " +
           "LOWER(p.nom) = LOWER(:nom) AND " +
           "LOWER(p.prenom) = LOWER(:prenom) AND " +
           "p.dateNaissance = :dateNaissance")
    boolean existsByNomAndPrenomAndDateNaissance(
        @Param("nom") String nom,
        @Param("prenom") String prenom,
        @Param("dateNaissance") LocalDate dateNaissance
    );
    
    /**
     * Recherche des patients par âge (calculé)
     */
    @Query("SELECT p FROM Patient p WHERE " +
           "FUNCTION('TIMESTAMPDIFF', YEAR, p.dateNaissance, CURRENT_DATE) BETWEEN :ageMin AND :ageMax")
    List<Patient> findByAgeBetween(@Param("ageMin") int ageMin, @Param("ageMax") int ageMax);
    
    /**
     * Statistiques par genre
     */
    @Query("SELECT p.genre, COUNT(p) FROM Patient p GROUP BY p.genre")
    List<Object[]> countByGenre();
    
    /**
     * Patients les plus récents (pagination automatique avec Spring Boot 3.5.5)
     */
    List<Patient> findTop10ByOrderByIdDesc();
}
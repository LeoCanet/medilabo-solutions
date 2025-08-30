package com.mediscreen.patientservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.Period;

/**
 * Entité Patient - Conforme à la normalisation 3NF
 */
@Entity
@Table(name = "patients", indexes = {
    @Index(name = "idx_patient_nom_prenom", columnList = "nom, prenom"),
    @Index(name = "idx_patient_genre", columnList = "genre")
})
@Data  // @Getter + @Setter + @ToString + @EqualsAndHashCode + @RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "adresse") // Évite les boucles dans toString
@EqualsAndHashCode(exclude = "adresse")
public class Patient {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "prenom", nullable = false, length = 50)
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 50, message = "Le prénom ne peut pas dépasser 50 caractères")
    private String prenom;
    
    @Column(name = "nom", nullable = false, length = 50)
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
    private String nom;
    
    @Column(name = "date_naissance", nullable = false)
    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateNaissance;
    
    @Column(name = "genre", nullable = false, length = 1)
    @NotNull(message = "Le genre est obligatoire")
    @Pattern(regexp = "[MF]", message = "Le genre doit être M ou F")
    private String genre;
    
    @Column(name = "telephone", length = 15)
    @Size(max = 15, message = "Le téléphone ne peut pas dépasser 15 caractères")
    @Pattern(regexp = "^[0-9\\-+\\s]*$", message = "Format de téléphone invalide")
    private String telephone;
    
    // Relation avec Adresse (3NF - évite la redondance)
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "adresse_id")
    private Adresse adresse;
    
    /**
     * Méthode utilitaire pour calculer l'âge précis
     */
    public int getAge() {
        return dateNaissance != null ? 
            Period.between(dateNaissance, LocalDate.now()).getYears() : 0;
    }
    
    /**
     * Méthode utilitaire pour obtenir le nom complet
     */
    public String getNomComplet() {
        return String.format("%s %s", prenom, nom);
    }
}
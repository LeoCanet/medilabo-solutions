package com.mediscreen.patientservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Entité Adresse - Normalisation 3NF
 */
@Entity
@Table(name = "adresses")
@Data  // @Getter + @Setter + @ToString + @EqualsAndHashCode + @RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Adresse {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "rue", length = 100)
    @Size(max = 100, message = "L'adresse ne peut pas dépasser 100 caractères")
    private String rue;
    
    @Column(name = "ville", length = 50)
    @Size(max = 50, message = "La ville ne peut pas dépasser 50 caractères")
    private String ville;
    
    @Column(name = "code_postal", length = 10)
    @Size(max = 10, message = "Le code postal ne peut pas dépasser 10 caractères")
    private String codePostal;
    
    @Column(name = "pays", length = 50)
    @Size(max = 50, message = "Le pays ne peut pas dépasser 50 caractères")
    private String pays;
}
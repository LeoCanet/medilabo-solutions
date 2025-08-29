package com.mediscreen.patientservice.mapper;

import com.mediscreen.patientservice.dto.*;
import com.mediscreen.patientservice.entity.Adresse;
import com.mediscreen.patientservice.entity.Patient;
import org.mapstruct.*;
import java.util.List;

/**
 * Mapper automatique avec MapStruct 1.6.0
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    builder = @Builder(disableBuilder = true)  // Utilise les Records au lieu de builders
)
public interface PatientMapper {
    
    // === PATIENT MAPPINGS ===
    
    /**
     * Conversion Patient -> PatientDto avec calcul automatique de l'âge
     */
    @Mapping(target = "age", expression = "java(patient.getAge())")
    PatientDto toDto(Patient patient);
    
    /**
     * Conversion PatientCreateDto -> Patient
     */
    @Mapping(target = "id", ignore = true)
    Patient toEntity(PatientCreateDto createDto);
    
    /**
     * Conversion PatientDto -> Patient (pour updates)
     */
    Patient toEntity(PatientDto patientDto);
    
    /**
     * Conversion Patient -> PatientSummaryDto (optimisé pour listes)
     */
    @Mapping(target = "age", expression = "java(patient.getAge())")
    PatientSummaryDto toSummaryDto(Patient patient);
    
    /**
     * Conversion List<Patient> -> List<PatientDto> (Virtual Threads compatible)
     */
    List<PatientDto> toDtoList(List<Patient> patients);
    
    /**
     * Conversion List<Patient> -> List<PatientSummaryDto> (Performance optimisée)
     */
    List<PatientSummaryDto> toSummaryDtoList(List<Patient> patients);
    
    // === ADRESSE MAPPINGS ===
    
    /**
     * Conversion Adresse -> AdresseDto
     */
    AdresseDto toDto(Adresse adresse);
    
    /**
     * Conversion AdresseDto -> Adresse
     */
    @Mapping(target = "id", ignore = true)
    Adresse toEntity(AdresseDto adresseDto);
    
    /**
     * Conversion List<Adresse> -> List<AdresseDto>
     */
    List<AdresseDto> toAdresseDtoList(List<Adresse> adresses);
    
    // === UPDATE MAPPINGS (MapStruct 1.6.0) ===
    
    /**
     * Met à jour une entité Patient existante avec les données d'un DTO
     * Ignore les valeurs null du DTO (mise à jour partielle)
     */
    @Mapping(target = "id", ignore = true)
    @BeanMapping(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    void updatePatientFromDto(PatientDto dto, @MappingTarget Patient entity);
    
    /**
     * Met à jour une entité Adresse existante
     */
    @Mapping(target = "id", ignore = true)
    @BeanMapping(
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
    )
    void updateAdresseFromDto(AdresseDto dto, @MappingTarget Adresse entity);
    
    // === MÉTHODES UTILITAIRES ===
    
    /**
     * Méthode par défaut pour gérer les adresses nulles
     */
    @AfterMapping
    default void linkAdresse(@MappingTarget Patient patient) {
        // Si l'adresse existe, on peut ajouter des validations spécifiques
        if (patient.getAdresse() != null) {
            // Logique métier à venir
        }
    }
    
    /**
     * Expression Java pour calculer l'âge de manière thread-safe
     */
    default String getAgeExpression() {
        return "java(patient.getAge())";
    }
}
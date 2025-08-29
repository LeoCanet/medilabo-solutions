package com.mediscreen.patientservice.config;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.InjectionStrategy;

/**
 * Configuration globale MapStruct 1.6.0
 */
@MapperConfig(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    disableSubMappingMethodsGeneration = true  // Performance
)
public interface MapStructConfig {
    
    /**
     * Annotation personnalisée pour les mappers complexes
     */
    @interface PatientMapping {
        boolean ignoreAge() default false;
        boolean includeSummary() default false;
    }
}
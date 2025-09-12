package com.mediscreen.frontend.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediscreen.frontend.client.PatientApiClient;
import com.mediscreen.frontend.dto.ApiErrorResponse;
import com.mediscreen.frontend.dto.PatientCreateDto;
import com.mediscreen.frontend.dto.PatientDto;
import com.mediscreen.frontend.exception.PatientNotFoundException;
import com.mediscreen.frontend.exception.PatientServiceException;
import com.mediscreen.frontend.exception.PatientValidationException;
import com.mediscreen.frontend.repository.PatientRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Implémentation Repository utilisant Feign
 * 
 * Cette classe encapsule complètement l'utilisation de Feign et transforme
 * les FeignExceptions en exceptions Business métier.
 *
 * Séparation des préoccupations (technique vs métier)
 * Facilite le changement de librairie HTTP à l'avenir
 * Controller et Service ne connaissent pas Feign
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class PatientRepositoryImpl implements PatientRepository {

    private final PatientApiClient patientApiClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<PatientDto> findAll() {
        try {
            log.debug("Récupération de tous les patients via Repository");
            return patientApiClient.getAllPatients();
            
        } catch (FeignException e) {
            log.error("Erreur lors de la récupération des patients: status={}", e.status(), e);
            throw new PatientServiceException(
                "Erreur technique lors de la récupération des patients", 
                e.status()
            );
        }
    }

    @Override
    public PatientDto findById(Long id) {
        try {
            log.debug("Récupération du patient ID={} via Repository", id);
            return patientApiClient.getPatientById(id);
            
        } catch (FeignException.NotFound e) {
            log.warn("Patient ID={} non trouvé", id);
            throw PatientNotFoundException.withId(id);
            
        } catch (FeignException e) {
            log.error("Erreur lors de la récupération du patient ID={}: status={}", id, e.status(), e);
            throw new PatientServiceException(
                "Erreur technique lors de la récupération du patient", 
                e.status()
            );
        }
    }

    @Override
    public void save(PatientCreateDto patientCreateDto) {
        try {
            log.debug("Création d'un patient via Repository: {}", patientCreateDto.nom());
            patientApiClient.createPatient(patientCreateDto);
            log.info("Patient créé avec succès: {}", patientCreateDto.nom());
            
        } catch (FeignException.BadRequest | FeignException.UnprocessableEntity e) {
            log.warn("Erreur de validation lors de la création du patient: status={}", e.status());
            throw transformToValidationException(e);
            
        } catch (FeignException e) {
            log.error("Erreur technique lors de la création du patient: status={}", e.status(), e);
            throw new PatientServiceException(
                "Erreur technique lors de la création du patient", 
                e.status()
            );
        }
    }

    @Override
    public void update(Long id, PatientDto patientDto) {
        try {
            log.debug("Modification du patient ID={} via Repository", id);
            patientApiClient.updatePatient(id, patientDto);
            log.info("Patient ID={} modifié avec succès", id);
            
        } catch (FeignException.NotFound e) {
            log.warn("Patient ID={} non trouvé pour modification", id);
            throw PatientNotFoundException.withId(id);
            
        } catch (FeignException.BadRequest | FeignException.UnprocessableEntity e) {
            log.warn("Erreur de validation lors de la modification du patient ID={}: status={}", id, e.status());
            throw transformToValidationException(e);
            
        } catch (FeignException e) {
            log.error("Erreur technique lors de la modification du patient ID={}: status={}", id, e.status(), e);
            throw new PatientServiceException(
                "Erreur technique lors de la modification du patient", 
                e.status()
            );
        }
    }

    /**
     * Transforme une FeignException de validation en PatientValidationException
     * Préserve les détails de validation JSON du backend
     */
    private PatientValidationException transformToValidationException(FeignException e) {
        String errorBody = e.contentUTF8();
        
        // Tente de parser les détails de validation
        if (errorBody != null && !errorBody.isBlank()) {
            try {
                ApiErrorResponse errorResponse = objectMapper.readValue(errorBody, ApiErrorResponse.class);
                return new PatientValidationException(
                    errorResponse.getMessage() != null ? errorResponse.getMessage() : "Erreur de validation", 
                    errorBody
                );
            } catch (JsonProcessingException jsonException) {
                log.warn("Impossible de parser les détails d'erreur JSON", jsonException);
            }
        }
        
        return new PatientValidationException("Erreur de validation patient");
    }
}
package com.mediscreen.frontend.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediscreen.frontend.client.NotesApiClient;
import com.mediscreen.frontend.dto.ApiErrorResponse;
import com.mediscreen.frontend.dto.NoteCreateDto;
import com.mediscreen.frontend.dto.NoteDto;
import com.mediscreen.frontend.exception.NoteNotFoundException;
import com.mediscreen.frontend.exception.NoteServiceException;
import com.mediscreen.frontend.exception.NoteValidationException;
import com.mediscreen.frontend.repository.NotesRepository;
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
 * Architecture cohérente avec PatientRepositoryImpl.
 *
 * Séparation des préoccupations (technique vs métier)
 * Facilite le changement de librairie HTTP à l'avenir
 * Controller et Service ne connaissent pas Feign
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class NotesRepositoryImpl implements NotesRepository {

    private final NotesApiClient notesApiClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<NoteDto> findByPatientId(Integer patId) {
        try {
            log.debug("Récupération des notes pour patient ID={} via Repository", patId);
            return notesApiClient.getNotesByPatientId(patId);
            
        } catch (FeignException e) {
            log.error("Erreur lors de la récupération des notes pour patient ID={}: status={}", patId, e.status(), e);
            throw new NoteServiceException(
                "Erreur technique lors de la récupération des notes", 
                e.status()
            );
        }
    }

    @Override
    public NoteDto findById(String id) {
        try {
            log.debug("Récupération de la note ID={} via Repository", id);
            return notesApiClient.getNoteById(id);
            
        } catch (FeignException.NotFound e) {
            log.warn("Note ID={} non trouvée", id);
            throw NoteNotFoundException.withId(id);
            
        } catch (FeignException e) {
            log.error("Erreur lors de la récupération de la note ID={}: status={}", id, e.status(), e);
            throw new NoteServiceException(
                "Erreur technique lors de la récupération de la note", 
                e.status()
            );
        }
    }

    @Override
    public void save(NoteCreateDto noteCreateDto) {
        try {
            log.debug("Création d'une note pour patient ID={} via Repository", noteCreateDto.patId());
            notesApiClient.createNote(noteCreateDto);
            log.info("Note créée avec succès pour patient ID={}", noteCreateDto.patId());
            
        } catch (FeignException.BadRequest | FeignException.UnprocessableEntity e) {
            log.warn("Erreur de validation lors de la création de la note: {}", e.getMessage());
            String validationDetails = extractValidationDetails(e);
            throw new NoteValidationException("Erreur de validation de la note", validationDetails);
            
        } catch (FeignException e) {
            log.error("Erreur lors de la création de la note: status={}", e.status(), e);
            throw new NoteServiceException(
                "Erreur technique lors de la création de la note", 
                e.status()
            );
        }
    }

    @Override
    public void update(String id, NoteDto noteDto) {
        try {
            log.debug("Mise à jour de la note ID={} via Repository", id);
            notesApiClient.updateNote(id, noteDto);
            log.info("Note ID={} mise à jour avec succès", id);
            
        } catch (FeignException.NotFound e) {
            log.warn("Note ID={} non trouvée lors de la mise à jour", id);
            throw NoteNotFoundException.withId(id);
            
        } catch (FeignException.BadRequest | FeignException.UnprocessableEntity e) {
            log.warn("Erreur de validation lors de la mise à jour de la note ID={}: {}", id, e.getMessage());
            String validationDetails = extractValidationDetails(e);
            throw new NoteValidationException("Erreur de validation de la note", validationDetails);
            
        } catch (FeignException e) {
            log.error("Erreur lors de la mise à jour de la note ID={}: status={}", id, e.status(), e);
            throw new NoteServiceException(
                "Erreur technique lors de la mise à jour de la note", 
                e.status()
            );
        }
    }

    @Override
    public void delete(String id) {
        try {
            log.debug("Suppression de la note ID={} via Repository", id);
            notesApiClient.deleteNote(id);
            log.info("Note ID={} supprimée avec succès", id);
            
        } catch (FeignException.NotFound e) {
            log.warn("Note ID={} non trouvée lors de la suppression", id);
            throw NoteNotFoundException.withId(id);
            
        } catch (FeignException e) {
            log.error("Erreur lors de la suppression de la note ID={}: status={}", id, e.status(), e);
            throw new NoteServiceException(
                "Erreur technique lors de la suppression de la note", 
                e.status()
            );
        }
    }

    /**
     * Extrait les détails de validation des erreurs Feign
     */
    private String extractValidationDetails(FeignException e) {
        try {
            String responseBody = e.contentUTF8();
            if (responseBody != null && !responseBody.isEmpty()) {
                ApiErrorResponse errorResponse = objectMapper.readValue(responseBody, ApiErrorResponse.class);
                return objectMapper.writeValueAsString(errorResponse.getErrors());
            }
        } catch (JsonProcessingException ex) {
            log.warn("Impossible de parser les détails de validation", ex);
        }
        return null;
    }
}
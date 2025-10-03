package com.mediscreen.assessmentservice.client;

import com.mediscreen.assessmentservice.dto.NoteDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Client Feign pour communiquer avec Notes Service via Gateway
 *
 * Communication sécurisée via Gateway avec Basic Auth automatique.
 * Utilise les mêmes endpoints que le frontend pour cohérence.
 */
@FeignClient(name = "notes-api", url = "${mediscreen.clients.notes.url}")
public interface NotesApiClient {

    /**
     * Récupère toutes les notes d'un patient par son ID
     * Endpoint utilisé par l'algorithme d'évaluation pour analyser les termes déclencheurs
     *
     * @param patId ID du patient
     * @return Liste des notes du patient
     */
    @GetMapping("/api/v1/notes/patient/{patId}")
    List<NoteDto> getNotesByPatientId(@PathVariable("patId") Integer patId);
}
package com.mediscreen.notesservice.repository;

import com.mediscreen.notesservice.entity.Note;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'entité Note
 */
@Repository
public interface NoteRepository extends MongoRepository<Note, String> {

    /**
     * Trouve toutes les notes d'un patient par son ID
     * @param patId ID du patient
     * @return Liste des notes du patient
     */
    List<Note> findByPatIdOrderByCreatedDateDesc(Integer patId);

    /**
     * Trouve toutes les notes d'un patient par son nom
     * @param patient Nom du patient
     * @return Liste des notes du patient
     */
    List<Note> findByPatientOrderByCreatedDateDesc(String patient);
}
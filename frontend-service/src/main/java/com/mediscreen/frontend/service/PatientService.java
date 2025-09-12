package com.mediscreen.frontend.service;

import com.mediscreen.frontend.dto.AdresseDto;
import com.mediscreen.frontend.dto.PatientDto;
import com.mediscreen.frontend.dto.PatientFormDto;
import com.mediscreen.frontend.dto.PatientCreateDto;
import com.mediscreen.frontend.exception.PatientNotFoundException;
import com.mediscreen.frontend.exception.PatientServiceException;
import com.mediscreen.frontend.exception.PatientValidationException;
import com.mediscreen.frontend.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service métier pour la gestion des patients
 *
 * Utilise PatientRepository au lieu de PatientApiClient directement
 * Découplé des librairies externes (Feign)
 * Focus sur la logique métier, pas sur la technologie de communication
 * 
 * Les exceptions Business (PatientNotFoundException, PatientValidationException)
 * remontent naturellement depuis la couche Repository.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {

    private final PatientRepository patientRepository;

    /**
     * Récupère tous les patients
     * 
     * @return Liste des patients
     * @throws PatientServiceException en cas d'erreur technique
     */
    public List<PatientDto> getAllPatients() {
        log.debug("Récupération de tous les patients via Service");
        return patientRepository.findAll();
    }

    /**
     * Récupère un patient par son ID
     * 
     * @param id ID du patient
     * @return Patient trouvé
     * @throws PatientNotFoundException si le patient n'existe pas
     */
    public PatientDto getPatientById(Long id) {
        log.debug("Récupération du patient ID={} via Service", id);
        return patientRepository.findById(id);
    }

    /**
     * Sauvegarde un patient (création ou modification)
     * 
     * @param formDto Données du formulaire patient
     * @throws PatientNotFoundException si patient à modifier n'existe pas
     * @throws PatientValidationException en cas d'erreur de validation
     * @throws PatientServiceException en cas d'erreur technique
     */
    public void savePatient(PatientFormDto formDto) {
        log.debug("Sauvegarde patient via Service: {} {}", formDto.prenom(), formDto.nom());
        
        // Crée l'objet Adresse à partir des champs plats du formulaire
        AdresseDto adresse = new AdresseDto(null, formDto.rue(), formDto.ville(), formDto.codePostal(), null);

        if (formDto.id() != null) {
            // C'est une mise à jour : on envoie un PatientDto complet
            PatientDto patientToUpdate = new PatientDto(
                    formDto.id(),
                    formDto.prenom(),
                    formDto.nom(),
                    formDto.dateNaissance(),
                    formDto.genre(),
                    formDto.telephone(),
                    adresse
            );
            patientRepository.update(formDto.id(), patientToUpdate);
        } else {
            // C'est une création : on envoie un PatientCreateDto
            PatientCreateDto patientToCreate = new PatientCreateDto(
                    formDto.prenom(),
                    formDto.nom(),
                    formDto.dateNaissance(),
                    formDto.genre(),
                    formDto.telephone(),
                    adresse
            );
            patientRepository.save(patientToCreate);
        }
    }

    /**
     * Convertit un Patient en PatientFormDto pour l'affichage dans le formulaire
     * 
     * @param id ID du patient à convertir
     * @return FormDto prêt pour le formulaire
     * @throws PatientNotFoundException si le patient n'existe pas
     */
    public PatientFormDto getPatientFormById(Long id) {
        log.debug("Conversion patient ID={} vers FormDto", id);
        PatientDto patientDto = getPatientById(id);
        return new PatientFormDto(
                patientDto.id(),
                patientDto.nom(),
                patientDto.prenom(),
                patientDto.dateNaissance(),
                patientDto.genre(),
                patientDto.telephone(),
                patientDto.adresse() != null ? patientDto.adresse().rue() : "",
                patientDto.adresse() != null ? patientDto.adresse().ville() : "",
                patientDto.adresse() != null ? patientDto.adresse().codePostal() : ""
        );
    }
}
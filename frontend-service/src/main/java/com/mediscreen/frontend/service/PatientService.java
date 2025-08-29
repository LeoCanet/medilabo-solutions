package com.mediscreen.frontend.service;

import com.mediscreen.frontend.client.PatientApiClient;
import com.mediscreen.frontend.dto.AdresseDto;
import com.mediscreen.frontend.dto.PatientDto;
import com.mediscreen.frontend.dto.PatientFormDto;
import com.mediscreen.frontend.dto.PatientCreateDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {

    private final PatientApiClient patientApiClient;

    public PatientService(PatientApiClient patientApiClient) {
        this.patientApiClient = patientApiClient;
    }

    public List<PatientDto> getAllPatients() {
        return patientApiClient.getAllPatients();
    }

    public List<PatientDto> searchPatients(String nom) {
        if (nom == null || nom.trim().isEmpty()) {
            return getAllPatients();
        }
        return patientApiClient.searchPatients(nom);
    }

    public PatientDto getPatientById(Long id) {
        return patientApiClient.getPatientById(id);
    }

    public void savePatient(PatientFormDto formDto) {
        // Crée l'objet Adresse à partir des champs plats du formulaire
        AdresseDto adresse = new AdresseDto(formDto.rue(), formDto.ville(), formDto.codePostal());

        if (formDto.id() != null) {
            // C'est une mise à jour : on envoie un PatientDto complet
            PatientDto patientToUpdate = new PatientDto(
                    formDto.id(),
                    formDto.nom(),
                    formDto.prenom(),
                    formDto.dateDeNaissance(),
                    formDto.genre(),
                    formDto.telephone(),
                    adresse
            );
            patientApiClient.updatePatient(formDto.id(), patientToUpdate);
        } else {
            // C'est une création : on envoie un PatientCreateDto
            PatientCreateDto patientToCreate = new PatientCreateDto(
                    formDto.prenom(),
                    formDto.nom(),
                    formDto.dateDeNaissance(),
                    formDto.genre(),
                    formDto.telephone(),
                    adresse
            );
            patientApiClient.createPatient(patientToCreate);
        }
    }

    public void deletePatient(Long id) {
        patientApiClient.deletePatient(id);
    }

    public PatientFormDto getPatientFormById(Long id) {
        PatientDto patientDto = getPatientById(id);
        return new PatientFormDto(
                patientDto.id(),
                patientDto.nom(),
                patientDto.prenom(),
                patientDto.dateDeNaissance(),
                patientDto.genre(),
                patientDto.telephone(),
                patientDto.adresse() != null ? patientDto.adresse().rue() : "",
                patientDto.adresse() != null ? patientDto.adresse().ville() : "",
                patientDto.adresse() != null ? patientDto.adresse().codePostal() : ""
        );
    }
}

package com.mediscreen.patientservice.repository;

import com.mediscreen.patientservice.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository pour l'entité Patient
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

}

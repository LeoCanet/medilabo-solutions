package com.mediscreen.frontend.client;

import com.mediscreen.frontend.dto.PatientCreateDto;
import com.mediscreen.frontend.dto.PatientDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "patient-api", url = "${feign.client.url}")
public interface PatientApiClient {

    @GetMapping("/api/v1/patients")
    List<PatientDto> getAllPatients();

    @GetMapping("/api/v1/patients/{id}")
    PatientDto getPatientById(@PathVariable("id") Long id);

    @PostMapping("/api/v1/patients")
    void createPatient(@RequestBody PatientCreateDto patient);

    @PutMapping("/api/v1/patients/{id}")
    void updatePatient(@PathVariable("id") Long id, @RequestBody PatientDto patient);

    
}
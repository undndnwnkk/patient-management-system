package com.pm.patientservice.controller;

import com.pm.patientservice.dto.PatientRequestDto;
import com.pm.patientservice.dto.PatientResponseDto;
import com.pm.patientservice.dto.validators.CreatePatientValidationGroup;
import com.pm.patientservice.service.PatientService;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {
    private final PatientService patientService;

    @GetMapping
    public ResponseEntity<List<PatientResponseDto>> getPatients(){
        return ResponseEntity.ok(patientService.getPatients());
    }

    @PostMapping
    public ResponseEntity<PatientResponseDto> createPatient(
            @Validated({Default.class, CreatePatientValidationGroup.class})
            @RequestBody PatientRequestDto patientRequestDto
    ){
        return ResponseEntity.ok(patientService.createPatient(patientRequestDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponseDto> updatePatient(
            @PathVariable UUID id,
            @Validated({Default.class}) @RequestBody PatientRequestDto patientRequestDto
    ){
        PatientResponseDto patientResponseDto = patientService.updatePatient(id, patientRequestDto);
        return ResponseEntity.ok().body(patientResponseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id){
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}

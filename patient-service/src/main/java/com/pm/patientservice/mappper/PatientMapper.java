package com.pm.patientservice.mappper;

import com.pm.patientservice.dto.PatientRequestDto;
import com.pm.patientservice.dto.PatientResponseDto;
import com.pm.patientservice.model.Patient;

import java.time.LocalDate;

public class PatientMapper {
    public static PatientResponseDto toDto(Patient patient){
        return PatientResponseDto.builder()
                .id(patient.getId().toString())
                .name(patient.getName())
                .email(patient.getEmail())
                .address(patient.getAddress())
                .dateOfBirth(patient.getDateOfBirth().toString())
                .build();
    }

    public static Patient toModel (PatientRequestDto patientRequestDto){
        Patient newPatient = new Patient();
        newPatient.setName(patientRequestDto.getName());
        newPatient.setEmail(patientRequestDto.getEmail());
        newPatient.setAddress(patientRequestDto.getAddress());
        newPatient.setDateOfBirth(LocalDate.parse(patientRequestDto.getDateOfBirth()));
        newPatient.setRegisteredDate(LocalDate.parse(patientRequestDto.getRegisteredDate()));

        return newPatient;
    }
}

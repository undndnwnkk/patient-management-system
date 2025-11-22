package com.pm.patientservice.service;

import com.pm.patientservice.dto.PatientRequestDto;
import com.pm.patientservice.dto.PatientResponseDto;
import com.pm.patientservice.exception.EmailAlreadyExistsException;
import com.pm.patientservice.exception.PatientNotFoundException;
import com.pm.patientservice.grpc.BillingServiceGrpcClient;
import com.pm.patientservice.kafka.KafkaProducer;
import com.pm.patientservice.mappper.PatientMapper;
import com.pm.patientservice.model.Patient;
import com.pm.patientservice.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient  billingServiceGrpcClient;
    private final KafkaProducer kafkaProducer;

    public List<PatientResponseDto> getPatients(){
        List<Patient> patients = patientRepository.findAll();
        return patients.stream().map(PatientMapper::toDto).toList();
    }

    @Transactional
    public PatientResponseDto createPatient(PatientRequestDto patientRequestDto){
        if (patientRepository.existsByEmail(patientRequestDto.getEmail())) {
            throw new EmailAlreadyExistsException("A patient with this email " +
                    "already exists: " + patientRequestDto.getEmail());
        }

        Patient newPatient = patientRepository.save(PatientMapper.toModel(patientRequestDto));

        try {
            billingServiceGrpcClient.createBillingAccount(
                    newPatient.getId().toString(),
                    newPatient.getName(),
                    newPatient.getEmail()
            );
        } catch (Exception e) {
            log.error("Failed to create billing account for patient {}: {}", newPatient.getId(), e.getMessage());
            throw new RuntimeException("Failed to create billing account", e);
        }

        try {
            kafkaProducer.sendEvent(newPatient);
        } catch (Exception e) {
            log.error("Failed to send Kafka event for patient {}: {}", newPatient.getId(), e.getMessage());
            throw new RuntimeException("Failed to send patient event", e);
        }

        return PatientMapper.toDto(newPatient);
    }

    @Transactional
    public PatientResponseDto updatePatient(UUID id, PatientRequestDto patientRequestDto){
        Patient patient = patientRepository.findById(id).orElseThrow(
                () -> new PatientNotFoundException("Patient not found with id " + id));

        if (patientRepository.existsByEmailAndIdNot(patientRequestDto.getEmail(), id)) {
            throw new EmailAlreadyExistsException("A patient with this email " +
                    "already exists: " + patientRequestDto.getEmail());
        }

        patient.setName(patientRequestDto.getName());
        patient.setAddress(patientRequestDto.getAddress());
        patient.setEmail(patientRequestDto.getEmail());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDto.getDateOfBirth()));
        Patient updatedPatient = patientRepository.save(patient);

        try {
            kafkaProducer.sendEvent(updatedPatient);
        } catch (Exception e) {
            log.warn("Failed to send Kafka event for updated patient {}: {}", updatedPatient.getId(), e.getMessage());
        }

        return PatientMapper.toDto(updatedPatient);
    }

    public void deletePatient(UUID id){
        patientRepository.deleteById(id);
    }
}

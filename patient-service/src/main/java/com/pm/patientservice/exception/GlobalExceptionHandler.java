package com.pm.patientservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions (MethodArgumentNotValidException ex){
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(
                error -> errors.put(error.getObjectName(), error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyExistsException (EmailAlreadyExistsException ex){
        log.warn("Email already exists: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        errors.put("message", "email address already exists");
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePatientNotFoundException (PatientNotFoundException ex){
        log.warn("Patient not found: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        errors.put("message", "Patient not found");
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException (RuntimeException ex){
        log.error("Service error: {}", ex.getMessage(), ex);
        Map<String, String> errors = new HashMap<>();

        if (ex.getMessage().contains("billing")) {
            errors.put("message", "Failed to create billing account. Please try again.");
        } else if (ex.getMessage().contains("Kafka") || ex.getMessage().contains("event")) {
            errors.put("message", "Failed to process patient event. Please try again.");
        } else {
            errors.put("message", "Internal server error. Please try again.");
        }

        return ResponseEntity.status(500).body(errors);
    }
}

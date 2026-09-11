package com.fraud_detector.project.dto.response;

import java.time.Instant;
import java.time.LocalDate;

public record UserResponseDTO(
        String userId,
        String fullName,
        String email,
        String document,
        String phoneNumber,
        LocalDate dateOfBirth,
        String country,
        Instant createdAt,
        Instant updatedAt
) {}
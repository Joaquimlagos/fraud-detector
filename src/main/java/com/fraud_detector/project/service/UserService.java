package com.fraud_detector.project.service;

import com.fraud_detector.project.dto.request.UserRequestDTO;
import com.fraud_detector.project.dto.response.UserResponseDTO;

public interface UserService {
    UserResponseDTO create(UserRequestDTO dto);
    UserResponseDTO findById(String userId);
    void delete(String userId);
}
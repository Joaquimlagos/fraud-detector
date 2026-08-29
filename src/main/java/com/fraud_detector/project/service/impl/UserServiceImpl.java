package com.fraud_detector.project.service.impl;

import com.fraud_detector.project.dto.request.UserRequestDTO;
import com.fraud_detector.project.dto.response.UserResponseDTO;
import com.fraud_detector.project.exception.ResourceNotFoundException;
import com.fraud_detector.project.mapper.UserMapper;
import com.fraud_detector.project.model.User;
import com.fraud_detector.project.repository.UserRepository;
import com.fraud_detector.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponseDTO create(UserRequestDTO dto) {
        User user = userMapper.toModel(dto);
        user.setUserId(UUID.randomUUID().toString());
        user.setCreatedAt(Instant.now());

        User saved = userRepository.save(user);
        return userMapper.toResponseDTO(saved);
    }

    @Override
    public UserResponseDTO findById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return userMapper.toResponseDTO(user);
    }

    @Override
    public void delete(String userId) {
        findById(userId); // Throws ResourceNotFoundException if user doesn't exist
        userRepository.deleteById(userId);
    }
}
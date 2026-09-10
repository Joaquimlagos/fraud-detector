package com.fraud_detector.project.service.impl;

import com.fraud_detector.project.dto.request.UserRequestDTO;
import com.fraud_detector.project.dto.response.TransactionItemDTO;
import com.fraud_detector.project.dto.response.UserHistoryResponseDTO;
import com.fraud_detector.project.dto.response.UserResponseDTO;
import com.fraud_detector.project.exception.ResourceNotFoundException;
import com.fraud_detector.project.mapper.TransactionMapper;
import com.fraud_detector.project.mapper.UserMapper;
import com.fraud_detector.project.model.Transaction;
import com.fraud_detector.project.model.User;
import com.fraud_detector.project.repository.TransactionRepository;
import com.fraud_detector.project.repository.UserRepository;
import com.fraud_detector.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

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
        User user = findUserOrThrow(userId);
        return userMapper.toResponseDTO(user);
    }

    @Override
    public UserHistoryResponseDTO findHistory(String userId) {
        User user = findUserOrThrow(userId);

        List<TransactionItemDTO> transactions = transactionRepository.findByUserId(userId)
                .stream()
                .map(transactionMapper::toItemDTO)
                .toList();

        return new UserHistoryResponseDTO(user.getUserId(), user.getFullName(), transactions);
    }

    @Override
    public void delete(String userId) {
        findUserOrThrow(userId);
        userRepository.deleteById(userId);
    }

    private User findUserOrThrow(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
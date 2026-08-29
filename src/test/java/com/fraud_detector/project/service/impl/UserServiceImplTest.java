package com.fraud_detector.project.service.impl;

import com.fraud_detector.project.dto.request.UserRequestDTO;
import com.fraud_detector.project.dto.response.UserResponseDTO;
import com.fraud_detector.project.exception.ResourceNotFoundException;
import com.fraud_detector.project.mapper.UserMapper;
import com.fraud_detector.project.model.User;
import com.fraud_detector.project.repository.UserRepository;
import com.fraud_detector.project.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldCreateUser() {
        UserRequestDTO request = new UserRequestDTO(
                "John Doe",
                "john@example.com",
                "12345678901",
                "+5511999999999",
                LocalDate.of(1990, 1, 1),
                "BR"
        );

        User user = new User();
        user.setUserId(UUID.randomUUID().toString());
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setDocument(request.document());
        user.setPhoneNumber(request.phoneNumber());
        user.setDateOfBirth(request.dateOfBirth());
        user.setCountry(request.country());
        user.setCreatedAt(Instant.now());

        UserResponseDTO response = new UserResponseDTO(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getDocument(),
                user.getPhoneNumber(),
                user.getDateOfBirth(),
                user.getCountry(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );

        when(userMapper.toModel(request)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponseDTO(user)).thenReturn(response);

        UserResponseDTO result = userService.create(request);

        assertNotNull(result);
        assertNotNull(result.userId());
        assertEquals(request.fullName(), result.fullName());
        assertEquals(request.email(), result.email());
        assertEquals(request.document(), result.document());
        assertEquals(request.phoneNumber(), result.phoneNumber());
        assertEquals(request.dateOfBirth(), result.dateOfBirth());
        assertEquals(request.country(), result.country());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldFindUserById() {
        String userId = UUID.randomUUID().toString();
        User user = new User();
        user.setUserId(userId);
        user.setFullName("John Doe");
        user.setEmail("john@example.com");
        user.setDocument("12345678901");
        user.setPhoneNumber("+5511999999999");
        user.setDateOfBirth(LocalDate.of(1990, 1, 1));
        user.setCountry("BR");
        user.setCreatedAt(Instant.now());

        UserResponseDTO response = new UserResponseDTO(
                userId, "John Doe", "john@example.com", "12345678901",
                "+5511999999999", user.getDateOfBirth(), "BR", user.getCreatedAt(), user.getUpdatedAt()
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toResponseDTO(user)).thenReturn(response);

        UserResponseDTO result = userService.findById(userId);

        assertEquals(userId, result.userId());
        assertEquals("John Doe", result.fullName());
        assertEquals("BR", result.country());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        String userId = UUID.randomUUID().toString();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.findById(userId));
    }

    @Test
    void shouldDeleteUser() {
        String userId = UUID.randomUUID().toString();
        User user = new User();
        user.setUserId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.delete(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        String userId = UUID.randomUUID().toString();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.delete(userId));
    }
}
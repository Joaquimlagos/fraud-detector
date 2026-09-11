package com.fraud_detector.project.controller;

import com.fraud_detector.project.dto.request.UserRequestDTO;
import com.fraud_detector.project.dto.response.UserHistoryResponseDTO;
import com.fraud_detector.project.dto.response.UserResponseDTO;
import com.fraud_detector.project.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management operations")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Creates a new user")
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserRequestDTO dto) {
        UserResponseDTO created = userService.create(dto);
        return ResponseEntity.created(URI.create("/api/v1/users/" + created.userId())).body(created);
    }

    @GetMapping("/history/{userId}")
    @Operation(summary = "Returns the user profile and all their transactions")
    public ResponseEntity<UserHistoryResponseDTO> findHistory(@PathVariable String userId) {
        return ResponseEntity.ok(userService.findHistory(userId));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Deletes a user")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String userId) {
        userService.delete(userId);
    }
}
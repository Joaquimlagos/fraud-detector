package com.fraud_detector.project.mapper;

import com.fraud_detector.project.dto.request.UserRequestDTO;
import com.fraud_detector.project.dto.response.UserResponseDTO;
import com.fraud_detector.project.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toModel(UserRequestDTO dto);

    UserResponseDTO toResponseDTO(User model);
}
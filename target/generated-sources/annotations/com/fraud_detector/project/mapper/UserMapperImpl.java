package com.fraud_detector.project.mapper;

import com.fraud_detector.project.dto.request.UserRequestDTO;
import com.fraud_detector.project.dto.response.UserResponseDTO;
import com.fraud_detector.project.model.User;
import java.time.Instant;
import java.time.LocalDate;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-29T15:08:52-0300",
    comments = "version: 1.6.0, compiler: javac, environment: Java 21.0.12 (Amazon.com Inc.)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toModel(UserRequestDTO dto) {
        if ( dto == null ) {
            return null;
        }

        User user = new User();

        user.setFullName( dto.fullName() );
        user.setEmail( dto.email() );
        user.setDocument( dto.document() );
        user.setPhoneNumber( dto.phoneNumber() );
        user.setDateOfBirth( dto.dateOfBirth() );
        user.setCountry( dto.country() );

        return user;
    }

    @Override
    public UserResponseDTO toResponseDTO(User model) {
        if ( model == null ) {
            return null;
        }

        String userId = null;
        String fullName = null;
        String email = null;
        String document = null;
        String phoneNumber = null;
        LocalDate dateOfBirth = null;
        String country = null;
        Instant createdAt = null;
        Instant updatedAt = null;

        userId = model.getUserId();
        fullName = model.getFullName();
        email = model.getEmail();
        document = model.getDocument();
        phoneNumber = model.getPhoneNumber();
        dateOfBirth = model.getDateOfBirth();
        country = model.getCountry();
        createdAt = model.getCreatedAt();
        updatedAt = model.getUpdatedAt();

        UserResponseDTO userResponseDTO = new UserResponseDTO( userId, fullName, email, document, phoneNumber, dateOfBirth, country, createdAt, updatedAt );

        return userResponseDTO;
    }
}

package com.fraud_detector.project.repository;

import com.fraud_detector.project.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private DynamoDbTable<User> table;

    @Test
    void shouldSaveUser() {
        UserRepository repository = new UserRepository(table);
        User user = new User();
        user.setUserId(UUID.randomUUID().toString());

        User saved = repository.save(user);

        assertEquals(user, saved);
        verify(table).putItem(user);
    }

    @Test
    void shouldFindUserByIdWhenUserExists() {
        UserRepository repository = new UserRepository(table);
        String userId = UUID.randomUUID().toString();
        User user = new User();
        user.setUserId(userId);

        when(table.getItem(any(Key.class))).thenReturn(user);

        Optional<User> result = repository.findById(userId);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getUserId());
    }

    @Test
    void shouldReturnEmptyWhenUserDoesNotExist() {
        UserRepository repository = new UserRepository(table);

        when(table.getItem(any(Key.class))).thenReturn(null);

        Optional<User> result = repository.findById(UUID.randomUUID().toString());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldDeleteUser() {
        UserRepository repository = new UserRepository(table);
        String userId = UUID.randomUUID().toString();

        repository.deleteById(userId);

        verify(table).deleteItem(any(Key.class));
    }
}
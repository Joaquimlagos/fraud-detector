package com.fraud_detector.project.repository;

import com.fraud_detector.project.model.User;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.Optional;

@Repository
public class UserRepository {

    private final DynamoDbTable<User> table;

    public UserRepository(DynamoDbTable<User> userTable) {
        this.table = userTable;
    }

    public User save(User user) {
        table.putItem(user);
        return user;
    }

    public Optional<User> findById(String userId) {
        User user = table.getItem(Key.builder().partitionValue(userId).build());
        return Optional.ofNullable(user);
    }

    public void deleteById(String userId) {
        table.deleteItem(Key.builder().partitionValue(userId).build());
    }
}
package com.fraud_detector.project.config;

import com.fraud_detector.project.model.Transaction;
import com.fraud_detector.project.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Configuration
public class DynamoDbConfig {

    @Value("${aws.dynamodb.users-table:users}")
    private String usersTableName;

    @Value("${aws.dynamodb.transactions-table:transactions}")
    private String transactionsTableName;

    @Bean
    public DynamoDbTable<User> userTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table(usersTableName, TableSchema.fromBean(User.class));
    }

    @Bean
    public DynamoDbTable<Transaction> transactionTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table(transactionsTableName, TableSchema.fromBean(Transaction.class));
    }
}
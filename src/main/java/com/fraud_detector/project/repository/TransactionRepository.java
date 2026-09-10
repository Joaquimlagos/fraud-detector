package com.fraud_detector.project.repository;

import com.fraud_detector.project.model.Transaction;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;

@Repository
public class TransactionRepository {

    private static final String USER_ID_INDEX = "userId-occurredAt-index";

    private final DynamoDbTable<Transaction> table;

    public TransactionRepository(DynamoDbTable<Transaction> transactionTable) {
        this.table = transactionTable;
    }

    public List<Transaction> findByUserId(String userId) {
        QueryConditional condition = QueryConditional.keyEqualTo(Key.builder().partitionValue(userId).build());

        DynamoDbIndex<Transaction> index = table.index(USER_ID_INDEX);
        return index.query(condition)
                .stream()
                .flatMap(page -> page.items().stream())
                .toList();
    }
}
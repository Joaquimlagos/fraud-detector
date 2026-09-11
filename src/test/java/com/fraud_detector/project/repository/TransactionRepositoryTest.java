package com.fraud_detector.project.repository;

import com.fraud_detector.project.model.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionRepositoryTest {

    private static final String USER_ID_INDEX = "userId-occurredAt-index";

    @Mock
    private DynamoDbTable<Transaction> table;

    @Test
    @SuppressWarnings("unchecked")
    void shouldQueryUserIdIndexWhenFindingByUserId() {
        Transaction transaction = new Transaction();
        transaction.setTransactionId("txn-123");
        transaction.setUserId("user-1");

        DynamoDbIndex<Transaction> index = mock(DynamoDbIndex.class);
        Page<Transaction> page = Page.create(List.of(transaction));

        SdkIterable<Page<Transaction>> pages = mock(SdkIterable.class);
        when(pages.stream()).thenReturn(Stream.of(page));
        when(table.index(USER_ID_INDEX)).thenReturn(index);
        when(index.query(any(QueryConditional.class))).thenReturn(pages);

        TransactionRepository repository = new TransactionRepository(table);

        List<Transaction> result = repository.findByUserId("user-1");

        assertEquals(1, result.size());
        assertEquals("txn-123", result.get(0).getTransactionId());
        verify(table).index(USER_ID_INDEX);
        verify(index).query(any(QueryConditional.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnEmptyListWhenUserIdHasNoTransactions() {
        DynamoDbIndex<Transaction> index = mock(DynamoDbIndex.class);
        Page<Transaction> page = Page.create(List.of());

        SdkIterable<Page<Transaction>> pages = mock(SdkIterable.class);
        when(pages.stream()).thenReturn(Stream.of(page));
        when(table.index(USER_ID_INDEX)).thenReturn(index);
        when(index.query(any(QueryConditional.class))).thenReturn(pages);

        TransactionRepository repository = new TransactionRepository(table);

        List<Transaction> result = repository.findByUserId("no-transactions-user");

        assertTrue(result.isEmpty());
    }
}
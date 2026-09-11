package com.fraud_detector.project.exception;

public class TransactionAnalysisException extends RuntimeException {
    public TransactionAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionAnalysisException(String message) {
        super(message);
    }
}

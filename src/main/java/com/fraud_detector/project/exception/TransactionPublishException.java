package com.fraud_detector.project.exception;

public class TransactionPublishException extends RuntimeException {
    public TransactionPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
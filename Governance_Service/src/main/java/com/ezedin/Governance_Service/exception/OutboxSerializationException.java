package com.ezedin.Governance_Service.exception;

public class OutboxSerializationException extends RuntimeException {

    public OutboxSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}

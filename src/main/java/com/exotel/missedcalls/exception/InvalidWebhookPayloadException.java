package com.exotel.missedcalls.exception;

/**
 * Thrown when an incoming Exotel webhook payload fails validation
 * (e.g., missing CallSid or From number).
 */
public class InvalidWebhookPayloadException extends RuntimeException {

    public InvalidWebhookPayloadException(String message) {
        super(message);
    }
}

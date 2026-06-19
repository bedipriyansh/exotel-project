package com.exotel.missedcalls.exception;

/**
 * Thrown (internally caught) when a webhook delivers a call_sid
 * that has already been processed. Used to short-circuit duplicate
 * processing while still returning a 200 OK to Exotel.
 */
public class DuplicateCallSidException extends RuntimeException {

    public DuplicateCallSidException(String message) {
        super(message);
    }
}

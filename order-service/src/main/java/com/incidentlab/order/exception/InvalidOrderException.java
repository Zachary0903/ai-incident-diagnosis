package com.incidentlab.order.exception;

/** The order request is malformed (e.g. non-positive quantity). */
public class InvalidOrderException extends RuntimeException {

    public InvalidOrderException(String message) {
        super(message);
    }
}

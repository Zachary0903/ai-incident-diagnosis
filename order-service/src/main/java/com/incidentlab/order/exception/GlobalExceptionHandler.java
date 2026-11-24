package com.incidentlab.order.exception;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Timeouts / connection failures calling inventory-service. */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, String>> downstreamUnavailable(ResourceAccessException e) {
        log.error("Inventory call failed: {}", e.getMessage(), e);
        return error(HttpStatus.SERVICE_UNAVAILABLE, "inventory unavailable");
    }

    /** inventory-service answered with an error status (e.g. 409 out of stock). */
    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<Map<String, String>> downstreamError(RestClientResponseException e) {
        log.warn("Inventory returned {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
        return error(HttpStatus.valueOf(e.getStatusCode().value()), "inventory rejected reservation");
    }

    @ExceptionHandler(UnknownProductException.class)
    public ResponseEntity<Map<String, String>> unknownProduct(UnknownProductException e) {
        log.warn("Order rejected: {}", e.getMessage());
        return error(HttpStatus.NOT_FOUND, "unknown product " + e.getSku());
    }

    @ExceptionHandler(InvalidOrderException.class)
    public ResponseEntity<Map<String, String>> invalidOrder(InvalidOrderException e) {
        log.warn("Order rejected: {}", e.getMessage());
        return error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> unexpected(Exception e) {
        log.error("Unhandled error while processing request", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "internal error");
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }
}

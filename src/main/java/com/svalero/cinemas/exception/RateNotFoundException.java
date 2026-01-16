package com.svalero.cinemas.exception;

public class RateNotFoundException extends RuntimeException {
    public RateNotFoundException() {
        super("Rate not found");
    }
    public RateNotFoundException(String message) {
        super(message);
    }
}


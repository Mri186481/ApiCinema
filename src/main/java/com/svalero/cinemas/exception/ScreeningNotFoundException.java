package com.svalero.cinemas.exception;

public class ScreeningNotFoundException extends RuntimeException {
    public ScreeningNotFoundException() {
        super("Screening not found");
    }

    public ScreeningNotFoundException(String message) {
        super(message);
    }
}

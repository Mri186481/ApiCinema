package com.svalero.cinemas.exception;

public class SeatNotFoundException extends RuntimeException {
    public SeatNotFoundException() {
        super("Seat not found");
    }
    public SeatNotFoundException(String message) {
        super(message);
    }
}


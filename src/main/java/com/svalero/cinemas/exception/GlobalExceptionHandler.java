package com.svalero.cinemas.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MovieNotFoundException.class)
    public ResponseEntity<Void> handleMovieNotFound(MovieNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }
}

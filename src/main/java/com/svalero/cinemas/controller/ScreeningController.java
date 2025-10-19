package com.svalero.cinemas.controller;

import com.svalero.cinemas.domain.dto.ScreeningInDto;
import com.svalero.cinemas.domain.dto.ScreeningOutDto;
import com.svalero.cinemas.exception.ScreeningNotFoundException;
import com.svalero.cinemas.service.ScreeningService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ScreeningController {

    @Autowired
    private ScreeningService screeningService;

    @GetMapping("/screenings")
    public ResponseEntity<List<ScreeningOutDto>> getAllScreenings() {
        List<ScreeningOutDto> screenings = screeningService.findAll();
        return ResponseEntity.ok(screenings);
    }

    @GetMapping("/screenings/{screeningId}")
    public ResponseEntity<ScreeningOutDto> getScreeningById(@PathVariable Long screeningId) throws ScreeningNotFoundException {
        ScreeningOutDto screening = screeningService.findById(screeningId);
        return ResponseEntity.ok(screening);
    }

    @PostMapping("/screenings")
    public ResponseEntity<ScreeningOutDto> addScreening(@Valid @RequestBody ScreeningInDto screeningInDto) throws ScreeningNotFoundException {
        ScreeningOutDto addScreening = screeningService.add(screeningInDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(addScreening);
    }

    @PutMapping("/screenings/{screeningId}")
    public ResponseEntity<ScreeningOutDto> modifyScreening(@Valid @PathVariable Long screeningId, @RequestBody ScreeningInDto screeningInDto) throws ScreeningNotFoundException {
        ScreeningOutDto modifyScreening = screeningService.modify(screeningId, screeningInDto);
        return new ResponseEntity<>(modifyScreening, HttpStatus.OK);
    }

    @DeleteMapping("/screenings/{screeningId}")
    public ResponseEntity<Void> deleteScreening(@PathVariable Long screeningId) throws ScreeningNotFoundException {
        screeningService.delete(screeningId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ScreeningNotFoundException.class)
    public ResponseEntity<String> handleScreeningNotFound(ScreeningNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body("Validation failed: " +
                ex.getBindingResult().getFieldErrors().stream()
                        .map(error -> error.getField() + " " + error.getDefaultMessage())
                        .findFirst().orElse("Unknown error"));
    }
}
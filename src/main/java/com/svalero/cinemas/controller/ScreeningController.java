package com.svalero.cinemas.controller;

import com.svalero.cinemas.domain.dto.ErrorResponse;
import com.svalero.cinemas.domain.dto.ScreeningInDto;
import com.svalero.cinemas.domain.dto.ScreeningOutDto;
import com.svalero.cinemas.exception.ScreeningNotFoundException;
import com.svalero.cinemas.service.ScreeningService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ScreeningController {

    @Autowired
    private ScreeningService screeningService;

    private final Logger logger = LoggerFactory.getLogger(MovieController.class);

    @GetMapping("/screenings")
    public ResponseEntity<List<ScreeningOutDto>> getAllScreenings() {
        logger.info("BEGIN Allscreenings");
        List<ScreeningOutDto> screenings = screeningService.findAll();
        logger.info("END Allscreeenings");
        return ResponseEntity.ok(screenings);
    }

    @GetMapping("/screenings/{screeningId}")
    public ResponseEntity<ScreeningOutDto> getScreeningById(@PathVariable Long screeningId) throws ScreeningNotFoundException {
        logger.info("BEGIN getScreeningById");
        ScreeningOutDto screening = screeningService.findById(screeningId);
        logger.info("END screeningsById");
        return ResponseEntity.ok(screening);
    }

    @PostMapping("/screenings")
    public ResponseEntity<ScreeningOutDto> addScreening(@Valid @RequestBody ScreeningInDto screeningInDto) throws ScreeningNotFoundException {
        logger.info("BEGIN addScreening");
        ScreeningOutDto addScreening = screeningService.add(screeningInDto);
        logger.info("END addScreening");
        return ResponseEntity.status(HttpStatus.CREATED).body(addScreening);
    }

    @PutMapping("/screenings/{screeningId}")
    public ResponseEntity<ScreeningOutDto> modifyScreening(@Valid @PathVariable Long screeningId, @RequestBody ScreeningInDto screeningInDto) throws ScreeningNotFoundException {
        logger.info("BEGIN modifyScreening");
        ScreeningOutDto modifyScreening = screeningService.modify(screeningId, screeningInDto);
        logger.info("END modifyScreening");
        return new ResponseEntity<>(modifyScreening, HttpStatus.OK);
    }

    @DeleteMapping("/screenings/{screeningId}")
    public ResponseEntity<Void> deleteScreening(@PathVariable Long screeningId) throws ScreeningNotFoundException {
        logger.info("BEGIN deleteScreening");
        screeningService.delete(screeningId);
        logger.info("END deleteScreening");
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ScreeningNotFoundException.class)
    public ResponseEntity<String> handleScreeningNotFound(ScreeningNotFoundException e) {
        logger.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> MethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });
        logger.error(exception.getMessage(), exception);

        return new ResponseEntity<>(ErrorResponse.validationError(errors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        // al usuario le digo esto
        ErrorResponse error = ErrorResponse.generalError(500, "Internal Server Error");
        //pero yo en mi log me lo guardo de verdad, para no dar detalle y no tener brecha
        logger.error(exception.getMessage(), exception);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
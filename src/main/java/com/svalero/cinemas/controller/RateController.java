package com.svalero.cinemas.controller;


import com.svalero.cinemas.domain.Rate;
import com.svalero.cinemas.domain.dto.*;
import com.svalero.cinemas.exception.RateNotFoundException;
import com.svalero.cinemas.service.RateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rates")
@RequiredArgsConstructor
public class RateController {
    @Autowired
    private RateService rateService;

    //defino el objeto logger basado en la clase Logger
    private final Logger logger = LoggerFactory.getLogger(RateController.class);
    // Obtener todos las tarifas
    @GetMapping
    public ResponseEntity<List<RateOutDto>> getAll(@RequestParam(value = "rateDate", required = false) LocalDate rateDate,
                                                   @RequestParam(value = "nameDayRate", required = false) String nameDayRate,
                                                   @RequestParam(value = "promoDay",  required = false) Boolean promoDay) {
        logger.info("Begin Get all Rates");
        List<RateOutDto> rates = rateService.getAll(rateDate, nameDayRate,promoDay);
        logger.info("End all Rates");
        return new ResponseEntity<>(rates, HttpStatus.OK);
    }
    // Obtener una tarifa por ID
    @GetMapping("/{rateId}")
    public ResponseEntity<Rate> getRateById(@PathVariable Long rateId) throws RateNotFoundException {
        logger.info("Begin Get Rate");
        Rate rate = rateService.get(rateId);
        logger.info("Fetching Rate with id: {}", rateId);
        return new ResponseEntity<>(rate, HttpStatus.OK);
    }
    // Agregar un nueva tarifa
    @PostMapping
    public ResponseEntity<RateOutDto> addRate( @Valid @RequestBody RateInDto rateInDto) {
        logger.info("Adding new rate");
        RateOutDto addRate = rateService.add(rateInDto);
        logger.info("End adding new rate");
        return new ResponseEntity<>(addRate, HttpStatus.CREATED);
    }

    // Modificar una tarifa
    @PutMapping("/{rateId}")
    public ResponseEntity<RateOutDto> modifyRate(@PathVariable long rateId, @Valid @RequestBody RateInDto rate)
            throws RateNotFoundException {
        logger.info("Begin Modify Rate");
        RateOutDto modifiedRate = rateService.modify(rateId, rate);
        logger.info("End Modify Rate");
        return ResponseEntity.ok(modifiedRate);
    }
    // Borrar una tarifa
    @DeleteMapping("/{rateId}")
    public ResponseEntity<Rate> deleteRate(@PathVariable long rateId) throws RateNotFoundException {
        logger.info("Deleting rate with id: {}", rateId);
        rateService.delete(rateId);
        return ResponseEntity.noContent().build();
    }

    // Manejo de excepción: User no encontrado
    @ExceptionHandler(RateNotFoundException.class)
    public ResponseEntity<String> handleRateNotFound(RateNotFoundException e) {
        logger.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
    // Manejo de excepciones por validaciones incorrectas
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
    // Manejo de error genérico
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        // al usuario le digo esto
        ErrorResponse error = ErrorResponse.generalError(500, "Internal Server Error");
        //pero yo en mi log me lo guardo de verdad, para no dar detalle y no tener brecha
        logger.error(exception.getMessage(), exception);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}


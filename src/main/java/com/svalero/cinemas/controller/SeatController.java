package com.svalero.cinemas.controller;

import com.svalero.cinemas.domain.Seat;
import com.svalero.cinemas.domain.dto.*;
import com.svalero.cinemas.exception.SeatNotFoundException;
import com.svalero.cinemas.service.SeatService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
//@RequestMapping("/seats")
@RequiredArgsConstructor
public class SeatController {
    @Autowired
    private SeatService seatService;

    private final Logger logger = LoggerFactory.getLogger(SeatController.class);
    // Obtener todos los usuarios
    @GetMapping("/seats")
    public ResponseEntity<List<SeatOutDto>> getAll(@RequestParam(value = "seatRow", required = false) Integer seatRow,
                                                   @RequestParam(value = "seatColumn", required = false) Integer seatColumn,
                                                   @RequestParam(value = "status", defaultValue = "") String status) {
        logger.info("Begin Get all seats");
        List<SeatOutDto> seats = seatService.getAll(seatRow,seatColumn,status);
        logger.info("End all seats");
        return new ResponseEntity<>(seats, HttpStatus.OK);
    }

    // Obtener un seat por ID
    @GetMapping("/seats/{seatId}")
    public ResponseEntity<Seat> getSeatById(@PathVariable Long seatId) throws SeatNotFoundException {
        logger.info("Begin Get seat");
        Seat seat = seatService.get(seatId);
        logger.info("Fetching seat with id: {}", seatId);
        return new ResponseEntity<>(seat, HttpStatus.OK);
    }

    // Agregar una nueva butaca
    @PostMapping("/rooms/{roomId}/seats")
    public ResponseEntity<SeatOutDto> addSeat(@PathVariable long roomId, @Valid @RequestBody SeatInDto seat) {
        logger.info("Adding new seat");
        SeatOutDto newSeat = seatService.add(roomId, seat);
        logger.info("End adding new seat");
        return new ResponseEntity<>(newSeat, HttpStatus.CREATED);
    }

    // Modificar una butaca
    @PutMapping("/seats/{seatId}")
    public ResponseEntity<SeatOutDto> modifySeat(@PathVariable long seatId, @Valid  @RequestBody SeatInDto seat)
            throws SeatNotFoundException {
        logger.info("Begin Modify seat");
        SeatOutDto modifiedSeat = seatService.modify(seatId, seat);
        logger.info("End Modify seat");
        return ResponseEntity.ok(modifiedSeat);
    }

    // Modificar una butaca parcialmente
    @PatchMapping("/seats/{seatId}")
    public ResponseEntity<SeatOutDto> modifySeatPartial(@PathVariable long seatId, @RequestBody Map<String, Object> updates)
            throws SeatNotFoundException {
        logger.info("Begin ModifyPartial seat");
        SeatOutDto modifiedSeat = seatService.modifyPartial(seatId, updates);
        logger.info("End ModifyPartial seat");
        return ResponseEntity.ok(modifiedSeat);
    }

    // Borrar una butaca
    @DeleteMapping("/seats/{seatId}")
    public ResponseEntity<Seat> deleteSeat(@PathVariable long seatId) throws SeatNotFoundException {
        logger.info("Deleting seat with id: {}", seatId);
        seatService.delete(seatId);
        return ResponseEntity.noContent().build();
    }

    // Manejo de excepción: Seat no encontrado
    @ExceptionHandler(SeatNotFoundException.class)
    public ResponseEntity<String> handlerSeatNotFound(SeatNotFoundException e) {
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
        ErrorResponse error = ErrorResponse.generalError(500, "Internal Server Error");
        logger.error(exception.getMessage(), exception);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
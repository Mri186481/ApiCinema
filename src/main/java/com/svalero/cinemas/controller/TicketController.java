package com.svalero.cinemas.controller;


import com.svalero.cinemas.domain.Ticket;
import com.svalero.cinemas.domain.dto.*;
import com.svalero.cinemas.exception.*;
import com.svalero.cinemas.service.TicketService;
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

@RequiredArgsConstructor
public class TicketController {
    @Autowired
    private TicketService ticketService;

    //defino el objeto logger basado en la clase Logger
    private final Logger logger = LoggerFactory.getLogger(TicketController.class);
    // Obtener todos los tickets
    @GetMapping("/tickets")
    public ResponseEntity<List<TicketOutDto>> getAll(@RequestParam(value = "customerId", required = false) Long customerId,
                                                     @RequestParam(value = "screeningId", required = false) Long screeningId,
                                                     @RequestParam(value = "seatId", required = false) Long seatId)   {
        logger.info("Begin Get all tickets with filters: customerId={}, screeningId={}, seatId={}", customerId, screeningId, seatId);
        List<TicketOutDto> tickets = ticketService.getAll(customerId, screeningId, seatId);
        logger.info("End all tickets");
        return new ResponseEntity<>(tickets, HttpStatus.OK);
    }


    // Obtener un ticket por ID
    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<TicketOutDto> getTicketById(@PathVariable Long ticketId) throws TicketNotFoundException {
        logger.info("Begin Get ticket");
        TicketOutDto ticketOutDto = ticketService.get(ticketId);
        logger.info("Fetching ticket with id: {}", ticketId);
        return new ResponseEntity<>(ticketOutDto, HttpStatus.OK);
    }
    //Grabar un nuevo ticket
    @PostMapping("/tickets")
    public ResponseEntity<TicketOutDto> addTickets(@Valid @RequestBody TicketInDto ticketInDto) throws ScreeningNotFoundException, CustomerNotFoundException, RateNotFoundException,SeatNotFoundException {
        logger.info("BEGIN addTickets");
        TicketOutDto addTicket = ticketService.add(ticketInDto);
        logger.info("END addTickets");
        return ResponseEntity.status(HttpStatus.CREATED).body(addTicket);
    }

    // Modificar un ticket
    @PutMapping("/tickets/{ticketId}")
    public ResponseEntity<TicketOutDto> modifyTicket(@PathVariable Long ticketId, @Valid  @RequestBody TicketInDto ticketInDto) throws TicketNotFoundException, ScreeningNotFoundException, CustomerNotFoundException, RateNotFoundException,SeatNotFoundException {
        logger.info("BEGIN modifyTicket");
        TicketOutDto modifyTicket = ticketService.modify(ticketId, ticketInDto);
        logger.info("END modifyTicket");
        return new ResponseEntity<>(modifyTicket, HttpStatus.OK);
    }

    // Modificar un ticket parcialmente
    @PatchMapping("/tickets/{ticketId}")
    public ResponseEntity<TicketOutDto> modifyTicketPartial(@PathVariable Long ticketId, @RequestBody Map<String, Object> updates) throws TicketNotFoundException, ScreeningNotFoundException, CustomerNotFoundException, RateNotFoundException,SeatNotFoundException {
        logger.info("BEGIN modifyTicket");
        TicketOutDto modifyTicket = ticketService.modifyPartial(ticketId, updates);
        logger.info("END modifyTicket");
        return new ResponseEntity<>(modifyTicket, HttpStatus.OK);
    }

    // Borrar una ticket
    @DeleteMapping("/tickets/{ticketId}")
    public ResponseEntity<Ticket> deleteTicket(@PathVariable long ticketId) throws TicketNotFoundException {
        logger.info("Deleting ticket with id: {}", ticketId);
        ticketService.delete(ticketId);
        return ResponseEntity.noContent().build();
    }

    // Manejo de excepción: Ticket no encontrado
    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<String> handlerTicketNotFound(TicketNotFoundException e) {
        logger.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
    @ExceptionHandler(ScreeningNotFoundException.class)
    public ResponseEntity<String> handlerScreeningNotFound(ScreeningNotFoundException e) {
        logger.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<String> handlerCustomerNotFound(CustomerNotFoundException e) {
        logger.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
    @ExceptionHandler(RateNotFoundException.class)
    public ResponseEntity<String> handlerRateNotFound(RateNotFoundException e) {
        logger.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
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
        // al usuario le digo esto
        ErrorResponse error = ErrorResponse.generalError(500, "Internal Server Error");
        //pero yo en mi log me lo guardo de verdad, para no dar detalle y no tener brecha
        logger.error(exception.getMessage(), exception);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}

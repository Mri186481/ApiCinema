package com.svalero.cinemas.controller;

import com.svalero.cinemas.domain.Room;
import com.svalero.cinemas.domain.dto.*;
import com.svalero.cinemas.exception.RoomNotFoundException;
import com.svalero.cinemas.service.RoomService;
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
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {
    @Autowired
    private RoomService roomService;

    private final Logger logger = LoggerFactory.getLogger(RoomController.class);
    // Obtener todos los usuarios
    @GetMapping
    public ResponseEntity<List<RoomOutDto>> getAll(@RequestParam(value = "room3d", required = false) Boolean room3d,
                                                   @RequestParam(value = "roomAtmos", required = false) Boolean roomAtmos,
                                                   @RequestParam(value = "roomLaser", required = false) Boolean roomLaser) {
        logger.info("Begin Get all rooms");
        List<RoomOutDto> rooms = roomService.getAll(room3d, roomAtmos,roomLaser);
        logger.info("End all rooms");
        return new ResponseEntity<>(rooms, HttpStatus.OK);
    }

    // Obtener un room por ID
    @GetMapping("/{roomId}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long roomId) throws RoomNotFoundException {
        logger.info("Begin Get room");
        Room room = roomService.get(roomId);
        logger.info("Fetching room with id: {}", roomId);
        return new ResponseEntity<>(room, HttpStatus.OK);
    }

    // Agregar una nueva sala
    @PostMapping
    public ResponseEntity<RoomOutDto> addRoom( @Valid @RequestBody RoomInDto roomInDto) {
        logger.info("Adding new room");
        RoomOutDto addRoom = roomService.add(roomInDto);
        logger.info("End adding new customer");
        return new ResponseEntity<>(addRoom, HttpStatus.CREATED);
    }

    // Modificar una sala
    @PutMapping("/{roomId}")
    public ResponseEntity<RoomOutDto> modifyRoom(@PathVariable long roomId, @Valid  @RequestBody RoomInDto room)
            throws RoomNotFoundException {
        logger.info("Begin Modify room");
        RoomOutDto modifiedRoom = roomService.modify(roomId, room);
        logger.info("End Modify room");
        return ResponseEntity.ok(modifiedRoom);
    }

    //Modificar una sala Parcialmente
    @PatchMapping("/{roomId}")
    public ResponseEntity<RoomOutDto> modifyRoomPartial(@PathVariable long roomId, @RequestBody Map<String, Object> updates)
            throws RoomNotFoundException {
        logger.info("Begin Modify room partial");
        RoomOutDto modifiedRoom = roomService.modifyPartial(roomId, updates);
        logger.info("End Modify room partial");
        return ResponseEntity.ok(modifiedRoom);
    }

    // Borrar una sala
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Room> deleteRoom(@PathVariable long roomId) throws RoomNotFoundException {
        logger.info("Deleting room with id: {}", roomId);
        roomService.delete(roomId);
        return ResponseEntity.noContent().build();
    }

    // Manejo de excepción: Room no encontrado
    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<String> handlerRoomNotFound(RoomNotFoundException e) {
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
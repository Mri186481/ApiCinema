package com.svalero.cinemas.controller;

import com.svalero.cinemas.domain.Customer;
import com.svalero.cinemas.domain.dto.CustomerInDto;
import com.svalero.cinemas.domain.dto.CustomerOutDto;
import com.svalero.cinemas.domain.dto.ErrorResponse;
import com.svalero.cinemas.exception.CustomerNotFoundException;
import com.svalero.cinemas.service.CustomerService;
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
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    //defino el objeto logger basado en la clase Logger
    private final Logger logger = LoggerFactory.getLogger(CustomerController.class);
    // Obtener todos los usuarios
    @GetMapping
    public ResponseEntity<List<CustomerOutDto>> getAll(@RequestParam(value = "name", defaultValue = "") String name,
                                                 @RequestParam(value = "address", defaultValue = "") String address,
                                                 @RequestParam(value = "mail", defaultValue ="") String mail) {
        logger.info("Begin Get all Customers");
        List<CustomerOutDto> customers = customerService.getAll(name, address,mail);
        logger.info("End all customers");
        return new ResponseEntity<>(customers, HttpStatus.OK);
    }
    // Obtener un usuario por ID
    @GetMapping("/{customerId}")
    public ResponseEntity<Customer> getUserById(@PathVariable Long customerId) throws CustomerNotFoundException {
        logger.info("Begin Get customer");
        Customer customer = customerService.get(customerId);
        logger.info("Fetching customer with id: {}", customerId);
        return new ResponseEntity<>(customer, HttpStatus.OK);
    }
    // Obtener lista de usuarios que admiten o no publicidad
    // Obtener un usuario por ID
    @GetMapping("/admitsAdvertising/{admitsAdvertising}")
    public ResponseEntity<List<CustomerOutDto>> getUserByAd(@PathVariable boolean admitsAdvertising) throws CustomerNotFoundException {
        logger.info("Begin Get List of customer with ad");
        List<CustomerOutDto> customers = customerService.getAllAd(admitsAdvertising);
        logger.info("Begin Get List of customer with ad");
        return new ResponseEntity<>(customers, HttpStatus.OK);
    }

    // Agregar un nuevo usuario
    @PostMapping
    public ResponseEntity<CustomerOutDto> addUser(@RequestBody CustomerInDto customerInDto) {
        logger.info("Adding new customer");
        CustomerOutDto addUser = customerService.add(customerInDto);
        logger.info("End adding new customer");
        return new ResponseEntity<>(addUser, HttpStatus.CREATED);
    }

    // Modificar un usuario
    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerOutDto> modifyUser(@PathVariable long customerId, @RequestBody CustomerInDto customer)
            throws CustomerNotFoundException {
        logger.info("Begin Modify customer");
        CustomerOutDto modifiedUser = customerService.modify(customerId, customer);
        logger.info("End Modify customer");
        return ResponseEntity.ok(modifiedUser);
    }
    // Borrar un usuario
    // Eliminar un user
    @DeleteMapping("/{customerId}")
    public ResponseEntity<Customer> deleteUser(@PathVariable long customerId) throws CustomerNotFoundException {
        logger.info("Deleting user with id: {}", customerId);
        customerService.delete(customerId);
        return ResponseEntity.noContent().build();
    }

    // Manejo de excepción: User no encontrado
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<String> handleCustomerNotFound(CustomerNotFoundException e) {
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


package com.svalero.cinemas.controller;

import com.svalero.cinemas.domain.Movie;
import com.svalero.cinemas.domain.dto.ErrorResponse;
import com.svalero.cinemas.domain.dto.MovieInDto;
import com.svalero.cinemas.exception.MovieNotFoundException;
import com.svalero.cinemas.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieController {
    @Autowired
    private  MovieService movieService;

    //defino el objeto logger basado en la clase Logger
    private final Logger logger = LoggerFactory.getLogger(MovieController.class);

    // Obtener todas las películas con posibilidad de filtro por genero
    @GetMapping
    public ResponseEntity<List<Movie>> getAllMovies(@RequestParam(value = "title", defaultValue = "") String title,
                                                    @RequestParam(value = "genre", defaultValue = "") String genre,
                                                    @RequestParam(value = "durationMinutes", required = false) Integer durationMinutes) {

        logger.info("BEGIN getAllMovies");
        List<Movie> movies = movieService.findAll(title, genre, durationMinutes);
        logger.info("END getAllMovies");
        return new ResponseEntity<>(movies, HttpStatus.OK);
    }

    // Buscar película por ID
    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovieById(@PathVariable Long id)  {
//        logger.info("BEGIN getMovieById");
//        return movieService.findById(id)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
        logger.info("BEGIN getMovieById");
        Optional<Movie> movieOptional = movieService.findById(id);



        if (movieOptional.isPresent()) {
            logger.info("END getMovieById - Movie found");
            return ResponseEntity.ok(movieOptional.get());
        } else {
            logger.info("END getMovieById - Movie not found");
            return ResponseEntity.notFound().build();
        }
    }

    // Buscar película por título
    @GetMapping("/title/{title}")
    public ResponseEntity<List<Movie>> getByTitle(@PathVariable String title) throws MovieNotFoundException {
        logger.info("BEGIN getByTitle");
        List<Movie> movie = movieService.findByTitle(title);
        logger.info("END getByTitle");
        return ResponseEntity.ok(movie);

    }

    // Buscar película por currentlyShowing usando JPQL
    @GetMapping("/currentlyShowing/{currentlyShowing}")
    public ResponseEntity<List<Movie>> getBycurrentlyShowing(@PathVariable boolean currentlyShowing) throws MovieNotFoundException {
        logger.info("BEGIN getBycurrentlyShowing");
        List<Movie> movie = movieService.findBycurrentlyShowing(currentlyShowing);
        logger.info("END getBycurrentlyShowing");
        return ResponseEntity.ok(movie);

    }

    // Crear nueva película
    @PostMapping
    public ResponseEntity<Movie> createMovie(@Valid @RequestBody MovieInDto movieInDto) {
        logger.info("BEGIN createMovie");
        Movie movie = movieService.create(movieInDto);
        logger.info("END createMovies");
        return new ResponseEntity<>(movie, HttpStatus.OK);
//        return ResponseEntity.ok(movieService.create(movieInDto));

    }

    // Actualizar película
    @PutMapping("/{id}")
    public ResponseEntity<Movie> updateMovie(@PathVariable Long id, @RequestBody MovieInDto movieInDto) throws MovieNotFoundException{
        logger.info("BEGIN updateMovie");
        Movie movie = movieService.update(id, movieInDto);
        logger.info("END updateMovie");
        return new ResponseEntity<>(movie, HttpStatus.OK);


//        return ResponseEntity.ok(movieService.update(id, movieInDto));
    }
    // Actualizar Tabla parcialmente
    @PatchMapping ("/{id}")
    public ResponseEntity<Movie> updateMoviePartial(@PathVariable Long id, @RequestBody Map<String, Object> updates) throws MovieNotFoundException{
        logger.info("BEGIN updateMoviePartial");
        Movie updatedMovie = movieService.updatePartial(id, updates);
        logger.info("END updateMoviePartial");
        return new ResponseEntity<>(updatedMovie, HttpStatus.OK);


//        return ResponseEntity.ok(movieService.update(id, movieInDto));
    }
    @GetMapping("/release-date/{date}")
    public ResponseEntity<List<Movie>> getByReleaseDate(@PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        logger.info("BEGIN getByReleaseDate");
        List<Movie> movies = movieService.findByReleaseDate(date);
        logger.info("END getByReleaseDate");
        return new ResponseEntity<>(movies, HttpStatus.OK);
//        return ResponseEntity.ok(movieService.findByReleaseDate(date));
    }


    // Eliminar película
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) throws MovieNotFoundException {
        logger.info("BEGIN deleteMovie");
        movieService.delete(id);
        logger.info("END deleteMovie");
        return ResponseEntity.noContent().build();
    }
    // Manejo de excepción: Movie no encontrado
    @ExceptionHandler(MovieNotFoundException.class)
    public ResponseEntity<String> handleMovieNotFound(MovieNotFoundException e) {
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
package com.svalero.cinemas.controller;

import com.svalero.cinemas.domain.Movie;
import com.svalero.cinemas.domain.dto.MovieInDto;
import com.svalero.cinemas.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieController {
    @Autowired
    private  MovieService movieService;

    // Obtener todas las películas
    @GetMapping
    public ResponseEntity<List<Movie>> getAllMovies() {
        return ResponseEntity.ok(movieService.findAll());
    }

    // Buscar película por ID
    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovieById(@PathVariable Long id) {
        return movieService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Buscar película por título
    @GetMapping("/title/{title}")
    public ResponseEntity<Movie> getByTitle(@PathVariable String title) {
        Movie movie = movieService.findByTitle(title);
        if (movie != null) {
            return ResponseEntity.ok(movie);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Buscar película por género
    @GetMapping("/genre/{genre}")
    public ResponseEntity<Movie> getByGenre(@PathVariable String genre) {
        Movie movie = movieService.findByGenre(genre);
        if (movie != null) {
            return ResponseEntity.ok(movie);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Crear nueva película
    @PostMapping
    public ResponseEntity<Movie> createMovie(@Valid @RequestBody MovieInDto movieInDto) {
        return ResponseEntity.ok(movieService.create(movieInDto));
    }

    // Actualizar película
    @PutMapping("/{id}")
    public ResponseEntity<Movie> updateMovie(@PathVariable Long id, @RequestBody MovieInDto movieInDto) {
        return ResponseEntity.ok(movieService.update(id, movieInDto));
    }

    @GetMapping("/release-date/{date}")
    public ResponseEntity<List<Movie>> getByReleaseDate(@PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(movieService.findByReleaseDate(date));
    }


    // Eliminar película
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        movieService.delete(id);
        return ResponseEntity.noContent().build();
    }
}


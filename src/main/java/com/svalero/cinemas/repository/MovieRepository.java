package com.svalero.cinemas.repository;

import com.svalero.cinemas.domain.Movie;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface MovieRepository extends CrudRepository<Movie, Long> {

    // Método para obtener todos los usuarios
    List<Movie> findAll();


    // Método para buscar una pelicula por su titulo
    List<Movie> findByMovieTitle(String movieTitle);

    List<Movie> findByGenre(String genre);

    List<Movie> findByDurationMinutes(Integer durationMinutes);

    List<Movie> findByReleaseDate(LocalDate releaseDate);

    List<Movie> findByMovieTitleAndGenreAndDurationMinutes(String movieTitle,String genre, Integer durationMinutes);

    List<Movie> findByMovieTitleAndGenre(String movieTitle,String genre);

    List<Movie> findByMovieTitleAndDurationMinutes(String movieTitle,Integer durationMinutes);

    List<Movie> findByGenreAndDurationMinutes(String genre,Integer durationMinutes);

    @Query("select m FROM Movie m WHERE m.currentlyShowing = :currentlyShowing")
    List<Movie> findAllMoviesByCurrentlyShowing(Boolean currentlyShowing);

}




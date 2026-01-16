package com.svalero.cinemas.service;

import com.svalero.cinemas.domain.Movie;
import com.svalero.cinemas.domain.dto.*;
import com.svalero.cinemas.exception.MovieNotFoundException;
import com.svalero.cinemas.repository.MovieRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    public MovieService(MovieRepository movieRepository, ModelMapper modelMapper) {
        this.movieRepository = movieRepository;
        this.modelMapper = modelMapper;
    }

    // Obtener todas las películas
    public List<MovieOutDto> findAll(String movieTitle, String genre, Integer durationMinutes) {
        List<Movie> movieList;

        boolean hasTitle = !movieTitle.isEmpty();
        boolean hasGenre = !genre.isEmpty();
        boolean hasDurationMinutes = durationMinutes != null;

        if (hasTitle && hasGenre && hasDurationMinutes) {
            movieList = movieRepository.findByMovieTitleAndGenreAndDurationMinutes(movieTitle, genre, durationMinutes);
        } else if (hasTitle && hasGenre) {
            movieList = movieRepository.findByMovieTitleAndGenre(movieTitle,genre);
        } else if (hasTitle && hasDurationMinutes) {
            movieList = movieRepository.findByMovieTitleAndDurationMinutes(movieTitle,durationMinutes);
        } else if (hasGenre && hasDurationMinutes) {
            movieList = movieRepository.findByGenreAndDurationMinutes(genre, durationMinutes);
        } else if (hasTitle) {
            movieList = movieRepository.findByMovieTitle(movieTitle);
        } else if (hasGenre) {
            movieList = movieRepository.findByGenre(genre);
        } else if (hasDurationMinutes) {
            movieList = movieRepository.findByDurationMinutes(durationMinutes);
        } else {
            movieList = movieRepository.findAll();
        }

        return modelMapper.map(movieList, new TypeToken<List<MovieOutDto>>() {}.getType());
    }

    public MovieOutDto findById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie with ID " + id + " not found"));
        return convertToOutDto(movie);
    }

    // Buscar por título
    public List<MovieOutDto> findByTitle(String movieTitle) {
        List<Movie> movie = movieRepository.findByMovieTitle(movieTitle);
        if (movie == null) {
            throw new MovieNotFoundException("Movie not found with title: " + movieTitle);
        }
        return modelMapper.map(movie, new TypeToken<List<MovieOutDto>>() {}.getType());

    }

    //Buscar Por ReleaseDate
    public List<MovieOutDto> findByReleaseDate(LocalDate releaseDate) {
        List<Movie> movie = movieRepository.findByReleaseDate(releaseDate);
        return modelMapper.map(movie, new TypeToken<List<MovieOutDto>>() {}.getType());
    }

    //Buscar Por CurrentlyShowing
    public List<MovieOutDto> findBycurrentlyShowing(boolean currentlyShowing) {
        List<Movie> movie = movieRepository.findAllMoviesByCurrentlyShowing(currentlyShowing);
        return modelMapper.map(movie, new TypeToken<List<MovieOutDto>>() {}.getType());
    }

    // Crear nueva película
    public MovieOutDto create(MovieInDto movieInDto) {
        Movie movie = modelMapper.map(movieInDto, Movie.class);
        movie = movieRepository.save(movie);
        return modelMapper.map(movie, MovieOutDto.class);
    }

    // Actualizar película completa
    public Movie update(Long id, MovieInDto movieInDto) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with id: " + id));

        movie.setMovieTitle(movieInDto.getMovieTitle());
        movie.setGenre(movieInDto.getGenre());
        movie.setDurationMinutes(movieInDto.getDurationMinutes());
        movie.setFilmingLatitude(movieInDto.getFilmingLatitude());
        movie.setFilmingLongitude(movieInDto.getFilmingLongitude());
        movie.setReleaseDate(movieInDto.getReleaseDate());
        movie.setCurrentlyShowing(movieInDto.isCurrentlyShowing());

        return movieRepository.save(movie);
//        No funciona bien, si lo muevo manualmente va bien
//        Posteriormente he ayudado al modelmapper creando un mapa especifico y parece que va bien;
//        Estan implementados ahora dos formas de hacer lo mismo, manualmente y con modelmapper
    }

    // Actualización parcial (PATCH)
    public MovieOutDto updatePartial(Long id, Map<String, Object> updates) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new MovieNotFoundException("Movie not found with id: " + id));

        modelMapper.map(updates, movie);
        movieRepository.save(movie);
        return modelMapper.map(movie, MovieOutDto.class);
    }


    // Eliminar película
    public void delete(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new MovieNotFoundException("Movie not found with id: " + id);
        }
        movieRepository.deleteById(id);
    }

    private MovieOutDto convertToOutDto(Movie movie) {
        MovieOutDto dto = modelMapper.map(movie,MovieOutDto.class);
        return dto;
    }
}

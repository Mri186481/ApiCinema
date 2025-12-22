package com.svalero.cinemas;

import com.svalero.cinemas.domain.Movie;
import com.svalero.cinemas.domain.dto.MovieInDto;
import com.svalero.cinemas.domain.dto.MovieOutDto;
import com.svalero.cinemas.exception.MovieNotFoundException;
import com.svalero.cinemas.repository.MovieRepository;
import com.svalero.cinemas.service.MovieService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MovieServiceTests {
    @InjectMocks
    private MovieService movieService;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ModelMapper modelMapper;
    //Este es el primer caso de prueba sin nada, es decir sin parametros
    @Test
    public void testFindAll() {
        //Me voy a imaginar la lista de movies que me va al devolver (
        List<Movie> mockMovieList = List.of(
                new Movie(1L, "Terminator 1", "Scifi", 101, 41.6598777, -0.8835212, LocalDate.parse("1992-08-11"),true, null),
                new Movie(2L, "Terminator 2", "Scifi", 101, 41.6545777, -0.8839212, LocalDate.parse("1984-08-11"),true, null),
                new Movie(3L, "Terminator 3", "Scifi", 101, 41.6598777, -0.8897212, LocalDate.parse("1997-08-11"),true, null)
        );
        List<MovieOutDto> mockModelMapperOut = List.of(
                new MovieOutDto(1L, "Terminator 1", "Scifi", 101, 41.6598777, -0.8835212, LocalDate.parse("1992-08-11"),true),
                new MovieOutDto(2L, "Terminator 2", "Scifi", 101, 41.6545777, -0.8839212, LocalDate.parse("1984-08-11"),true),
                new MovieOutDto(3L, "Terminator 3", "Scifi", 101, 41.6598777, -0.8897212, LocalDate.parse("1997-08-11"),true)
        );
        //cuando se llame al movieRepository.findAll tu devuelve esta lista
        when(movieRepository.findAll()).thenReturn(mockMovieList);
        //cuando llamen a modelmapper quiero que devuelvas una lista de MovieOutDto
        when(modelMapper.map(mockMovieList, new TypeToken<List<MovieOutDto>>() {}.getType())).thenReturn(mockModelMapperOut);

        List<MovieOutDto> actualMovieList = movieService.findAll("","",null);
        assertEquals(3, actualMovieList.size());
        assertEquals("Terminator 1", actualMovieList.getFirst().getMovieTitle());
        assertEquals("Terminator 3", actualMovieList.getLast().getMovieTitle());
        //Puedo comprobar por donde se ejecuta mi codigo
        //Aqui le estamos diciendo que compruebe en movieRepository se ha llamado 1 vez a findAll
        verify(movieRepository, times(1)).findAll();
        //Compruebo algo que no tiene que pasar, es decir un metodo al que no se tiene que llamar
        //Para eso le digo que no se ha llamado nunca ==> 0, no se ha llamdo nunca a findByGenre en este caso
        verify(movieRepository,times(0)).findByGenre("");

    }

    @Test
    public void testFindByMovieTitlel() {

        List<Movie> mockMovieList = List.of(
                new Movie(1L, "Terminator 1", "Scifi", 101, 41.6598777, -0.8835212, LocalDate.parse("1984-08-11"),true, null),
                new Movie(2L, "La Guerra de las Galaxias", "Scifi", 101, 41.6545777, -0.8839212, LocalDate.parse("1977-08-11"),true, null),
                new Movie(3L, "Avatar", "Scifi", 101, 41.6598777, -0.8897212, LocalDate.parse("2010-08-11"),true, null)
        );
        List<MovieOutDto> mockModelMapperOut = List.of(
                new MovieOutDto(1L, "Terminator 1", "Scifi", 101, 41.6598777, -0.8835212, LocalDate.parse("1984-08-11"),true)
        );

        when(movieRepository.findByMovieTitle("Terminator 1")).thenReturn(mockMovieList);
        when(modelMapper.map(mockMovieList, new TypeToken<List<MovieOutDto>>() {}.getType())).thenReturn(mockModelMapperOut);
        //Recuerda que el service tiene tres parametros
        List<MovieOutDto> actualMovieList = movieService.findAll("Terminator 1","",null);
        assertEquals(1, actualMovieList.size());
        assertEquals("Terminator 1", actualMovieList.getFirst().getMovieTitle());
        //No deberia de ejcutarse el findAll
        verify(movieRepository, times(0)).findAll();
        verify(movieRepository,times(0)).findByGenre("");
        verify(movieRepository,times(1)).findByMovieTitle("Terminator 1");

    }

    // ---------------------------------------------------------------------------------
    // TEST GET BY ID
    // ---------------------------------------------------------------------------------

    @Test // Caso 200 OK
    public void testFindById_Success() {
        Long id = 1L;
        Movie mockMovie = new Movie(id, "Terminator 1", "SciFi", 101, 41.6598777, -0.8835212, LocalDate.parse("1984-08-11"), true, null);
        MovieOutDto mockOutDto = new MovieOutDto(id, "Terminator 1", "SciFi", 101, 41.6598777, -0.8835212, LocalDate.parse("1984-08-11"), true);

        // Simulo que el repositorio encuentra la película (devuelve Optional con valor)
        when(movieRepository.findById(id)).thenReturn(Optional.of(mockMovie));
        when(modelMapper.map(mockMovie, MovieOutDto.class)).thenReturn(mockOutDto);

        MovieOutDto result = movieService.findById(id);

        assertNotNull(result);
        assertEquals("Terminator 1", result.getMovieTitle());
        verify(movieRepository, times(1)).findById(id);
    }

    @Test // Caso 404 Not Found
    public void testFindById_NotFound() {
        Long id = 99L;
        //El repositorio NO encuentra nada (Optional empty)
        when(movieRepository.findById(id)).thenReturn(Optional.empty());

        //El servicio lanza la excepción correcta
        MovieNotFoundException exception = assertThrows(MovieNotFoundException.class, () -> {
            movieService.findById(id);
        });

        assertTrue(exception.getMessage().contains("Movie with ID " + id + " not found"));
    }

    // ---------------------------------------------------------------------------------
    // TEST POST (CREATE)
    // ---------------------------------------------------------------------------------
    @Test // Caso 201 Created
    public void testCreate_Success() {
        MovieInDto inDto = new MovieInDto("Avatar", "SciFi", 160, 41.6598777, -0.8835212, LocalDate.now(), true);
        Movie movieEntity = new Movie(null, "Avatar", "SciFi", 160, 41.6598777, -0.8835212, LocalDate.now(), true, null);
        Movie savedMovie = new Movie(1L, "Avatar", "SciFi", 160, 41.6598777, -0.8835212, LocalDate.now(), true, null);
        MovieOutDto outDto = new MovieOutDto(1L, "Avatar", "SciFi", 160, 41.6598777, -0.8835212, LocalDate.now(), true);

        // 1. Mapeo DTO -> Entidad
        when(modelMapper.map(inDto, Movie.class)).thenReturn(movieEntity);
        // 2. Guardado en repositorio
        when(movieRepository.save(movieEntity)).thenReturn(savedMovie);
        // 3. Mapeo Entidad guardada -> DTO Salida
        when(modelMapper.map(savedMovie, MovieOutDto.class)).thenReturn(outDto);

        MovieOutDto result = movieService.create(inDto);

        assertNotNull(result.getId());
        assertEquals("Avatar", result.getMovieTitle());
        verify(movieRepository, times(1)).save(movieEntity);
    }
    //El caso 400 no tiene sentido ya que se han ejecutado las validaciones en la capa Controller con @Valid
    //Si no pasan la capa service no se llega a ejecutar, aqui ya llegan los datos validos
    //Se puede simular un error al guardar en la BD(error de conexion o alguna restriccion UNIQUE que fallara al insertar)
    @Test
    public void testCreate_Error() {
        // Datos de entrada
        MovieInDto inDto = new MovieInDto("Avatar", "SciFi", 164, 0, 0, LocalDate.now(), true);
        Movie movieEntity = new Movie(null, "Avatar", "SciFi", 164, 0, 0, LocalDate.now(), true, null);

        // 1. El mapper funciona bien
        when(modelMapper.map(inDto, Movie.class)).thenReturn(movieEntity);

        // 2. PERO el repositorio falla al guardar (Simulamos error de BD)
        when(movieRepository.save(any(Movie.class))).thenThrow(new RuntimeException("Database error"));

        // 3. Verificamos que el servicio propaga la excepción hacia arriba
        assertThrows(RuntimeException.class, () -> {
            movieService.create(inDto);
        });

        // Verificamos que intentó guardar pero falló
        verify(movieRepository, times(1)).save(movieEntity);
    }

    // ---------------------------------------------------------------------------------
    // TEST PUT (UPDATE)
    // ---------------------------------------------------------------------------------

    @Test // Caso 200 OK
    public void testUpdate_Success() {
        Long id = 1L;
        MovieInDto inDto = new MovieInDto("Matrix Reloaded", "SciFi", 130, 0, 0, LocalDate.now(), true);

        // Película existente en BD antes de actualizar
        Movie existingMovie = new Movie(id, "Matrix", "SciFi", 120, 0, 0, LocalDate.now(), true, null);

        // Película después de que el repositorio la guarde (con los datos nuevos)
        Movie updatedMovie = new Movie(id, "Matrix Reloaded", "SciFi", 130, 0, 0, LocalDate.now(), true, null);

        // Encontramos la película
        when(movieRepository.findById(id)).thenReturn(Optional.of(existingMovie));
        // Simulo el guardado.
        when(movieRepository.save(any(Movie.class))).thenReturn(updatedMovie);

        Movie result = movieService.update(id, inDto);

        assertEquals("Matrix Reloaded", result.getMovieTitle());
        assertEquals(130, result.getDurationMinutes());
        verify(movieRepository, times(1)).findById(id);
        verify(movieRepository, times(1)).save(existingMovie);
    }

    @Test // Caso 404 Not Found
    public void testUpdate_NotFound() {
        Long id = 99L;
        MovieInDto inDto = new MovieInDto("Matrix", "SciFi", 120, 0, 0, LocalDate.now(), true);

        // Simulo que no existe la película
        when(movieRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(MovieNotFoundException.class, () -> {
            movieService.update(id, inDto);
        });

        //Nunca se llamó a save si no se encontró la película
        verify(movieRepository, times(0)).save(any());
    }

    // ---------------------------------------------------------------------------------
    // TEST DELETE
    // ---------------------------------------------------------------------------------
    @Test // Caso 204 No Content
    public void testDelete_Success() {
        Long id = 1L;

        //existe
        when(movieRepository.existsById(id)).thenReturn(true);
        // deleteById devuelve void

        movieService.delete(id);

        verify(movieRepository, times(1)).existsById(id);
        verify(movieRepository, times(1)).deleteById(id);
    }

    @Test // Caso 404 Not Found
    public void testDelete_NotFound() {
        Long id = 99L;

        //NO existe
        when(movieRepository.existsById(id)).thenReturn(false);

        assertThrows(MovieNotFoundException.class, () -> {
            movieService.delete(id);
        });

        //NO se llamó a deleteById
        verify(movieRepository, times(0)).deleteById(id);
    }

}

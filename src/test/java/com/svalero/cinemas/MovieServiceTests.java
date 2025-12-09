package com.svalero.cinemas;

import com.svalero.cinemas.domain.Movie;
import com.svalero.cinemas.domain.dto.MovieOutDto;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
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
                new Movie(1L, "Terminator 1", "Scifi", 101, 41.6598777, -0.8835212, LocalDate.parse("1992-08-11"), true, null),
                new Movie(2L, "Terminator 2", "Scifi", 101, 41.6545777, -0.8839212, LocalDate.parse("1984-08-11"), true, null),
                new Movie(3L, "Terminator 3", "Scifi", 101, 41.6598777, -0.8897212, LocalDate.parse("1997-08-11"), true, null)
        );
        List<MovieOutDto> mockModelMapperOut = List.of(
                new MovieOutDto(1L, "Terminator 1", "Scifi", 101, 41.6598777, -0.8835212, LocalDate.parse("1992-08-11"), true),
                new MovieOutDto(2L, "Terminator 2", "Scifi", 101, 41.6545777, -0.8839212, LocalDate.parse("1984-08-11"), true),
                new MovieOutDto(3L, "Terminator 3", "Scifi", 101, 41.6598777, -0.8897212, LocalDate.parse("1997-08-11"), true)
        );
        //cuando se llame al movieRepository.findAll tu devuelve esta lista
        when(movieRepository.findAll()).thenReturn(mockMovieList);
        //cuando llamen a modelmapper quiero que devuelvas una lista de MovieOutDto
        when(modelMapper.map(mockMovieList, new TypeToken<List<MovieOutDto>>() {
        }.getType())).thenReturn(mockModelMapperOut);

        List<MovieOutDto> actualMovieList = movieService.findAll("", "", null);
        assertEquals(3, actualMovieList.size());
        assertEquals("Terminator 1", actualMovieList.getFirst().getMovieTitle());
        assertEquals("Terminator 3", actualMovieList.getLast().getMovieTitle());
        //Ahora puedo comprobar por donde se ejecuta mi codigo
        //Aqui le estamos diciendo que compruebe en movieRepository se ha llamado 1 vez a findAll
        verify(movieRepository, times(1)).findAll();
        //Ahora voy a comprobar algo que no tiene que pasar, es decir un metodo al que no se tiene que llamar
        //Para eso le digo que no se ha llamado nunca ==> 0, no se ha llamdo nunca a findByGenre en este caso
        verify(movieRepository, times(0)).findByGenre("");

    }

    //Caso de prueba pasando un parametro, en este caso genre
    @Test
    public void testFindByMovieTitlel() {
        //Me voy a imaginar la lista de movies que me va al devolver (
        List<Movie> mockMovieList = List.of(
                new Movie(1L, "Terminator 1", "Scifi", 101, 41.6598777, -0.8835212, LocalDate.parse("1984-08-11"), true, null),
                new Movie(2L, "La Guerra de las Galaxias", "Scifi", 101, 41.6545777, -0.8839212, LocalDate.parse("1977-08-11"), true, null),
                new Movie(3L, "Avatar", "Scifi", 101, 41.6598777, -0.8897212, LocalDate.parse("2010-08-11"), true, null)
        );
        List<MovieOutDto> mockModelMapperOut = List.of(
                new MovieOutDto(1L, "Terminator 1", "Scifi", 101, 41.6598777, -0.8835212, LocalDate.parse("1984-08-11"), true)
        );
        //Cuidado cambia aqui el findAll por findByMovieTitle
        when(movieRepository.findByMovieTitle("Terminator 1")).thenReturn(mockMovieList);
        when(modelMapper.map(mockMovieList, new TypeToken<List<MovieOutDto>>() {
        }.getType())).thenReturn(mockModelMapperOut);
        //Recuerda que el service tiene tres parametros
        List<MovieOutDto> actualMovieList = movieService.findAll("Terminator 1", "", null);
        assertEquals(1, actualMovieList.size());
        assertEquals("Terminator 1", actualMovieList.getFirst().getMovieTitle());
        //Ahora no deberia de ejcutarse el findAll
        verify(movieRepository, times(0)).findAll();
        verify(movieRepository, times(0)).findByGenre("");
        verify(movieRepository, times(1)).findByMovieTitle("Terminator 1");

    }

    //Se puede hacer ahora toda clase de pruebas.. una buena y una mala por endpoint? seguramente..confirmar
    //Hay algunos metodos que tienen mas logica, esos serian los buenos de probar, para probar el resultado
    //y mockeando las partes que hagan falta.

}

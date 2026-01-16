package com.svalero.cinemas;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.cinemas.controller.MovieController;
import com.svalero.cinemas.domain.Movie;
import com.svalero.cinemas.domain.dto.MovieInDto;
import com.svalero.cinemas.domain.dto.MovieOutDto;
import com.svalero.cinemas.exception.MovieNotFoundException;
import com.svalero.cinemas.service.MovieService;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieController.class)
public class MovieControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieService movieService;

    @MockitoBean
    private ModelMapper modelMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetAll() throws Exception {
        List<MovieOutDto> moviesOutDto = List.of(
                new MovieOutDto(1L, "La Guerra de las Galaxias", "Scifi", 101, 41.6598777, -0.8835212, LocalDate.parse("1977-08-11"),true),
                new MovieOutDto(2L, "Terminator 2", "Action", 101, 41.6545777, -0.8839212, LocalDate.parse("1992-08-11"),true),
                new MovieOutDto(3L, "El Señor de los Anillos", "Epic Fantasy", 101, 41.6598777, -0.8897212, LocalDate.parse("2001-08-11"),true)
        );
        when(movieService.findAll("","",null)).thenReturn(moviesOutDto);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/movies")
                        .accept(MediaType.APPLICATION_JSON_VALUE))//me va a pasr un Json
                .andExpect(status().isOk())//espero que sea ok
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();//paso la respuesta a objeto JAVA con estas dos lineas
        List<MovieOutDto> moviesListResponse = objectMapper.readValue(jsonResponse, new TypeReference<>(){});

        assertNotNull(moviesListResponse);//La respuesta no deberia de ser nula
        assertEquals(3, moviesListResponse.size());
        assertEquals("La Guerra de las Galaxias", moviesListResponse.getFirst().getMovieTitle());
    }

    @Test
    public void testGetAllByGenre() throws Exception {
        List<MovieOutDto> moviesOutDto = List.of(
                new MovieOutDto(3L, "El Señor de los Anillos", "Epic Fantasy", 101, 41.6598777, -0.8897212, LocalDate.parse("2001-08-11"),true)
        );
        when(movieService.findAll("","Epic Fantasy",null)).thenReturn(moviesOutDto);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/movies")
                        .queryParam("genre", "Epic Fantasy")
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        List<MovieOutDto> moviesListResponse = objectMapper.readValue(jsonResponse, new TypeReference<>(){});

        assertNotNull(moviesListResponse);
        assertEquals(1, moviesListResponse.size());
        assertEquals("El Señor de los Anillos", moviesListResponse.getFirst().getMovieTitle());

    }

    // TEST GET BY ID
    // Caso 200 OK
    @Test
    public void testGetMovieById() throws Exception {
        Long movieId = 1L;
        MovieOutDto movieOutDto = new MovieOutDto(movieId, "Dune", "SciFi", 155, 41.6598777, -0.8897212, LocalDate.now(), true);

        when(movieService.findById(movieId)).thenReturn(movieOutDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/movies/{id}", movieId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Esperamos 200
                .andExpect(result -> { // Verifico contenido básico
                    String json = result.getResponse().getContentAsString();
                    MovieOutDto response = objectMapper.readValue(json, MovieOutDto.class);
                    assertEquals("Dune", response.getMovieTitle());
                });
    }

    // Caso 404 Not Found
    @Test
    public void testGetMovieByIdNotFound() throws Exception {
        Long movieId = 99L;
        // Mockeo que la capa service lanza la excepción cuando busca ese ID
        when(movieService.findById(movieId)).thenThrow(new MovieNotFoundException("Movie not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/movies/{id}", movieId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Esperamos 404
    }

    // TEST POST (CREATE)
    @Test // Caso 201 Created
    public void testCreateMovie() throws Exception {
        // Objeto que enviamos (Input)
        MovieInDto movieInDto = new MovieInDto();
        movieInDto.setMovieTitle("Avatar");
        movieInDto.setGenre("SciFi");
        movieInDto.setDurationMinutes(160);
        movieInDto.setFilmingLatitude(41.6598777);
        movieInDto.setFilmingLongitude(-0.8897212);
        movieInDto.setReleaseDate(LocalDate.now());
        movieInDto.setCurrentlyShowing(true);

        MovieOutDto movieOutDto = new MovieOutDto(1L, "Avatar", "SciFi", 160, 41.6598777, -0.8897212, LocalDate.now(), true);

        when(movieService.create(any(MovieInDto.class))).thenReturn(movieOutDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movieInDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(result -> {
                    MovieOutDto response = objectMapper.readValue(result.getResponse().getContentAsString(), MovieOutDto.class);
                    assertNotNull(response.getId());
                });
    }

    // Caso 400 Bad Request
    @Test
    public void testCreateMovieBadRequest() throws Exception {
        // Creo una película vacía o inválida para que salten las validaciones @NotBlank
        MovieInDto badMovie = new MovieInDto();

        mockMvc.perform(MockMvcRequestBuilders.post("/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badMovie))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        // Se espera un 400 porque fallará el @NotBlank
    }

    // TEST PUT (UPDATE)
    // Caso 200 OK
    @Test
    public void testUpdateMovie() throws Exception {
        Long movieId = 1L;
        //Un DTO Valido
        MovieInDto movieInDto = new MovieInDto();
        movieInDto.setMovieTitle("Dune Parte 2");
        movieInDto.setGenre("SciFi");
        movieInDto.setDurationMinutes(166);
        movieInDto.setFilmingLatitude(41.6598777);
        movieInDto.setFilmingLongitude(-0.8897212);
        movieInDto.setReleaseDate(LocalDate.now());
        movieInDto.setCurrentlyShowing(true);
        //objeto que devuelve el servicio (Movie)
        Movie updatedMovie = new Movie();
        updatedMovie.setId(movieId);
        updatedMovie.setMovieTitle("Dune Parte 2");
        updatedMovie.setGenre("SciFi");
        movieInDto.setDurationMinutes(166);
        movieInDto.setFilmingLatitude(41.6598777);
        movieInDto.setFilmingLongitude(-0.8897212);
        movieInDto.setReleaseDate(LocalDate.now());
        movieInDto.setCurrentlyShowing(true);
        //MOCK
        when(movieService.update(any(Long.class), any(MovieInDto.class))).thenReturn(updatedMovie);
        //EJECUCION Y VERIFICACION
        mockMvc.perform(MockMvcRequestBuilders.put("/movies/{id}", movieId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movieInDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Esperamos 200 OK
                .andExpect(result -> {
                    //
                    com.svalero.cinemas.domain.Movie response = objectMapper.readValue(result.getResponse().getContentAsString(), com.svalero.cinemas.domain.Movie.class);
                    assertEquals("Dune Parte 2", response.getMovieTitle());
                    assertEquals(movieId, response.getId());
                });
    }

    // Caso 404 Not Found
    @Test
    public void testUpdateMovieNotFound() throws Exception {
        Long movieId = 99L;

        // Para que llegue al 404, el objeto DEBE SER VÁLIDO.
        MovieInDto movieInDto = new MovieInDto();
        movieInDto.setMovieTitle("Bullet Train");
        movieInDto.setGenre("Comedy");
        movieInDto.setDurationMinutes(90);
        movieInDto.setReleaseDate(LocalDate.now());

        // MOCK que al intentar actualizar lance la excepción de NO ENCONTRADO
        doThrow(new MovieNotFoundException("Movie not found")).when(movieService).update(any(Long.class), any(MovieInDto.class));

        mockMvc.perform(MockMvcRequestBuilders.put("/movies/{id}", movieId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movieInDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // Caso 400 Bad Request
    @Test
    public void testUpdateMovieBadRequest() throws Exception {
        Long movieId = 1L;
        // Fallará por @NotBlank en title/genre y @NotNull en releaseDate
        MovieInDto badMovie = new MovieInDto();

        mockMvc.perform(MockMvcRequestBuilders.put("/movies/{id}", movieId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badMovie))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // TEST DELETE
    // Caso 204
    @Test
    public void testDeleteMovie() throws Exception {
        Long movieId = 1L;
        // delete devuelve void
        mockMvc.perform(MockMvcRequestBuilders.delete("/movies/{id}", movieId))
                .andExpect(status().isNoContent()); // Esperamos 204
    }

    // Caso 404 Not Found
    @Test
    public void testDeleteMovieNotFound() throws Exception {
        Long movieId = 99L;
        // Como devuelve void, la sintaxis de Mockito cambia a 'doThrow'
        doThrow(new MovieNotFoundException("Movie not found")).when(movieService).delete(movieId);
        mockMvc.perform(MockMvcRequestBuilders.delete("/movies/{id}", movieId))
                .andExpect(status().isNotFound()); // Esperamos 404
    }

}

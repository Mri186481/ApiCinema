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
    //Como estamos mockeando la capa CONTROLLER aqui es un poco diferente
    //se utiliza @WebMvcTest(MovieController.class) , @MockitoBean y MockMvc
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieService movieService;
    // ATENCION ES OBLIGATORIO CON mockito mokear el modelMapper aunque no se use,
    //sino se pone no funciona, es curioso esto
    @MockitoBean
    private ModelMapper modelMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetAll() throws Exception {
        //Voy a probar la capa Controller por lo que tengo que mockear la llamada a la
        //capa service
        List<MovieOutDto> moviesOutDto = List.of(
                new MovieOutDto(1L, "La Guerra de las Galaxias", "Scifi", 101, 41.6598777, -0.8835212, LocalDate.parse("1977-08-11"),true),
                new MovieOutDto(2L, "Terminator 2", "Action", 101, 41.6545777, -0.8839212, LocalDate.parse("1992-08-11"),true),
                new MovieOutDto(3L, "El Señor de los Anillos", "Epic Fantasy", 101, 41.6598777, -0.8897212, LocalDate.parse("2001-08-11"),true)
        );
        //Cuando la capa service llame a findAll entonces tu le devuelves la lista, con esto ya tengo mockeada la capa service
        when(movieService.findAll("","",null)).thenReturn(moviesOutDto);
        //El objeto mockMvc es un objeto especial que nos permite simular llamadas http, es decir simula un cliente
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
//Curiosamente, aunque tenga mas parametros la llamada, solo hay que poner uno
//                        .queryParam("movieTitle","", "genre", "Epic Fantasy", "durationMinutes",null)
                        .queryParam("genre", "Epic Fantasy")
                        .accept(MediaType.APPLICATION_JSON_VALUE))//me va a pasr un Json
                .andExpect(status().isOk())//espero que sea ok
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();//paso la respuesta a objeto JAVA con estas dos lineas
        List<MovieOutDto> moviesListResponse = objectMapper.readValue(jsonResponse, new TypeReference<>(){});

        assertNotNull(moviesListResponse);//La respuesta no deberia de ser nula
        assertEquals(1, moviesListResponse.size());
        assertEquals("El Señor de los Anillos", moviesListResponse.getFirst().getMovieTitle());

    }

    // ---------------------------------------------------------------------------------
    // TEST GET BY ID
    // ---------------------------------------------------------------------------------

    @Test // Caso 200 OK
    public void testGetMovieById() throws Exception {
        Long movieId = 1L;
        MovieOutDto movieOutDto = new MovieOutDto(movieId, "Dune", "SciFi", 155, 41.6598777, -0.8897212, LocalDate.now(), true);

        // Mockeo que la capa service encuentra la película
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

    @Test // Caso 404 Not Found
    public void testGetMovieByIdNotFound() throws Exception {
        Long movieId = 99L;
        // Mockeo que la capa service lanza la excepción cuando busca ese ID
        when(movieService.findById(movieId)).thenThrow(new MovieNotFoundException("Movie not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/movies/{id}", movieId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Esperamos 404
    }

    // ---------------------------------------------------------------------------------
    // TEST POST (CREATE)
    // ---------------------------------------------------------------------------------

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

        // Objeto que esperamos que devuelva el mock (Output)
        MovieOutDto movieOutDto = new MovieOutDto(1L, "Avatar", "SciFi", 160, 41.6598777, -0.8897212, LocalDate.now(), true);

        // Cuando le llegue CUALQUIER MovieInDto, devuelve el OutDto simulado
        when(movieService.create(any(MovieInDto.class))).thenReturn(movieOutDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/movies")
                        .contentType(MediaType.APPLICATION_JSON) // Importante: enviamos JSON
                        .content(objectMapper.writeValueAsString(movieInDto)) // Convertimos objeto a JSON string
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()) // Esperamos 201
                .andExpect(result -> {
                    MovieOutDto response = objectMapper.readValue(result.getResponse().getContentAsString(), MovieOutDto.class);
                    assertNotNull(response.getId());
                });
    }

    @Test // Caso 400 Bad Request
    public void testCreateMovieBadRequest() throws Exception {
        // Creo una película vacía o inválida para que salten las validaciones @NotBlank
        MovieInDto badMovie = new MovieInDto();

        mockMvc.perform(MockMvcRequestBuilders.post("/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badMovie))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()); // Se espera un 400 porque fallará el @NotBlank
    }

    // ---------------------------------------------------------------------------------
    // TEST PUT (UPDATE)
    // ---------------------------------------------------------------------------------

    @Test // Caso 200 OK
    public void testUpdateMovie() throws Exception {
        Long movieId = 1L;
        // 1. Un DTO Valido
        MovieInDto movieInDto = new MovieInDto();
        movieInDto.setMovieTitle("Dune Parte 2");
        movieInDto.setGenre("SciFi");
        movieInDto.setDurationMinutes(166);
        movieInDto.setFilmingLatitude(41.6598777);
        movieInDto.setFilmingLongitude(-0.8897212);
        movieInDto.setReleaseDate(LocalDate.now());
        movieInDto.setCurrentlyShowing(true);
        // 2. objeto que devuelve el servicio (Movie)
        Movie updatedMovie = new Movie();
        updatedMovie.setId(movieId);
        updatedMovie.setMovieTitle("Dune Parte 2");
        updatedMovie.setGenre("SciFi");
        movieInDto.setDurationMinutes(166);
        movieInDto.setFilmingLatitude(41.6598777);
        movieInDto.setFilmingLongitude(-0.8897212);
        movieInDto.setReleaseDate(LocalDate.now());
        movieInDto.setCurrentlyShowing(true);
        // 3. MOCK
        when(movieService.update(any(Long.class), any(MovieInDto.class))).thenReturn(updatedMovie);
        // 4. EJECUCION Y VERIFICACION
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
    @Test // Caso 404 Not Found
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

    @Test // Caso 400 Bad Request
    public void testUpdateMovieBadRequest() throws Exception {
        Long movieId = 1L;
        // Fallará por @NotBlank en title/genre y @NotNull en releaseDate
        MovieInDto badMovie = new MovieInDto();

        mockMvc.perform(MockMvcRequestBuilders.put("/movies/{id}", movieId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badMovie))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()); // Esperamos 400
    }

    // ---------------------------------------------------------------------------------
    // TEST DELETE
    // ---------------------------------------------------------------------------------

    @Test // Caso 204
    public void testDeleteMovie() throws Exception {
        Long movieId = 1L;
        // delete devuelve void
        mockMvc.perform(MockMvcRequestBuilders.delete("/movies/{id}", movieId))
                .andExpect(status().isNoContent()); // Esperamos 204
    }

    @Test // Caso 404 Not Found
    public void testDeleteMovieNotFound() throws Exception {
        Long movieId = 99L;
        // Como el método devuelve void, la sintaxis de Mockito cambia a 'doThrow'
        doThrow(new MovieNotFoundException("Movie not found")).when(movieService).delete(movieId);
        mockMvc.perform(MockMvcRequestBuilders.delete("/movies/{id}", movieId))
                .andExpect(status().isNotFound()); // Esperamos 404
    }


}

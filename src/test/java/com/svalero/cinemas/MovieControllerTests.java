package com.svalero.cinemas;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.cinemas.controller.MovieController;
import com.svalero.cinemas.domain.dto.MovieOutDto;
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
    //primer caso de prueba testGetALL() es decir sin paramatro o categoria
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
}

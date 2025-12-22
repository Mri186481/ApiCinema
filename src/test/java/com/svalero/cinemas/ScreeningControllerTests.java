package com.svalero.cinemas;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.cinemas.controller.ScreeningController;
import com.svalero.cinemas.domain.dto.ScreeningInDto;
import com.svalero.cinemas.domain.dto.ScreeningOutDto;
import com.svalero.cinemas.exception.ScreeningNotFoundException;
import com.svalero.cinemas.service.ScreeningService;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScreeningController.class)
public class ScreeningControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ScreeningService screeningService;

    @MockitoBean
    private ModelMapper modelMapper;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------------------------------------------------------------------------------
    // TEST GET ALL
    // ---------------------------------------------------------------------------------

    @Test
    public void testGetAllScreenings() throws Exception {
        // Preparamos datos de prueba
        List<ScreeningOutDto> screeningsOutDto = List.of(
                new ScreeningOutDto(1L, LocalDateTime.now().plusDays(1), 9.50, false, 1L, 1L, "Avatar", "Sala 1"),
                new ScreeningOutDto(2L, LocalDateTime.now().plusDays(2), 10.00, true, 2L, 2L, "Dune", "Sala 2")
        );

        // Mockeamos el servicio
        when(screeningService.findAll(any(), any(), any())).thenReturn(screeningsOutDto);

        // Ejecutamos la petición
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/screenings")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Verificamos
        String jsonResponse = result.getResponse().getContentAsString();
        List<ScreeningOutDto> responseList = objectMapper.readValue(jsonResponse, new TypeReference<>(){});

        assertNotNull(responseList);
        assertEquals(2, responseList.size());
        assertEquals("Avatar", responseList.getFirst().getMovieTitle());
    }

    // ---------------------------------------------------------------------------------
    // TEST GET BY ID
    // ---------------------------------------------------------------------------------

    @Test // Caso 200 OK
    public void testGetScreeningById() throws Exception {
        Long screeningId = 1L;
        ScreeningOutDto outDto = new ScreeningOutDto(screeningId, LocalDateTime.now(), 8.50, false, 1L, 1L, "Titanic", "Sala A");

        when(screeningService.findById(screeningId)).thenReturn(outDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/screenings/{id}", screeningId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    ScreeningOutDto response = objectMapper.readValue(json, ScreeningOutDto.class);
                    assertEquals("Titanic", response.getMovieTitle());
                    assertEquals(8.50, response.getTicketPrice());
                });
    }

    @Test // Caso 404 Not Found
    public void testGetScreeningByIdNotFound() throws Exception {
        Long screeningId = 99L;
        when(screeningService.findById(screeningId)).thenThrow(new ScreeningNotFoundException("Screening not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/screenings/{id}", screeningId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------------------------
    // TEST POST (CREATE)
    // ---------------------------------------------------------------------------------

    @Test // Caso 201 Created
    public void testAddScreening() throws Exception {
        // 1. Input válido (cumpliendo @NotNull)
        ScreeningInDto inputDto = new ScreeningInDto();
        inputDto.setScreeningTime(LocalDateTime.now().plusDays(1));
        inputDto.setTicketPrice(12.00);
        inputDto.setMovieId(1L);
        inputDto.setRoomId(1L);
        inputDto.setSubtitled(true);

        // 2. Output esperado
        ScreeningOutDto outputDto = new ScreeningOutDto(1L, inputDto.getScreeningTime(), 12.00, true, 1L, 1L, "Movie", "Room");

        // 3. Mock
        when(screeningService.add(any(ScreeningInDto.class))).thenReturn(outputDto);

        // 4. Ejecución
        mockMvc.perform(MockMvcRequestBuilders.post("/screenings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(result -> {
                    ScreeningOutDto response = objectMapper.readValue(result.getResponse().getContentAsString(), ScreeningOutDto.class);
                    assertNotNull(response.getId());
                });
    }

    @Test // Caso 400 Bad Request
    public void testAddScreeningBadRequest() throws Exception {
        // Envio objeto vacío para provocar fallo de validación
        ScreeningInDto invalidDto = new ScreeningInDto();

        mockMvc.perform(MockMvcRequestBuilders.post("/screenings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------------------------
    // TEST PUT (UPDATE)
    // ---------------------------------------------------------------------------------

    @Test // Caso 200 OK
    public void testModifyScreening() throws Exception {
        Long screeningId = 1L;

        // 1. Input válido
        ScreeningInDto inputDto = new ScreeningInDto();
        inputDto.setScreeningTime(LocalDateTime.now().plusDays(5));
        inputDto.setTicketPrice(15.00);
        inputDto.setMovieId(2L);
        inputDto.setRoomId(2L);

        // 2. Output esperado
        ScreeningOutDto outputDto = new ScreeningOutDto(screeningId, inputDto.getScreeningTime(), 15.00, false, 2L, 2L, "New Movie", "New Room");

        // 3. Mock
        when(screeningService.modify(eq(screeningId), any(ScreeningInDto.class))).thenReturn(outputDto);

        // 4. Ejecución
        mockMvc.perform(MockMvcRequestBuilders.put("/screenings/{id}", screeningId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    ScreeningOutDto response = objectMapper.readValue(result.getResponse().getContentAsString(), ScreeningOutDto.class);
                    assertEquals(15.00, response.getTicketPrice());
                    assertEquals("New Movie", response.getMovieTitle());
                });
    }

    @Test // Caso 404 Not Found
    public void testModifyScreeningNotFound() throws Exception {
        Long screeningId = 99L;

        ScreeningInDto inputDto = new ScreeningInDto();
        inputDto.setScreeningTime(LocalDateTime.now());
        inputDto.setTicketPrice(10.0);
        inputDto.setMovieId(1L);
        inputDto.setRoomId(1L);

        doThrow(new ScreeningNotFoundException("Screening not found"))
                .when(screeningService).modify(eq(screeningId), any(ScreeningInDto.class));

        mockMvc.perform(MockMvcRequestBuilders.put("/screenings/{id}", screeningId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test // Caso 400 Bad Request
    public void testModifyScreeningBadRequest() throws Exception {
        Long screeningId = 1L;
        // Objeto vacío para que salten los @NotNull
        ScreeningInDto invalidDto = new ScreeningInDto();

        mockMvc.perform(MockMvcRequestBuilders.put("/screenings/{id}", screeningId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------------------------
    // TEST DELETE
    // ---------------------------------------------------------------------------------

    @Test // Caso 204 No Content
    public void testDeleteScreening() throws Exception {
        Long screeningId = 1L;
        // Mock implícito (doNothing)

        mockMvc.perform(MockMvcRequestBuilders.delete("/screenings/{id}", screeningId))
                .andExpect(status().isNoContent());
    }

    @Test // Caso 404 Not Found
    public void testDeleteScreeningNotFound() throws Exception {
        Long screeningId = 99L;

        doThrow(new ScreeningNotFoundException("Screening not found"))
                .when(screeningService).delete(screeningId);

        mockMvc.perform(MockMvcRequestBuilders.delete("/screenings/{id}", screeningId))
                .andExpect(status().isNotFound());
    }
}
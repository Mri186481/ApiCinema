package com.svalero.cinemas;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.cinemas.controller.SeatController;
import com.svalero.cinemas.domain.Seat;
import com.svalero.cinemas.domain.dto.SeatInDto;
import com.svalero.cinemas.domain.dto.SeatOutDto;
import com.svalero.cinemas.exception.SeatNotFoundException;
import com.svalero.cinemas.service.SeatService;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SeatController.class)
public class SeatControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SeatService seatService;

    @MockitoBean
    private ModelMapper modelMapper;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------------------------------------------------------------------------------
    // TEST GET ALL
    // ---------------------------------------------------------------------------------

    @Test
    public void testGetAllSeats() throws Exception {
        // Preparamos datos de prueba
        List<SeatOutDto> seatsOutDto = List.of(
                new SeatOutDto(1L, 1, 1, true, "OPERATIONAL", 0.0, LocalDate.now(), 1L, "Room A"),
                new SeatOutDto(2L, 1, 2, false, "BROKEN", 0.0, LocalDate.now(), 1L, "Room A")
        );

        // Mockeamos el servicio
        when(seatService.getAll(null,null,"")).thenReturn(seatsOutDto);

        // Ejecutamos la petición
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/seats")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Verificamos
        String jsonResponse = result.getResponse().getContentAsString();
        List<SeatOutDto> responseList = objectMapper.readValue(jsonResponse, new TypeReference<>(){});

        assertNotNull(responseList);
        assertEquals(2, responseList.size());
        assertEquals("OPERATIONAL", responseList.getFirst().getStatus());
    }

    // ---------------------------------------------------------------------------------
    // TEST GET BY ID
    // ---------------------------------------------------------------------------------

    @Test // Caso 200 OK
    public void testGetSeatById() throws Exception {
        Long seatId = 1L;
        Seat seat = new Seat();
        seat.setId(seatId);
        seat.setSeatRow(5);
        seat.setSeatColumn(10);
        seat.setStatus("OPERATIONAL");
        seat.setPriceSurcharge(BigDecimal.ZERO);

        when(seatService.get(seatId)).thenReturn(seat);

        mockMvc.perform(MockMvcRequestBuilders.get("/seats/{seatId}", seatId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    Seat response = objectMapper.readValue(json, Seat.class);
                    assertEquals(5, response.getSeatRow());
                    assertEquals("OPERATIONAL", response.getStatus());
                });
    }

    @Test // Caso 404 Not Found
    public void testGetSeatByIdNotFound() throws Exception {
        Long seatId = 99L;
        when(seatService.get(seatId)).thenThrow(new SeatNotFoundException("Seat not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/seats/{seatId}", seatId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------------------------
    // TEST POST (ADD SEAT)
    // ---------------------------------------------------------------------------------

    @Test // Caso 201 Created
    public void testAddSeat() throws Exception {
        Long roomId = 10L;

        // 1. Input válido (cumpliendo @Min)
        SeatInDto inputDto = new SeatInDto();
        inputDto.setSeatRow(1);
        inputDto.setSeatColumn(1);
        inputDto.setStatus("OPERATIONAL");
        inputDto.setSeatAccesible(true);
        inputDto.setPriceSurcharge(2.50);

        // 2. Output esperado
        SeatOutDto outputDto = new SeatOutDto(1L, 1, 1, true, "OPERATIONAL", 2.50, null, roomId, "Room A");

        // 3. Mock
        when(seatService.add(eq(roomId), any(SeatInDto.class))).thenReturn(outputDto);

        // 4. Ejecución (OJO A LA URL)
        mockMvc.perform(MockMvcRequestBuilders.post("/rooms/{roomId}/seats", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(result -> {
                    SeatOutDto response = objectMapper.readValue(result.getResponse().getContentAsString(), SeatOutDto.class);
                    assertNotNull(response.getId());
                    assertEquals("OPERATIONAL", response.getStatus());
                });
    }

    @Test // Caso 400 Bad Request
    public void testAddSeatBadRequest() throws Exception {
        Long roomId = 10L;
        // Envio filas y columnas inválidas (<1) para provocar error de validación @Min
        SeatInDto invalidDto = new SeatInDto();
        invalidDto.setSeatRow(0); // Inválido
        invalidDto.setSeatColumn(-5); // Inválido

        mockMvc.perform(MockMvcRequestBuilders.post("/rooms/{roomId}/seats", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------------------------
    // TEST PUT (MODIFY SEAT)
    // ---------------------------------------------------------------------------------

    @Test // Caso 200 OK
    public void testModifySeat() throws Exception {
        Long seatId = 1L;

        // 1. Input válido
        SeatInDto inputDto = new SeatInDto();
        inputDto.setSeatRow(2);
        inputDto.setSeatColumn(2);
        inputDto.setStatus("MAINTENANCE");

        // 2. Output esperado
        SeatOutDto outputDto = new SeatOutDto(seatId, 2, 2, false, "MAINTENANCE", 0.0, LocalDate.now(), 1L, "Room A");

        // 3. Mock
        when(seatService.modify(eq(seatId), any(SeatInDto.class))).thenReturn(outputDto);

        // 4. Ejecución
        mockMvc.perform(MockMvcRequestBuilders.put("/seats/{seatId}", seatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    SeatOutDto response = objectMapper.readValue(result.getResponse().getContentAsString(), SeatOutDto.class);
                    assertEquals("MAINTENANCE", response.getStatus());
                });
    }

    @Test // Caso 404 Not Found
    public void testModifySeatNotFound() throws Exception {
        Long seatId = 99L;

        // IMPORTANTE: Objeto VÁLIDO para pasar el @Valid del controller
        SeatInDto inputDto = new SeatInDto();
        inputDto.setSeatRow(5);
        inputDto.setSeatColumn(5);

        doThrow(new SeatNotFoundException("Seat not found"))
                .when(seatService).modify(eq(seatId), any(SeatInDto.class));

        mockMvc.perform(MockMvcRequestBuilders.put("/seats/{seatId}", seatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test // Caso 400 Bad Request
    public void testModifySeatBadRequest() throws Exception {
        Long seatId = 1L;
        // Objeto inválido (fila 0) para que salte @Min
        SeatInDto invalidDto = new SeatInDto();
        invalidDto.setSeatRow(0);

        mockMvc.perform(MockMvcRequestBuilders.put("/seats/{seatId}", seatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------------------------
    // TEST DELETE
    // ---------------------------------------------------------------------------------

    @Test // Caso 204 No Content
    public void testDeleteSeat() throws Exception {
        Long seatId = 1L;
        // Mock implícito (doNothing)

        mockMvc.perform(MockMvcRequestBuilders.delete("/seats/{seatId}", seatId))
                .andExpect(status().isNoContent());
    }

    @Test // Caso 404 Not Found
    public void testDeleteSeatNotFound() throws Exception {
        Long seatId = 99L;

        doThrow(new SeatNotFoundException("Seat not found"))
                .when(seatService).delete(seatId);

        mockMvc.perform(MockMvcRequestBuilders.delete("/seats/{seatId}", seatId))
                .andExpect(status().isNotFound());
    }
}
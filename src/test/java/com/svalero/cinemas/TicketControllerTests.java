package com.svalero.cinemas;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.cinemas.controller.TicketController;
import com.svalero.cinemas.domain.dto.TicketInDto;
import com.svalero.cinemas.domain.dto.TicketOutDto;
import com.svalero.cinemas.exception.TicketNotFoundException;
import com.svalero.cinemas.service.TicketService;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
public class TicketControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    @MockitoBean
    private ModelMapper modelMapper;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------------------------------------------------------------------------------
    // TEST GET ALL (Con filtros opcionales)
    // ---------------------------------------------------------------------------------

    @Test
    public void testGetAllTickets() throws Exception {
        // Preparamos datos de prueba
        List<TicketOutDto> ticketsOutDto = List.of(
                new TicketOutDto(1L, LocalDateTime.now(), 9.50, "TICKET-001", "Juan", "juan@mail.com", false, false, false, false, "Avatar", 10.0, "Sala 1", LocalDateTime.now(), 1, 1, true, true, true, true,
                        "Normal", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,false),
                new TicketOutDto(2L, LocalDateTime.now(), 8.00, "TICKET-002", "Maria", "maria@mail.com", true, true, false, false, "Dune", 10.0, "Sala 2", LocalDateTime.now(), 2, 2, true, false, false, false,
                        "Normal", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,false)
                        );

        // Mockeamos la llamada con filtros nulos (comportamiento por defecto)
        when(ticketService.getAll(null, null, null)).thenReturn(ticketsOutDto);

        // Ejecutamos la petición
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/tickets")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Verificamos la respuesta
        String jsonResponse = result.getResponse().getContentAsString();
        List<TicketOutDto> responseList = objectMapper.readValue(jsonResponse, new TypeReference<>(){});

        assertNotNull(responseList);
        assertEquals(2, responseList.size());
        assertEquals("TICKET-001", responseList.getFirst().getTicketCode());
    }

    // ---------------------------------------------------------------------------------
    // TEST GET BY ID
    // ---------------------------------------------------------------------------------

    @Test // Caso 200 OK
    public void testGetTicketById() throws Exception {
        Long ticketId = 1L;
        // Tu controller devuelve TicketOutDto
        TicketOutDto outDto = new TicketOutDto();
        outDto.setId(ticketId);
        outDto.setTicketCode("ABC-123");
        outDto.setFinalPricePaid(12.50);
        outDto.setMovieTitle("Matrix");

        when(ticketService.get(ticketId)).thenReturn(outDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/tickets/{ticketId}", ticketId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    TicketOutDto response = objectMapper.readValue(json, TicketOutDto.class);
                    assertEquals("ABC-123", response.getTicketCode());
                    assertEquals("Matrix", response.getMovieTitle());
                });
    }

    @Test // Caso 404 Not Found
    public void testGetTicketByIdNotFound() throws Exception {
        Long ticketId = 99L;
        when(ticketService.get(ticketId)).thenThrow(new TicketNotFoundException("Ticket not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/tickets/{ticketId}", ticketId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------------------------
    // TEST POST (ADD TICKET)
    // ---------------------------------------------------------------------------------

    @Test // Caso 201 Created
    public void testAddTicket() throws Exception {
        // 1. Input válido (Cumpliendo @NotNull en price, code, customerId, screeningId, rateId, seatId)
        TicketInDto inputDto = new TicketInDto();
        inputDto.setTicketCode("NEW-TICKET-001");
        inputDto.setFinalPricePaid(15.00);
        inputDto.setCustomerId(1L);
        inputDto.setScreeningId(2L);
        inputDto.setSeatId(3L);
        inputDto.setRateId(4L);
        inputDto.setScanned(false);
        inputDto.setStatus("ACTIVE");

        // 2. Output esperado
        TicketOutDto outputDto = new TicketOutDto();
        outputDto.setId(1L);
        outputDto.setTicketCode("NEW-TICKET-001");
        outputDto.setFinalPricePaid(15.00);
        outputDto.setMovieTitle("Inception"); // Simulado

        // 3. Mock
        when(ticketService.add(any(TicketInDto.class))).thenReturn(outputDto);

        // 4. Ejecución
        mockMvc.perform(MockMvcRequestBuilders.post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(result -> {
                    TicketOutDto response = objectMapper.readValue(result.getResponse().getContentAsString(), TicketOutDto.class);
                    assertNotNull(response.getId());
                    assertEquals("NEW-TICKET-001", response.getTicketCode());
                });
    }

    @Test // Caso 400 Bad Request
    public void testAddTicketBadRequest() throws Exception {
        // Enviamos objeto vacío para provocar fallo de validación (@NotNull)
        TicketInDto invalidDto = new TicketInDto();

        mockMvc.perform(MockMvcRequestBuilders.post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------------------------
    // TEST PUT (MODIFY TICKET)
    // ---------------------------------------------------------------------------------

    @Test // Caso 200 OK
    public void testModifyTicket() throws Exception {
        Long ticketId = 1L;

        // 1. Input válido
        TicketInDto inputDto = new TicketInDto();
        inputDto.setTicketCode("MODIFIED-CODE");
        inputDto.setFinalPricePaid(20.00);
        inputDto.setCustomerId(1L);
        inputDto.setScreeningId(2L);
        inputDto.setSeatId(3L);
        inputDto.setRateId(4L);

        // 2. Output esperado
        TicketOutDto outputDto = new TicketOutDto();
        outputDto.setId(ticketId);
        outputDto.setTicketCode("MODIFIED-CODE");
        outputDto.setFinalPricePaid(20.00);

        // 3. Mock
        when(ticketService.modify(eq(ticketId), any(TicketInDto.class))).thenReturn(outputDto);

        // 4. Ejecución
        mockMvc.perform(MockMvcRequestBuilders.put("/tickets/{ticketId}", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    TicketOutDto response = objectMapper.readValue(result.getResponse().getContentAsString(), TicketOutDto.class);
                    assertEquals("MODIFIED-CODE", response.getTicketCode());
                    assertEquals(20.00, response.getFinalPricePaid());
                });
    }

    @Test // Caso 404 Not Found
    public void testModifyTicketNotFound() throws Exception {
        Long ticketId = 99L;

        TicketInDto inputDto = new TicketInDto();
        inputDto.setTicketCode("GHOST-TICKET");
        inputDto.setFinalPricePaid(10.00);
        inputDto.setCustomerId(1L);
        inputDto.setScreeningId(1L);
        inputDto.setSeatId(1L);
        inputDto.setRateId(1L);

        doThrow(new TicketNotFoundException("Ticket not found"))
                .when(ticketService).modify(eq(ticketId), any(TicketInDto.class));

        mockMvc.perform(MockMvcRequestBuilders.put("/tickets/{ticketId}", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test // Caso 400 Bad Request
    public void testModifyTicketBadRequest() throws Exception {
        Long ticketId = 1L;
        // Objeto vacío para que salten los @NotNull
        TicketInDto invalidDto = new TicketInDto();

        mockMvc.perform(MockMvcRequestBuilders.put("/tickets/{ticketId}", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------------------------
    // TEST DELETE
    // ---------------------------------------------------------------------------------

    @Test // Caso 204 No Content
    public void testDeleteTicket() throws Exception {
        Long ticketId = 1L;
        // Mock implícito (doNothing)
        mockMvc.perform(MockMvcRequestBuilders.delete("/tickets/{ticketId}", ticketId))
                .andExpect(status().isNoContent());
    }

    @Test // Caso 404 Not Found
    public void testDeleteTicketNotFound() throws Exception {
        Long ticketId = 99L;

        doThrow(new TicketNotFoundException("Ticket not found"))
                .when(ticketService).delete(ticketId);

        mockMvc.perform(MockMvcRequestBuilders.delete("/tickets/{ticketId}", ticketId))
                .andExpect(status().isNotFound());
    }
}

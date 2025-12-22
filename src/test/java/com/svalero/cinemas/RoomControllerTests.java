package com.svalero.cinemas;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.cinemas.controller.RoomController;
import com.svalero.cinemas.domain.Room;
import com.svalero.cinemas.domain.dto.RoomInDto;
import com.svalero.cinemas.domain.dto.RoomOutDto;
import com.svalero.cinemas.exception.RoomNotFoundException;
import com.svalero.cinemas.service.RoomService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomController.class)
public class RoomControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoomService roomService;

    @MockitoBean
    private ModelMapper modelMapper;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------------------------------------------------------------------------------
    // TEST GET ALL (Con parámetros opcionales)
    // ---------------------------------------------------------------------------------

    @Test
    public void testGetAllRooms() throws Exception {
        List<RoomOutDto> roomsOutDto = List.of(
                new RoomOutDto(1L, "Sala 1", LocalDate.now(), 100, true, false, false),
                new RoomOutDto(2L, "Sala 2", LocalDate.now(), 150, true, true, true)
        );

        // Mockeo la llamada con nulos (sin filtros)
        when(roomService.getAll(null, null, null)).thenReturn(roomsOutDto);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/rooms")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        List<RoomOutDto> responseList = objectMapper.readValue(jsonResponse, new TypeReference<>(){});

        assertNotNull(responseList);
        assertEquals(2, responseList.size());
        assertEquals("Sala 1", responseList.getFirst().getRoomName());
    }

    // ---------------------------------------------------------------------------------
    // TEST GET BY ID
    // ---------------------------------------------------------------------------------

    @Test // Caso 200 OK
    public void testGetRoomById() throws Exception {
        Long roomId = 1L;
        Room room = new Room();
        room.setId(roomId);
        room.setRoomName("Sala IMAX");
        room.setCapacity(200);
        room.setOpeningDate(LocalDate.now());

        when(roomService.get(roomId)).thenReturn(room);

        mockMvc.perform(MockMvcRequestBuilders.get("/rooms/{roomId}", roomId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    Room response = objectMapper.readValue(json, Room.class);
                    assertEquals("Sala IMAX", response.getRoomName());
                    assertEquals(200, response.getCapacity());
                });
    }

    @Test // Caso 404 Not Found
    public void testGetRoomByIdNotFound() throws Exception {
        Long roomId = 99L;
        when(roomService.get(roomId)).thenThrow(new RoomNotFoundException("Room not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/rooms/{roomId}", roomId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------------------------
    // TEST POST (ADD ROOM)
    // ---------------------------------------------------------------------------------

    @Test // Caso 201 Created
    public void testAddRoom() throws Exception {
        // 1. Input válido (cumpliendo @NotBlank y @NotNull)
        RoomInDto inputDto = new RoomInDto();
        inputDto.setRoomName("Sala Nueva");
        inputDto.setCapacity(120);
        inputDto.setOpeningDate(LocalDate.now());
        inputDto.setRoom3d(true);

        // 2. Output esperado
        RoomOutDto outputDto = new RoomOutDto(1L, "Sala Nueva", LocalDate.now(), 120, true, false, false);

        // 3. Mock
        when(roomService.add(any(RoomInDto.class))).thenReturn(outputDto);

        // 4. Ejecución
        mockMvc.perform(MockMvcRequestBuilders.post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(result -> {
                    RoomOutDto response = objectMapper.readValue(result.getResponse().getContentAsString(), RoomOutDto.class);
                    assertNotNull(response.getId());
                    assertEquals("Sala Nueva", response.getRoomName());
                });
    }

    @Test // Caso 400 Bad Request
    public void testAddRoomBadRequest() throws Exception {
        // Envio objeto vacío para provocar fallo de validación
        RoomInDto invalidDto = new RoomInDto();

        mockMvc.perform(MockMvcRequestBuilders.post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------------------------
    // TEST PUT (MODIFY ROOM)
    // ---------------------------------------------------------------------------------

    @Test // Caso 200 OK
    public void testModifyRoom() throws Exception {
        Long roomId = 1L;

        // 1. Input válido
        RoomInDto inputDto = new RoomInDto();
        inputDto.setRoomName("Sala Modificada");
        inputDto.setCapacity(150);
        inputDto.setOpeningDate(LocalDate.now());

        // 2. Output esperado
        RoomOutDto outputDto = new RoomOutDto(roomId, "Sala Modificada", LocalDate.now(), 150, false, false, false);

        // 3. Mock
        when(roomService.modify(eq(roomId), any(RoomInDto.class))).thenReturn(outputDto);

        // 4. Ejecución
        mockMvc.perform(MockMvcRequestBuilders.put("/rooms/{roomId}", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    RoomOutDto response = objectMapper.readValue(result.getResponse().getContentAsString(), RoomOutDto.class);
                    assertEquals(150, response.getCapacity());
                    assertEquals("Sala Modificada", response.getRoomName());
                });
    }

    @Test // Caso 404 Not Found
    public void testModifyRoomNotFound() throws Exception {
        Long roomId = 99L;

        RoomInDto inputDto = new RoomInDto();
        inputDto.setRoomName("Sala Fantasma");
        inputDto.setCapacity(100);
        inputDto.setOpeningDate(LocalDate.now());

        doThrow(new RoomNotFoundException("Room not found"))
                .when(roomService).modify(eq(roomId), any(RoomInDto.class));

        mockMvc.perform(MockMvcRequestBuilders.put("/rooms/{roomId}", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test // Caso 400 Bad Request
    public void testModifyRoomBadRequest() throws Exception {
        Long roomId = 1L;
        // Objeto vacío para que salten los @NotBlank/@NotNull
        RoomInDto invalidDto = new RoomInDto();

        mockMvc.perform(MockMvcRequestBuilders.put("/rooms/{roomId}", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------------------------
    // TEST DELETE
    // ---------------------------------------------------------------------------------

    @Test // Caso 204 No Content
    public void testDeleteRoom() throws Exception {
        Long roomId = 1L;
        // Mock implícito (doNothing)

        mockMvc.perform(MockMvcRequestBuilders.delete("/rooms/{roomId}", roomId))
                .andExpect(status().isNoContent());
    }

    @Test // Caso 404 Not Found
    public void testDeleteRoomNotFound() throws Exception {
        Long roomId = 99L;

        doThrow(new RoomNotFoundException("Room not found"))
                .when(roomService).delete(roomId);

        mockMvc.perform(MockMvcRequestBuilders.delete("/rooms/{roomId}", roomId))
                .andExpect(status().isNotFound());
    }
}
package com.svalero.cinemas;

import com.svalero.cinemas.domain.Room;
import com.svalero.cinemas.domain.dto.RoomInDto;
import com.svalero.cinemas.domain.dto.RoomOutDto;
import com.svalero.cinemas.exception.RoomNotFoundException;
import com.svalero.cinemas.repository.RoomRepository;
import com.svalero.cinemas.service.RoomService;
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
public class RoomServiceTests {

    @InjectMocks
    private RoomService roomService;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ModelMapper modelMapper;

    // ---------------------------------------------------------------------------------
    // TEST GET ALL
    // ---------------------------------------------------------------------------------

    @Test
    public void testGetAll_NoFilters() {
        // Mock de datos
        List<Room> mockRooms = List.of(
                new Room(1L, "Sala 1", LocalDate.now(), 100, true, false, false, null, null),
                new Room(2L, "Sala 2", LocalDate.now(), 150, true, true, true, null, null)
        );
        List<RoomOutDto> mockOutDtos = List.of(
                new RoomOutDto(1L, "Sala 1", LocalDate.now(), 100, true, false, false),
                new RoomOutDto(2L, "Sala 2", LocalDate.now(), 150, true, true, true)
        );

        // Cuando se llame sin filtros, debe ir al findAll() general
        when(roomRepository.findAll()).thenReturn(mockRooms);
        when(modelMapper.map(mockRooms, new TypeToken<List<RoomOutDto>>() {}.getType())).thenReturn(mockOutDtos);

        List<RoomOutDto> result = roomService.getAll(null, null, null);

        assertEquals(2, result.size());
        assertEquals("Sala 1", result.getFirst().getRoomName());

        verify(roomRepository, times(1)).findAll();
        //NO llamó a ningún método con filtros
        verify(roomRepository, times(0)).findByRoom3d(anyBoolean());
    }

    @Test
    public void testGetAll_WithFilter3D() {
        // Mock de datos filtrados
        List<Room> mockRooms = List.of(
                new Room(1L, "Sala 3D", LocalDate.now(), 100, true, false, false, null, null)
        );
        List<RoomOutDto> mockOutDtos = List.of(
                new RoomOutDto(1L, "Sala 3D", LocalDate.now(), 100, true, false, false)
        );

        //Se llama con el filtro room3d = true
        when(roomRepository.findByRoom3d(true)).thenReturn(mockRooms);
        when(modelMapper.map(mockRooms, new TypeToken<List<RoomOutDto>>() {}.getType())).thenReturn(mockOutDtos);

        List<RoomOutDto> result = roomService.getAll(true, null, null);

        assertEquals(1, result.size());
        assertEquals("Sala 3D", result.getFirst().getRoomName());

        // Verificamos que se llamó al método específico y NO al general
        verify(roomRepository, times(1)).findByRoom3d(true);
        verify(roomRepository, times(0)).findAll();
    }

    // ---------------------------------------------------------------------------------
    // TEST GET BY ID
    // ---------------------------------------------------------------------------------

    @Test
    public void testGetById_Success() throws RoomNotFoundException {
        Long id = 1L;
        Room mockRoom = new Room(id, "Sala VIP", LocalDate.now(), 50, false, true, false, null, null);

        when(roomRepository.findById(id)).thenReturn(Optional.of(mockRoom));

        Room result = roomService.get(id);

        assertNotNull(result);
        assertEquals("Sala VIP", result.getRoomName());
        verify(roomRepository, times(1)).findById(id);
    }

    @Test
    public void testGetById_NotFound() {
        Long id = 99L;
        when(roomRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class, () -> roomService.get(id));
    }

    // ---------------------------------------------------------------------------------
    // TEST ADD (CREATE)
    // ---------------------------------------------------------------------------------

    @Test
    public void testAdd_Success() {
        RoomInDto inDto = new RoomInDto("Sala Nueva", LocalDate.now(), 200, true, true, true);
        Room roomEntity = new Room(0L, "Sala Nueva", LocalDate.now(), 200, true, true, true, null, null);
        Room savedRoom = new Room(1L, "Sala Nueva", LocalDate.now(), 200, true, true, true, null, null);
        RoomOutDto outDto = new RoomOutDto(1L, "Sala Nueva", LocalDate.now(), 200, true, true, true);

        // 1. Map DTO -> Entity
        when(modelMapper.map(inDto, Room.class)).thenReturn(roomEntity);
        // 2. Save
        when(roomRepository.save(roomEntity)).thenReturn(savedRoom);
        // 3. Map Entity -> OutDto
        when(modelMapper.map(savedRoom, RoomOutDto.class)).thenReturn(outDto);

        RoomOutDto result = roomService.add(inDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Sala Nueva", result.getRoomName());
        verify(roomRepository, times(1)).save(roomEntity);
    }

    @Test
    public void testAdd_Error() {
        RoomInDto inDto = new RoomInDto("Sala Error", LocalDate.now(), 100, false, false, false);
        Room roomEntity = new Room();

        when(modelMapper.map(inDto, Room.class)).thenReturn(roomEntity);
        // Simulo que hay un error en BD
        when(roomRepository.save(any(Room.class))).thenThrow(new RuntimeException("DB Error"));

        assertThrows(RuntimeException.class, () -> roomService.add(inDto));
    }

    // ---------------------------------------------------------------------------------
    // TEST MODIFY (UPDATE)
    // ---------------------------------------------------------------------------------

    @Test
    public void testModify_Success() throws RoomNotFoundException {
        Long id = 1L;
        RoomInDto inDto = new RoomInDto("Sala Modificada", LocalDate.now(), 120, false, false, false);
        Room existingRoom = new Room(id, "Sala Vieja", LocalDate.now(), 100, false, false, false, null, null);
        RoomOutDto outDto = new RoomOutDto(id, "Sala Modificada", LocalDate.now(), 120, false, false, false);

        // 1. Buscar existente
        when(roomRepository.findById(id)).thenReturn(Optional.of(existingRoom));

        // 2. Mapear cambios
        doNothing().when(modelMapper).map(inDto, existingRoom);

        // 3. Guardar
        when(roomRepository.save(existingRoom)).thenReturn(existingRoom);

        // 4. Convertir a salida
        when(modelMapper.map(existingRoom, RoomOutDto.class)).thenReturn(outDto);

        // Ejecución
        RoomOutDto result = roomService.modify(id, inDto);

        // Verificaciones
        assertEquals("Sala Modificada", result.getRoomName());

        verify(roomRepository, times(1)).findById(id);
        verify(roomRepository, times(1)).save(existingRoom);

        verify(modelMapper, times(1)).map(inDto, existingRoom);
    }

    @Test
    public void testModify_NotFound() {
        Long id = 99L;
        RoomInDto inDto = new RoomInDto("Sala", LocalDate.now(), 100, false, false, false);

        when(roomRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> roomService.modify(id, inDto));

        verify(roomRepository, times(0)).save(any());
    }

    // ---------------------------------------------------------------------------------
    // TEST DELETE
    // ---------------------------------------------------------------------------------

    @Test
    public void testDelete_Success() throws RoomNotFoundException {
        Long id = 1L;
        Room room = new Room();
        room.setId(id);

        when(roomRepository.findById(id)).thenReturn(Optional.of(room));

        roomService.delete(id);

        verify(roomRepository, times(1)).findById(id);
        verify(roomRepository, times(1)).delete(room);
    }

    @Test
    public void testDelete_NotFound() {
        Long id = 99L;
        when(roomRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class, () -> roomService.delete(id));

        verify(roomRepository, times(0)).delete(any());
    }
}

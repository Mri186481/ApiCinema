package com.svalero.cinemas;

import com.svalero.cinemas.domain.Room;
import com.svalero.cinemas.domain.Seat;
import com.svalero.cinemas.domain.dto.SeatInDto;
import com.svalero.cinemas.domain.dto.SeatOutDto;
import com.svalero.cinemas.exception.SeatNotFoundException;
import com.svalero.cinemas.repository.RoomRepository;
import com.svalero.cinemas.repository.SeatRepository;
import com.svalero.cinemas.service.SeatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SeatServiceTests {

    @InjectMocks
    private SeatService seatService;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ModelMapper modelMapper;


    // TEST GET ALL
    @Test
    public void testGetAll() {
        List<Seat> mockSeats = List.of(
                new Seat(1L, 1, 1, true, "OPERATIONAL", BigDecimal.ZERO, LocalDate.now(), null, null),
                new Seat(2L, 1, 2, false, "BROKEN", BigDecimal.ZERO, LocalDate.now(), null, null)
        );
        List<SeatOutDto> mockOutDtos = List.of(
                new SeatOutDto(1L, 1, 1, true, "OPERATIONAL", 0.0, LocalDate.now(), 1L, "Room A"),
                new SeatOutDto(2L, 1, 2, false, "BROKEN", 0.0, LocalDate.now(), 1L, "Room A")
        );

        when(seatRepository.findAll()).thenReturn(mockSeats);
        when(modelMapper.map(mockSeats, new TypeToken<List<SeatOutDto>>() {}.getType())).thenReturn(mockOutDtos);

        List<SeatOutDto> result = seatService.getAll(null, null, "");

        assertEquals(2, result.size());
        assertEquals("OPERATIONAL", result.get(0).getStatus());
        verify(seatRepository, times(1)).findAll();
        verify(seatRepository, times(0)).findBySeatRow(anyInt());
    }

    @Test
    public void testGetAll_FilterByRow() {
        Integer row = 5;
        // Datos Mock
        List<Seat> mockSeats = List.of(new Seat(1L, row, 1, true, "OPERATIONAL", BigDecimal.ZERO, LocalDate.now(), null, null));
        List<SeatOutDto> mockOutDtos = List.of(new SeatOutDto(1L, row, 1, true, "OPERATIONAL", 0.0, LocalDate.now(), 1L, "Room A"));

        // Simulo filtro por fila
        when(seatRepository.findBySeatRow(row)).thenReturn(mockSeats);
        when(modelMapper.map(mockSeats, new TypeToken<List<SeatOutDto>>() {}.getType())).thenReturn(mockOutDtos);

        List<SeatOutDto> result = seatService.getAll(row, null, "");

        assertEquals(1, result.size());
        verify(seatRepository, times(1)).findBySeatRow(row);
        verify(seatRepository, times(0)).findAll();
    }

    @Test
    public void testGetAll_FilterByRowAndColumn() {
        Integer row = 5;
        Integer col = 10;

        List<Seat> mockSeats = List.of(new Seat(1L, row, col, true, "OPERATIONAL", BigDecimal.ZERO, LocalDate.now(), null, null));
        List<SeatOutDto> mockOutDtos = List.of(new SeatOutDto(1L, row, col, true, "OPERATIONAL", 0.0, LocalDate.now(), 1L, "Room A"));

        // Simulo filtro por fila y columna
        when(seatRepository.findBySeatRowAndSeatColumn(row, col)).thenReturn(mockSeats);
        when(modelMapper.map(mockSeats, new TypeToken<List<SeatOutDto>>() {}.getType())).thenReturn(mockOutDtos);

        List<SeatOutDto> result = seatService.getAll(row, col, "");

        verify(seatRepository, times(1)).findBySeatRowAndSeatColumn(row, col);
    }

    @Test
    public void testGetAll_FilterByStatus() {
        String status = "BROKEN";

        List<Seat> mockSeats = List.of(new Seat(2L, 1, 2, false, status, BigDecimal.ZERO, LocalDate.now(), null, null));
        List<SeatOutDto> mockOutDtos = List.of(new SeatOutDto(2L, 1, 2, false, status, 0.0, LocalDate.now(), 1L, "Room A"));

        when(seatRepository.findByStatus(status)).thenReturn(mockSeats);
        when(modelMapper.map(mockSeats, new TypeToken<List<SeatOutDto>>() {}.getType())).thenReturn(mockOutDtos);

        // Ejecución con filtro de status
        List<SeatOutDto> result = seatService.getAll(null, null, status);

        verify(seatRepository, times(1)).findByStatus(status);
    }

    // TEST GET BY ID (Devuelve Entity)
    @Test
    public void testGet_Success() throws SeatNotFoundException {
        Long id = 1L;
        Seat mockSeat = new Seat(id, 5, 5, true, "OPERATIONAL", BigDecimal.TEN, null, null, null);

        when(seatRepository.findById(id)).thenReturn(Optional.of(mockSeat));

        Seat result = seatService.get(id);

        assertNotNull(result);
        assertEquals(5, result.getSeatRow());
        verify(seatRepository, times(1)).findById(id);
    }

    @Test
    public void testGet_NotFound() {
        Long id = 99L;
        when(seatRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(SeatNotFoundException.class, () -> seatService.get(id));
    }

    // TEST ADD (CREATE)
    @Test
    public void testAdd_Success() throws SeatNotFoundException {
        Long roomId = 10L;
        SeatInDto inDto = new SeatInDto(1, 1, true, "OPERATIONAL", 0.0, LocalDate.now());

        Room mockRoom = new Room();
        mockRoom.setId(roomId);
        mockRoom.setRoomName("Sala A");

        Seat seatEntity = new Seat();
        Seat savedSeat = new Seat(1L, 1, 1, true, "OPERATIONAL", BigDecimal.ZERO, LocalDate.now(), mockRoom, null);
        SeatOutDto outDto = new SeatOutDto(1L, 1, 1, true, "OPERATIONAL", 0.0, LocalDate.now(), roomId, "Sala A");

        //Buscar Room
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(mockRoom));
        //Map DTO -> Entity
        when(modelMapper.map(inDto, Seat.class)).thenReturn(seatEntity);
        //Save
        when(seatRepository.save(seatEntity)).thenReturn(savedSeat);
        //Map Entity -> OutDto
        when(modelMapper.map(savedSeat, SeatOutDto.class)).thenReturn(outDto);

        SeatOutDto result = seatService.add(roomId, inDto);

        assertNotNull(result);
        assertEquals(roomId, result.getRoomId());

        // Verificaciones
        verify(roomRepository, times(1)).findById(roomId);
        verify(seatRepository, times(1)).save(seatEntity);
        assertEquals(mockRoom, seatEntity.getRoom());
    }

    @Test
    public void testAdd_RoomNotFound() {
        Long roomId = 99L;
        SeatInDto inDto = new SeatInDto();
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());
        assertThrows(SeatNotFoundException.class, () -> seatService.add(roomId, inDto));
        verify(seatRepository, times(0)).save(any());
    }

    // TEST MODIFY
    @Test
    public void testModify_Success() throws SeatNotFoundException {
        Long seatId = 1L;
        SeatInDto inDto = new SeatInDto(2, 2, false, "BROKEN", 0.0, LocalDate.now());
        Seat existingSeat = new Seat(seatId, 2, 2, true, "OPERATIONAL", BigDecimal.ZERO, LocalDate.now(), null, null);
        SeatOutDto outDto = new SeatOutDto(seatId, 2, 2, false, "BROKEN", 0.0, LocalDate.now(), 1L, "Room A");

        //Buscar Existente
        when(seatRepository.findById(seatId)).thenReturn(Optional.of(existingSeat));
        //Mapear void (Importante para verificar que se actualizan los datos)
        doNothing().when(modelMapper).map(inDto, existingSeat);
        //Guardar
        when(seatRepository.save(existingSeat)).thenReturn(existingSeat);
        //Mapear salida
        when(modelMapper.map(existingSeat, SeatOutDto.class)).thenReturn(outDto);

        SeatOutDto result = seatService.modify(seatId, inDto);

        assertEquals("BROKEN", result.getStatus());
        verify(seatRepository, times(1)).findById(seatId);
        verify(seatRepository, times(1)).save(existingSeat);
        verify(modelMapper, times(1)).map(inDto, existingSeat);
    }

    @Test
    public void testModify_NotFound() {
        Long seatId = 99L;
        SeatInDto inDto = new SeatInDto();
        when(seatRepository.findById(seatId)).thenReturn(Optional.empty());
        assertThrows(SeatNotFoundException.class, () -> seatService.modify(seatId, inDto));
        verify(seatRepository, times(0)).save(any());
    }

    // TEST DELETE
    @Test
    public void testDelete_Success() throws SeatNotFoundException {
        Long seatId = 1L;
        Seat seat = new Seat();
        seat.setId(seatId);
        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
        seatService.delete(seatId);
        verify(seatRepository, times(1)).findById(seatId);
        verify(seatRepository, times(1)).delete(seat);
    }

    @Test
    public void testDelete_NotFound() {
        Long seatId = 99L;
        when(seatRepository.findById(seatId)).thenReturn(Optional.empty());
        assertThrows(SeatNotFoundException.class, () -> seatService.delete(seatId));
        verify(seatRepository, times(0)).delete(any());
    }
}

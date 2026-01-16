package com.svalero.cinemas;

import com.svalero.cinemas.domain.Movie;
import com.svalero.cinemas.domain.Room;
import com.svalero.cinemas.domain.Screening;
import com.svalero.cinemas.domain.dto.ScreeningInDto;
import com.svalero.cinemas.domain.dto.ScreeningOutDto;
import com.svalero.cinemas.exception.RoomNotFoundException;
import com.svalero.cinemas.exception.ScreeningNotFoundException;
import com.svalero.cinemas.repository.MovieRepository;
import com.svalero.cinemas.repository.RoomRepository;
import com.svalero.cinemas.repository.ScreeningRepository;
import com.svalero.cinemas.service.ScreeningService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScreeningServiceTests {

    @InjectMocks
    private ScreeningService screeningService;

    @Mock
    private ScreeningRepository screeningRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ModelMapper modelMapper;


    // TEST FIND ALL
    private Screening createMockScreening(Long id, String movieTitle, String roomName) {
        Movie movie = new Movie(); movie.setId(1L); movie.setMovieTitle(movieTitle);
        Room room = new Room(); room.setId(1L); room.setRoomName(roomName);
        return new Screening(id, LocalDateTime.now(), 10.0, false, "es-ES", 5, false, movie, room, null);
    }
    @Test
    public void testFindAll_NoFilters() {
        // Datos de prueba
        Screening screening = createMockScreening(1L, "Dune", "Sala 1");
        List<Screening> mockList = List.of(screening);
        ScreeningOutDto outDto = new ScreeningOutDto(1L, LocalDateTime.now(), 10.0, false, 1L, 1L, "Dune", "Sala 1");
        when(screeningRepository.findAll()).thenReturn(mockList);
        when(modelMapper.map(any(Screening.class), eq(ScreeningOutDto.class))).thenReturn(outDto);

        List<ScreeningOutDto> result = screeningService.findAll(null, null, null);

        // Verificación
        assertEquals(1, result.size());
        assertEquals("Dune", result.get(0).getMovieTitle());
        verify(screeningRepository, times(1)).findAll();
        verify(screeningRepository, times(0)).findByMovieId(any());
    }

    @Test
    public void testFindAll_FilterByMovieId() {
        // Datos de prueba
        Long movieId = 1L;
        Screening screening = createMockScreening(1L, "Avatar", "IMAX");
        List<Screening> mockList = List.of(screening);
        ScreeningOutDto outDto = new ScreeningOutDto(1L, LocalDateTime.now(), 12.0, true, 1L, 1L, "Avatar", "IMAX");

        when(screeningRepository.findByMovieId(movieId)).thenReturn(mockList);
        when(modelMapper.map(any(Screening.class), eq(ScreeningOutDto.class))).thenReturn(outDto);

        List<ScreeningOutDto> result = screeningService.findAll(movieId, null, null);

        // Verificación
        assertEquals(1, result.size());
        verify(screeningRepository, times(1)).findByMovieId(movieId);
        verify(screeningRepository, times(0)).findAll();
    }

    // Test para probar combinación de filtros
    @Test
    public void testFindAll_FilterByMovieAndRoom() {
        Long movieId = 1L;
        Long roomId = 2L;
        Screening screening = createMockScreening(1L, "Titanic", "Sala 2");
        List<Screening> mockList = List.of(screening);
        ScreeningOutDto outDto = new ScreeningOutDto();
        // filtro: movieId + roomId (sin subtitled)
        when(screeningRepository.findByMovieIdAndRoomId(movieId, roomId)).thenReturn(mockList);
        when(modelMapper.map(any(Screening.class), eq(ScreeningOutDto.class))).thenReturn(outDto);

        // Ejecución
        screeningService.findAll(movieId, null, roomId);
        verify(screeningRepository, times(1)).findByMovieIdAndRoomId(movieId, roomId);
    }

    // TEST FIND BY ID
    @Test
    public void testFindById_Success() {
        Long id = 1L;
        Movie movie = new Movie(); movie.setId(1L); movie.setMovieTitle("Avatar");
        Room room = new Room(); room.setId(1L); room.setRoomName("IMAX");

        Screening screening = new Screening(id, LocalDateTime.now(), 12.0, true, "en", 5, true, movie, room, null);
        ScreeningOutDto outDto = new ScreeningOutDto(id, LocalDateTime.now(), 12.0, true, 1L, 1L, "Avatar", "IMAX");

        when(screeningRepository.findById(id)).thenReturn(Optional.of(screening));
        when(modelMapper.map(screening, ScreeningOutDto.class)).thenReturn(outDto);

        ScreeningOutDto result = screeningService.findById(id);

        assertNotNull(result);
        assertEquals("Avatar", result.getMovieTitle());
        verify(screeningRepository, times(1)).findById(id);
    }

    @Test
    public void testFindById_NotFound() {
        Long id = 99L;
        when(screeningRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ScreeningNotFoundException.class, () -> screeningService.findById(id));
    }

    // TEST ADD CREATE
    @Test
    public void testAdd_Success() throws ScreeningNotFoundException, RoomNotFoundException {

        ScreeningInDto inDto = new ScreeningInDto(LocalDateTime.now(), 9.0, false, 1L, 2L);
        // Mocks de dependencias (Movie y Room)
        Movie mockMovie = new Movie(); mockMovie.setId(1L); mockMovie.setMovieTitle("Matrix");
        Room mockRoom = new Room(); mockRoom.setId(2L); mockRoom.setRoomName("Sala 2");
        // Mock screening guardado
        Screening savedScreening = new Screening(10L, inDto.getScreeningTime(), 9.0, false, "es", 5, false, mockMovie, mockRoom, null);
        // Comportamiento de los repositorios
        when(movieRepository.findById(1L)).thenReturn(Optional.of(mockMovie));
        when(roomRepository.findById(2L)).thenReturn(Optional.of(mockRoom));
        when(screeningRepository.save(any(Screening.class))).thenReturn(savedScreening);
        // Ejecución
        ScreeningOutDto result = screeningService.add(inDto);
        // Verificación
        assertNotNull(result.getId());
        assertEquals("Matrix", result.getMovieTitle());
        assertEquals("Sala 2", result.getRoomName());
        verify(movieRepository, times(1)).findById(1L);
        verify(roomRepository, times(1)).findById(2L);
        verify(screeningRepository, times(1)).save(any(Screening.class));
    }

    @Test
    public void testAdd_MovieNotFound() {
        ScreeningInDto inDto = new ScreeningInDto(LocalDateTime.now(), 9.0, false, 99L, 2L);
        when(movieRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ScreeningNotFoundException.class, () -> screeningService.add(inDto));

        verify(roomRepository, times(0)).findById(any()); // No debería llegar a buscar la sala
        verify(screeningRepository, times(0)).save(any());
    }

    @Test
    public void testAdd_RoomNotFound() {
        ScreeningInDto inDto = new ScreeningInDto(LocalDateTime.now(), 9.0, false, 1L, 99L);
        Movie mockMovie = new Movie(); mockMovie.setId(1L);
        when(movieRepository.findById(1L)).thenReturn(Optional.of(mockMovie));
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class, () -> screeningService.add(inDto));
        verify(screeningRepository, times(0)).save(any());
    }

    // TEST UPDATE
    @Test
    public void testModify_Success() throws ScreeningNotFoundException, RoomNotFoundException {
        Long id = 10L;
        ScreeningInDto inDto = new ScreeningInDto(LocalDateTime.now().plusHours(2), 15.0, true, 1L, 2L);
        // Existencias previas
        Screening existingScreening = new Screening();existingScreening.setId(id);
        Movie mockMovie = new Movie(); mockMovie.setId(1L); mockMovie.setMovieTitle("New Movie");
        Room mockRoom = new Room(); mockRoom.setId(2L); mockRoom.setRoomName("New Room");
        // Screening guardado resultante
        Screening savedScreening = new Screening(id, inDto.getScreeningTime(), 15.0, true, "es", 5, false, mockMovie, mockRoom, null);
        // Mocks
        when(screeningRepository.findById(id)).thenReturn(Optional.of(existingScreening)); // Chequeo inicial
        when(movieRepository.findById(1L)).thenReturn(Optional.of(mockMovie));
        when(roomRepository.findById(2L)).thenReturn(Optional.of(mockRoom));
        when(screeningRepository.save(any(Screening.class))).thenReturn(savedScreening);

        ScreeningOutDto result = screeningService.modify(id, inDto);

        assertEquals(15.0, result.getTicketPrice());
        assertEquals("New Movie", result.getMovieTitle());
        verify(screeningRepository, times(1)).save(any(Screening.class));
    }

    @Test
    public void testModify_ScreeningNotFound() {
        Long id = 99L;
        ScreeningInDto inDto = new ScreeningInDto();
        when(screeningRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ScreeningNotFoundException.class, () -> screeningService.modify(id, inDto));
        verify(movieRepository, times(0)).findById(any());
        verify(screeningRepository, times(0)).save(any());
    }

    // TEST DELETE
    @Test
    public void testDelete_Success() {
        Long id = 1L;
        Screening screening = new Screening(); screening.setId(id);
        when(screeningRepository.findById(id)).thenReturn(Optional.of(screening));
        screeningService.delete(id);
        verify(screeningRepository, times(1)).findById(id);
        verify(screeningRepository, times(1)).deleteById(id);
    }

    @Test
    public void testDelete_NotFound() {
        Long id = 99L;
        when(screeningRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ScreeningNotFoundException.class, () -> screeningService.delete(id));
        verify(screeningRepository, times(0)).deleteById(id);
    }
}

package com.svalero.cinemas.service;

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
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final MovieRepository movieRepository;

    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;

    public ScreeningService(ScreeningRepository screeningRepository, MovieRepository movieRepository, RoomRepository roomRepository,ModelMapper modelMapper) {
        this.screeningRepository = screeningRepository;
        this.movieRepository = movieRepository;
        this.roomRepository = roomRepository;
        this.modelMapper = modelMapper;
    }



//    public List<ScreeningOutDto> findAll() {
//        return ((List<Screening>) screeningRepository.findAll())
//                .stream()
//                .map(this::convertToOutDto)
//                .collect(Collectors.toList());
//    }

    public List<ScreeningOutDto> findAll(Long movieId, Boolean subtitled, Long roomId) {
        List<Screening> screeningList;
//
        boolean hasMovieId = movieId != null;
        boolean hasSubtitled = subtitled != null;
        boolean hasRoomId = roomId != null;

        if (hasMovieId && hasSubtitled && hasRoomId) {
            screeningList = screeningRepository.findByMovieIdAndSubtitledAndRoomId(movieId, subtitled, roomId);
        } else if (hasMovieId && hasSubtitled) {
            screeningList = screeningRepository.findByMovieIdAndSubtitled(movieId,subtitled);
        } else if (hasMovieId && hasRoomId) {
            screeningList = screeningRepository.findByMovieIdAndRoomId(movieId,roomId);
        } else if (hasSubtitled && hasRoomId) {
            screeningList = screeningRepository.findBySubtitledAndRoomId(subtitled, roomId);
        } else if (hasMovieId) {
            screeningList = screeningRepository.findByMovieId(movieId);
        } else if (hasSubtitled) {
            screeningList = screeningRepository.findBySubtitled(subtitled);
        } else if (hasRoomId) {
            screeningList = screeningRepository.findByRoomId(roomId);
        } else {
            screeningList = screeningRepository.findAll();
        }

//        return modelMapper.map(screeningList, new TypeToken<List<ScreeningOutDto>>() {}.getType());
        return screeningList.stream()
                .map(this::convertToOutDto)
                .collect(Collectors.toList());
    }

    public ScreeningOutDto findById(Long id) {
        Screening screening = screeningRepository.findById(id)
                .orElseThrow(() -> new ScreeningNotFoundException("Screening with ID " + id + " not found"));
        return convertToOutDto(screening);
    }

    public ScreeningOutDto add(ScreeningInDto screeningInDto) throws ScreeningNotFoundException, RoomNotFoundException{
        // 1. Buscar película
        Movie movie = movieRepository.findById(screeningInDto.getMovieId())
                .orElseThrow(() -> new ScreeningNotFoundException("Movie not found"));

        // 1. Buscar sala
        Room room = roomRepository.findById(screeningInDto.getRoomId())
                .orElseThrow(() -> new RoomNotFoundException("Room not found"));
        // 2. Crear manualmente la entidad Screening
        Screening screening = new Screening();
        screening.setId(null);
        screening.setScreeningTime(screeningInDto.getScreeningTime());
        screening.setTicketPrice(screeningInDto.getTicketPrice());
        screening.setSubtitled(screeningInDto.isSubtitled());
        screening.setMovie(movie);
        screening.setRoom(room);

        // 3. Guardar en BD
        Screening savedScreening = screeningRepository.save(screening);

        // 4. Crear DTO de salida manualmente
        ScreeningOutDto outDto = new ScreeningOutDto();
        outDto.setId(savedScreening.getId());
        outDto.setScreeningTime(savedScreening.getScreeningTime());
        outDto.setTicketPrice(savedScreening.getTicketPrice());
        outDto.setSubtitled(savedScreening.isSubtitled());
        outDto.setMovieId(savedScreening.getMovie().getId());
        outDto.setRoomId(savedScreening.getRoom().getId());
        outDto.setMovieTitle(savedScreening.getMovie().getMovieTitle());
        outDto.setRoomName(savedScreening.getRoom().getRoomName());

        return outDto;
    }

    public ScreeningOutDto modify(Long id, ScreeningInDto screeningInDto) throws ScreeningNotFoundException, RoomNotFoundException{
        screeningRepository.findById(id)
                .orElseThrow(() -> new ScreeningNotFoundException("Screening with ID " + id + " not found"));
        //Esta bien pues hay que buscarlo otra vez a ver si existen en la BD
        // 1. Buscar película
        Movie movie = movieRepository.findById(screeningInDto.getMovieId())
                .orElseThrow(() -> new ScreeningNotFoundException("Movie not found"));

        // 1. Buscar sala
        Room room = roomRepository.findById(screeningInDto.getRoomId())
                .orElseThrow(() -> new RoomNotFoundException("Room not found"));
        // 2. Crear manualmente la entidad Screening
        //La unica diferencia es que aqui SI QUE TENGO EL ID
        Screening screening = new Screening();
        screening.setId(id);//tengo el ID, lo preservo para el update
        screening.setScreeningTime(screeningInDto.getScreeningTime());
        screening.setTicketPrice(screeningInDto.getTicketPrice());
        screening.setSubtitled(screeningInDto.isSubtitled());
        screening.setMovie(movie);
        screening.setRoom(room);

        // 3. Guardar en BD
        Screening savedScreening = screeningRepository.save(screening);

        // 4. Crear DTO de salida manualmente
        ScreeningOutDto outDto = new ScreeningOutDto();
        outDto.setId(savedScreening.getId());
        outDto.setScreeningTime(savedScreening.getScreeningTime());
        outDto.setTicketPrice(savedScreening.getTicketPrice());
        outDto.setSubtitled(savedScreening.isSubtitled());
        outDto.setMovieId(savedScreening.getMovie().getId());
        outDto.setRoomId(savedScreening.getRoom().getId());
        outDto.setMovieTitle(savedScreening.getMovie().getMovieTitle());
        outDto.setRoomName(savedScreening.getRoom().getRoomName());

        return outDto;

    }

//He tenido muchos problemas con el mapeador, lo he hecho a mano tanto modificar
//como para grabar no separa bien los id de la clave principal con los otros dos claves foraneas
// Estos dos metodos dan el mismo resultado

//    public ScreeningOutDto modify(Long id, ScreeningInDto screeningInDto) {
//        screeningRepository.findById(id)
//                .orElseThrow(() -> new ScreeningNotFoundException("Screening with ID " + id + " not found"));
//
//        Screening screening = convertToEntity(screeningInDto);
//
//        screening.setId(id); // preserve ID for update
//        Screening updatedScreening = screeningRepository.save(screening);
//        return convertToOutDto(updatedScreening);
//    }

//Este es el que he utilizado en otros
//public ScreeningOutDto modify(Long screeningId, ScreeningInDto screeningInDto) throws ScreeningNotFoundException {
//    Screening screening = screeningRepository.findById(screeningId)
//            .orElseThrow();
//
//    modelMapper.map(screeningInDto, screening);
//    screeningRepository.save(screening);
//    return modelMapper.map(screening, ScreeningOutDto.class);
//}

// A la hora de modificar da problemas de mapeo

    public void delete(Long id) {
        screeningRepository.findById(id)
                .orElseThrow(() -> new ScreeningNotFoundException("Screening with ID " + id + " not found"));
        screeningRepository.deleteById(id);
    }


    private ScreeningOutDto convertToOutDto(Screening screening) {
        ScreeningOutDto dto = modelMapper.map(screening, ScreeningOutDto.class);
        dto.setMovieTitle(screening.getMovie().getMovieTitle());
        dto.setRoomName(screening.getRoom().getRoomName());
        return dto;
    }
}
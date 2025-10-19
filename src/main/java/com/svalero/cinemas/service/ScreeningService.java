package com.svalero.cinemas.service;

import com.svalero.cinemas.domain.Movie;
import com.svalero.cinemas.domain.Screening;
import com.svalero.cinemas.domain.dto.ScreeningInDto;
import com.svalero.cinemas.domain.dto.ScreeningOutDto;
import com.svalero.cinemas.exception.MovieNotFoundException;
import com.svalero.cinemas.exception.ScreeningNotFoundException;
import com.svalero.cinemas.repository.MovieRepository;
import com.svalero.cinemas.repository.ScreeningRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final MovieRepository movieRepository;
    private final ModelMapper modelMapper;

    public ScreeningService(ScreeningRepository screeningRepository, MovieRepository movieRepository, ModelMapper modelMapper) {
        this.screeningRepository = screeningRepository;
        this.movieRepository = movieRepository;
        this.modelMapper = modelMapper;
    }

    public List<ScreeningOutDto> findAll() {
        return ((List<Screening>) screeningRepository.findAll())
                .stream()
                .map(this::convertToOutDto)
                .collect(Collectors.toList());
    }

    public ScreeningOutDto findById(Long id) {
        Screening screening = screeningRepository.findById(id)
                .orElseThrow(() -> new ScreeningNotFoundException("Screening with ID " + id + " not found"));
        return convertToOutDto(screening);
    }

    public ScreeningOutDto add(ScreeningInDto screeningInDto) throws ScreeningNotFoundException {
        // 1. Buscar película
        Movie movie = movieRepository.findById(screeningInDto.getMovieId())
                .orElseThrow(() -> new ScreeningNotFoundException("Movie not found"));

        // 2. Crear manualmente la entidad Screening
        Screening screening = new Screening();
        screening.setId(null);
        screening.setScreeningTime(screeningInDto.getScreeningTime());
        screening.setTheaterRoom(screeningInDto.getTheaterRoom());
        screening.setTicketPrice(screeningInDto.getTicketPrice());
        screening.setSubtitled(screeningInDto.isSubtitled());
        screening.setMovie(movie);

        // 3. Guardar en BD
        Screening savedScreening = screeningRepository.save(screening);

        // 4. Crear DTO de salida manualmente
        ScreeningOutDto outDto = new ScreeningOutDto();
        outDto.setId(savedScreening.getId());
        outDto.setScreeningTime(savedScreening.getScreeningTime());
        outDto.setTheaterRoom(savedScreening.getTheaterRoom());
        outDto.setTicketPrice(savedScreening.getTicketPrice());
        outDto.setSubtitled(savedScreening.isSubtitled());
        outDto.setMovieTitle(savedScreening.getMovie().getTitle());

        return outDto;
    }


    public ScreeningOutDto modify(Long id, ScreeningInDto screeningInDto) {
        screeningRepository.findById(id)
                .orElseThrow(() -> new ScreeningNotFoundException("Screening with ID " + id + " not found"));

        Screening screening = convertToEntity(screeningInDto);
        screening.setId(id); // preserve ID for update
        Screening updatedScreening = screeningRepository.save(screening);
        return convertToOutDto(updatedScreening);
    }

    public void delete(Long id) {
        screeningRepository.findById(id)
                .orElseThrow(() -> new ScreeningNotFoundException("Screening with ID " + id + " not found"));
        screeningRepository.deleteById(id);
    }

    private Screening convertToEntity(ScreeningInDto dto) {
        Screening screening = modelMapper.map(dto, Screening.class);
        Movie movie = movieRepository.findById(dto.getMovieId())
                .orElseThrow(() -> new MovieNotFoundException("Movie with ID " + dto.getMovieId() + " not found"));
        screening.setMovie(movie);
        return screening;
    }

    private ScreeningOutDto convertToOutDto(Screening screening) {
        ScreeningOutDto dto = modelMapper.map(screening, ScreeningOutDto.class);
        dto.setMovieTitle(screening.getMovie().getTitle());
        return dto;
    }
}

package com.svalero.cinemas.config;

import com.svalero.cinemas.domain.Screening;
import com.svalero.cinemas.domain.Seat;
import com.svalero.cinemas.domain.dto.ScreeningOutDto;
import com.svalero.cinemas.domain.dto.SeatOutDto;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class AppConfig {

@Bean
public ModelMapper modelMapper() {
    ModelMapper modelMapper = new ModelMapper();

    //Configuración para saltar nulos (para PATCH)
    modelMapper.getConfiguration().setSkipNullEnabled(true);
    modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

    //Mapa para Screening
    TypeMap<Screening, ScreeningOutDto> typeMap = modelMapper.createTypeMap(Screening.class, ScreeningOutDto.class);
    typeMap.addMappings(mapper -> {
        mapper.map(src -> src.getMovie().getId(), ScreeningOutDto::setMovieId);
        mapper.map(src -> src.getMovie().getMovieTitle(), ScreeningOutDto::setMovieTitle);
        mapper.map(src -> src.getRoom().getId(), ScreeningOutDto::setRoomId);
        mapper.map(src -> src.getRoom().getRoomName(), ScreeningOutDto::setRoomName);
    });

    //Mapa para Seat
    TypeMap<Seat, SeatOutDto> typeMap2 = modelMapper.createTypeMap(Seat.class, SeatOutDto.class);
    typeMap2.addMappings(mapper -> {
        mapper.map(src -> src.getRoom().getId(), SeatOutDto::setRoomId);
        mapper.map(src -> src.getRoom().getRoomName(), SeatOutDto::setRoomName);
    });

    //Conversor manual de String a LocalDate (vital para arreglar error de fecha)
    modelMapper.addConverter(new AbstractConverter<String, LocalDate>() {
        @Override
        protected LocalDate convert(String source) {
            if (source == null || source.isBlank()) return null;
            return LocalDate.parse(source);
        }
    });

    // Conversor para LocalDateTime (Fecha y Hora)
    modelMapper.addConverter(new AbstractConverter<String, LocalDateTime>() {
        @Override
        protected LocalDateTime convert(String source) {
            if (source == null || source.isBlank()) return null;
            // Espera formato ISO estándar: "2025-12-22T14:30:00"
            return LocalDateTime.parse(source);
        }
    });

    return modelMapper;
}
}

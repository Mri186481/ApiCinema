package com.svalero.cinemas.service;

import com.svalero.cinemas.domain.Room;
import com.svalero.cinemas.domain.Seat;
import com.svalero.cinemas.domain.dto.SeatInDto;
import com.svalero.cinemas.domain.dto.SeatOutDto;
import com.svalero.cinemas.exception.SeatNotFoundException;
import com.svalero.cinemas.repository.RoomRepository;
import com.svalero.cinemas.repository.SeatRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SeatService {

    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private ModelMapper modelMapper;

    // Obtener todos las butacas
    public List<SeatOutDto> getAll() {
        List<Seat> seatList = seatRepository.findAll();
        return modelMapper.map(seatList, new TypeToken<List<SeatOutDto>>() {}.getType());
    }

    // Buscar butaca por id

    public Seat get(long id) throws SeatNotFoundException {
        return seatRepository.findById(id)
                .orElseThrow(SeatNotFoundException::new);
    }

    // Dar de alta una butaca
    public SeatOutDto add(long roomId,SeatInDto seatInDto) throws SeatNotFoundException {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(SeatNotFoundException::new);

        Seat seat = modelMapper.map(seatInDto, Seat.class);
        seat.setRoom(room);
        Seat newSeat = seatRepository.save(seat);

        return modelMapper.map(newSeat, SeatOutDto.class);
    }
    // Modificar una butaca
    public SeatOutDto modify(long seatId, SeatInDto seatInDto) throws SeatNotFoundException {
        Seat seat = seatRepository.findById(seatId).orElseThrow(SeatNotFoundException::new);
        modelMapper.map(seatInDto, seat);
        seatRepository.save(seat);
        return modelMapper.map(seat, SeatOutDto.class);
    }
    // eliminar una butaca

    public void delete(long seatId) throws SeatNotFoundException {
        Seat seat = seatRepository.findById(seatId).orElseThrow(SeatNotFoundException::new);
        seatRepository.delete(seat);
    }




}

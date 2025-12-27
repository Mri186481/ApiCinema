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
import java.util.Map;


@Service
public class SeatService {

    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private ModelMapper modelMapper;

    public List<SeatOutDto> getAll(Integer seatRow, Integer seatColumn, String status) {
        List<Seat> seatList;

        boolean hasSeatRow = seatRow != null;
        boolean hasSeatColumn = seatColumn != null;
        boolean hasStatus = !status.isEmpty();

        if (hasSeatRow && hasSeatColumn && hasStatus) {
            seatList = seatRepository.findBySeatRowAndSeatColumnAndStatus(seatRow, seatColumn, status);
        } else if (hasSeatRow && hasSeatColumn) {
            seatList = seatRepository.findBySeatRowAndSeatColumn(seatRow,seatColumn);
        } else if (hasSeatRow && hasStatus) {
            seatList = seatRepository.findBySeatRowAndStatus(seatRow,status);
        } else if (hasSeatColumn && hasStatus) {
            seatList = seatRepository.findBySeatColumnAndStatus(seatColumn, status);
        } else if (hasSeatRow) {
            seatList = seatRepository.findBySeatRow(seatRow);
        } else if (hasSeatColumn) {
            seatList = seatRepository.findBySeatColumn(seatColumn);
        } else if (hasStatus) {
            seatList = seatRepository.findByStatus(status);
        } else {
            seatList = seatRepository.findAll();
        }

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
    // Modificacion parcial de una butaca
    public SeatOutDto modifyPartial(Long seatId, Map<String, Object> updates) {
        Seat seat = seatRepository.findById(seatId).orElseThrow(SeatNotFoundException::new);
        modelMapper.map(updates, seat);
        seatRepository.save(seat);
        return modelMapper.map(seat, SeatOutDto.class);

    }
    // eliminar una butaca
    public void delete(long seatId) throws SeatNotFoundException {
        Seat seat = seatRepository.findById(seatId).orElseThrow(SeatNotFoundException::new);
        seatRepository.delete(seat);
    }

}

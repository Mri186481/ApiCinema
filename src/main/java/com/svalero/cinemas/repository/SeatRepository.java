package com.svalero.cinemas.repository;

import com.svalero.cinemas.domain.Seat;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends CrudRepository<Seat, Long> {

    // Método para obtener todos los usuarios
    List<Seat> findAll();
    List<Seat> findBySeatRow(Integer seatRow);

    List<Seat> findBySeatColumn(Integer seatColumn);

    List<Seat> findByStatus(String status);

    List<Seat> findBySeatRowAndSeatColumnAndStatus(Integer seatRow, Integer seatColumn, String status);

    List<Seat> findBySeatRowAndSeatColumn(Integer seatRow, Integer seatColumn);

    List<Seat> findBySeatRowAndStatus(Integer seatRow, String status);

    List<Seat> findBySeatColumnAndStatus(Integer seatColumn, String status);

    Optional<Seat> findById(Long id);

    // Método para buscar por fila y columna
    Seat findBySeatRowAndSeatColumn(int seatRow, int seatColumn);

    List<Seat> findBySeatAccesible(boolean seatAccesible);

}
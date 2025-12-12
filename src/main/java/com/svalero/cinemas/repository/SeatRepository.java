package com.svalero.cinemas.repository;




import com.svalero.cinemas.domain.Seat;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends CrudRepository<Seat, Long> {

    // Método para obtener todos los usuarios
    List<Seat> findAll();

    Optional<Seat> findById(Long id);

    // Método para buscar por fila y columna
    Seat findBySeatRowAndSeatColumn(int seatRow, int seatColumn);

    List<Seat> findBySeatAccesible(boolean seatAccesible);


}


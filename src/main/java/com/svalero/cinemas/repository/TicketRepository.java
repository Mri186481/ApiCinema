package com.svalero.cinemas.repository;

import com.svalero.cinemas.domain.Ticket;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends CrudRepository<Ticket, Long> {

    // Método para obtener todos los usuarios
    List<Ticket> findAll();

    Optional<Ticket> findById(Long id);

    List<Ticket> findByCustomerId(Long customerId);

    List<Ticket> findByScreeningId(Long screeningId);

    List<Ticket> findBySeatId(Long seatId);

    List<Ticket> findByCustomerIdAndScreeningIdAndSeatId( Long customerId, Long screeningId, Long seatId);

    List<Ticket> findByCustomerIdAndScreeningId(Long customerId, Long screeningId);

    List<Ticket> findByCustomerIdAndSeatId(Long customerId, Long seatId);

    List<Ticket> findByScreeningIdAndSeatId(Long screeningId, Long seatId);

}
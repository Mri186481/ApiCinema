package com.svalero.cinemas.service;

import com.svalero.cinemas.domain.*;
import com.svalero.cinemas.domain.dto.*;
import com.svalero.cinemas.exception.*;
import com.svalero.cinemas.repository.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class TicketService {
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private SeatRepository seatRepository;
    @Autowired
    private ScreeningRepository screeningRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private RateRepository rateRepository;
    @Autowired
    private ModelMapper modelMapper;

    // Obtener todos los tickets con filtrado de customer, screening and seat
    public List<TicketOutDto> getAll(Long customerId,Long screeningId, Long seatId) {
        List<Ticket> ticketList;

        boolean hasCustomerId = customerId != null;
        boolean hasScreeningId = screeningId != null;
        boolean hasSeatId = seatId != null;

        if (hasCustomerId && hasScreeningId && hasSeatId) {
            ticketList = ticketRepository.findByCustomerIdAndScreeningIdAndSeatId(customerId, screeningId, seatId);
        } else if (hasCustomerId && hasScreeningId) {
            ticketList = ticketRepository.findByCustomerIdAndScreeningId(customerId,screeningId);
        } else if (hasCustomerId && hasSeatId) {
            ticketList = ticketRepository.findByCustomerIdAndSeatId(customerId,seatId);
        } else if (hasScreeningId && hasSeatId) {
            ticketList = ticketRepository.findByScreeningIdAndSeatId(screeningId, seatId);
        } else if (hasCustomerId) {
            ticketList = ticketRepository.findByCustomerId(customerId);
        } else if (hasScreeningId) {
            ticketList = ticketRepository.findByScreeningId(screeningId);
        } else if (hasSeatId) {
            ticketList = ticketRepository.findBySeatId(seatId);
        } else {
            ticketList = ticketRepository.findAll();
        }

        return (ticketList
                .stream()
                .map(this::convertToOutDto)
                .collect(Collectors.toList()));
    }

    // Buscar ticket por id
    public TicketOutDto get(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException("Ticket with ID " + id + " not found"));
        return convertToOutDto(ticket);
    }
    // Añadir Ticket
    public TicketOutDto add(TicketInDto ticketInDto) throws ScreeningNotFoundException, CustomerNotFoundException, RateNotFoundException, SeatNotFoundException {
        // 1A. Buscar screening
        Screening screening = screeningRepository.findById(ticketInDto.getScreeningId())
                .orElseThrow(() -> new ScreeningNotFoundException("Screening not found"));

        // 1B. Buscar Customer
        Customer customer = customerRepository.findById(ticketInDto.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        // 1C. Buscar Rate
        Rate rate = rateRepository.findById(ticketInDto.getRateId())
                .orElseThrow(() -> new RateNotFoundException("Rate not found"));
        // 1D. Buscar Seat
        Seat seat = seatRepository.findById(ticketInDto.getSeatId())
                .orElseThrow(() -> new CustomerNotFoundException("Seat not found"));
        // 2. Crear manualmente la entidad ticket
        Ticket ticket = new Ticket();
        ticket.setId(null);
        ticket.setSaleDate(LocalDateTime.now());
        ticket.setFinalPricePaid(ticketInDto.getFinalPricePaid());
        ticket.setScanned(ticketInDto.isScanned());
        ticket.setTicketCode(ticketInDto.getTicketCode());
        ticket.setStatus(ticketInDto.getStatus());
        ticket.setCustomer(customer);
        ticket.setRate(rate);
        ticket.setScreening(screening);
        ticket.setSeat(seat);

        // 3. Guardar en BD
        Ticket savedTicket = ticketRepository.save(ticket);

        // 4. Crear DTO de salida manualmente
        return convertToOutDto(savedTicket);
    }
    //Modificar ticket
    public TicketOutDto modify(Long id, TicketInDto ticketInDto) throws TicketNotFoundException, ScreeningNotFoundException, CustomerNotFoundException, RateNotFoundException, SeatNotFoundException {
        ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException("Ticket with ID " + id + " not found"));
        // 1A. Buscar screening
        Screening screening = screeningRepository.findById(ticketInDto.getScreeningId())
                .orElseThrow(() -> new ScreeningNotFoundException("Screening not found"));

        // 1B. Buscar Customer
        Customer customer = customerRepository.findById(ticketInDto.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        // 1C. Buscar Rate
        Rate rate = rateRepository.findById(ticketInDto.getRateId())
                .orElseThrow(() -> new RateNotFoundException("Rate not found"));
        // 1C. Buscar Seat
        Seat seat = seatRepository.findById(ticketInDto.getSeatId())
                .orElseThrow(() -> new CustomerNotFoundException("Seat not found"));
        // 2. Crear manualmente la entidad ticket
        Ticket ticket = new Ticket();
        ticket.setId(id);//LO PRESERVO PARA EL UPDATE
        ticket.setSaleDate(LocalDateTime.now());
        ticket.setFinalPricePaid(ticketInDto.getFinalPricePaid());
        ticket.setScanned(ticketInDto.isScanned());
        ticket.setTicketCode(ticketInDto.getTicketCode());
        ticket.setStatus(ticketInDto.getStatus());
        ticket.setCustomer(customer);
        ticket.setRate(rate);
        ticket.setScreening(screening);
        ticket.setSeat(seat);

        // 3. Guardar en BD
        Ticket savedTicket = ticketRepository.save(ticket);

        // 4. Crear DTO de salida manualmente
        return convertToOutDto(savedTicket);

    }

    // Modificacion parcial(hecha con modelMapper)
    //Al final he realizado una manual y otra con modelMapper
    public TicketOutDto modifyPartial(Long ticketId, Map<String, Object> updates) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(TicketNotFoundException::new);
        // --- BLOQUE DE AYUDA MANUAL PARA RELACIONES CON MODELLMAPER ---
        // 1A.Screening
        if (updates.containsKey("screeningId")) {
            Long newScreeningId = ((Number) updates.get("screeningId")).longValue();
            Screening screening = screeningRepository.findById(newScreeningId)
                    .orElseThrow(() -> new ScreeningNotFoundException("Screening not found"));
            ticket.setScreening(screening);
            updates.remove("screeningId");
        }
        // 1B.Customer
        if (updates.containsKey("customerId")) {
            // Obtenemos el ID del mapa (asegurando que sea Long)
            Long newCustomerId = ((Number) updates.get("customerId")).longValue();
            // Buscamos la película real
            Customer customer = customerRepository.findById(newCustomerId)
                    .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
            // Se la asignamos al screening
            ticket.setCustomer(customer);
            // Borramos la clave del mapa para que ModelMapper no intente tocarla
            updates.remove("customerId");
        }
        // 1C.Rate
        if (updates.containsKey("rateId")) {
            Long newRateId = ((Number) updates.get("rateId")).longValue();
            Rate rate = rateRepository.findById(newRateId)
                    .orElseThrow(() -> new RateNotFoundException("Rate not found"));
            ticket.setRate(rate);
            updates.remove("rateId");
        }
        // 1D.Seat
        if (updates.containsKey("seatId")) {
            Long newSeatId = ((Number) updates.get("seatId")).longValue();
            Seat seat = seatRepository.findById(newSeatId)
                    .orElseThrow(() -> new SeatNotFoundException("Seat not found"));
            ticket.setSeat(seat);
            updates.remove("seatId");
        }

        // 2. Crea automaticamnete con modelMapper ticket
        modelMapper.map(updates, ticket);

        // 3. Guardar en BD
        Ticket savedTicket = ticketRepository.save(ticket);

        // 4. Creo DTO de salida manualmente
        return convertToOutDto(savedTicket);

    }

    // eliminar un ticket

    public void delete(long ticketId) throws TicketNotFoundException {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(TicketNotFoundException::new);
        ticketRepository.delete(ticket);
    }

    private TicketOutDto convertToOutDto(Ticket ticket) {
        TicketOutDto outDto = new TicketOutDto();
        outDto.setId(ticket.getId());
        outDto.setSaleDate(ticket.getSaleDate());
        outDto.setFinalPricePaid(ticket.getFinalPricePaid());
        outDto.setTicketCode(ticket.getTicketCode());
        outDto.setName(ticket.getCustomer().getName());
        outDto.setMail(ticket.getCustomer().getMail());
        outDto.setYoung(ticket.getCustomer().isYoung());
        outDto.setYoung(ticket.getCustomer().isStudent());
        outDto.setYoung(ticket.getCustomer().isSenior());
        outDto.setYoung(ticket.getCustomer().isMember());
        outDto.setMovieTitle(ticket.getScreening().getMovie().getMovieTitle());
        outDto.setTicketPrice(ticket.getScreening().getTicketPrice());
        outDto.setRoomName(ticket.getScreening().getRoom().getRoomName());
        outDto.setScreeningTime(ticket.getScreening().getScreeningTime());
        outDto.setSeatRow(ticket.getSeat().getSeatRow());
        outDto.setSeatcolumn(ticket.getSeat().getSeatColumn());
        outDto.setSeatAccesible(ticket.getSeat().isSeatAccesible());
        outDto.setRoom3d(ticket.getScreening().getRoom().isRoom3d());
        outDto.setRoomAtmos(ticket.getScreening().getRoom().isRoomAtmos());
        outDto.setRoomLaser(ticket.getScreening().getRoom().isRoomLaser());
        outDto.setNameDayRate(ticket.getRate().getNameDayRate());
        outDto.setYoungDiscount(ticket.getRate().getYoungDiscount());
        outDto.setStudentDiscount(ticket.getRate().getStudentDiscount());
        outDto.setSeniorDiscount(ticket.getRate().getSeniorDiscount());
        outDto.setPromoDayDiscount(ticket.getRate().getPromoDayDiscount());
        outDto.setMemberDiscount(ticket.getRate().getMemberDiscount());
        outDto.setRoom3dPlus(ticket.getRate().getRoom3dPlus());
        outDto.setRoomAtmosPlus(ticket.getRate().getRoomAtmosPlus());
        outDto.setRoomLaserPlus(ticket.getRate().getRoomLaserPlus());
        outDto.setPromoDay(ticket.getRate().isPromoDay());
        return outDto;
    }

}

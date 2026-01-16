package com.svalero.cinemas;

import com.svalero.cinemas.domain.*;
import com.svalero.cinemas.domain.dto.TicketInDto;
import com.svalero.cinemas.domain.dto.TicketOutDto;
import com.svalero.cinemas.exception.*;
import com.svalero.cinemas.repository.*;
import com.svalero.cinemas.service.TicketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTests {

    @InjectMocks
    private TicketService ticketService;

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private ScreeningRepository screeningRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private RateRepository rateRepository;

    @Mock
    private ModelMapper modelMapper;

    private Ticket createMockTicket(Long id) {
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setFinalPricePaid(10.0);
        ticket.setTicketCode("CODE-" + id);
        ticket.setSaleDate(LocalDateTime.now());

        // Customer
        Customer customer = new Customer();
        customer.setName("Juan");
        customer.setMail("juan@mail.com");
        ticket.setCustomer(customer);

        // Rate
        Rate rate = new Rate();
        rate.setNameDayRate("Normal");
        rate.setRateDate(LocalDate.parse("1992-11-11"));
        ticket.setRate(rate);

        // Seat
        Seat seat = new Seat();
        seat.setSeatRow(5);
        seat.setSeatColumn(10);
        ticket.setSeat(seat);

        // Screening & Movie & Room
        Screening screening = new Screening();
        Movie movie = new Movie(); movie.setMovieTitle("Matrix");
        Room room = new Room(); room.setRoomName("Sala 1");

        screening.setMovie(movie);
        screening.setRoom(room);
        screening.setTicketPrice(12.0);
        screening.setScreeningTime(LocalDateTime.now());

        ticket.setScreening(screening);

        return ticket;
    }

    // TEST GET ALL
    @Test
    public void testGetAll_NoFilters() {
        List<Ticket> mockList = List.of(createMockTicket(1L));

        when(ticketRepository.findAll()).thenReturn(mockList);

        List<TicketOutDto> result = ticketService.getAll(null, null, null);

        assertEquals(1, result.size());
        assertEquals("CODE-1", result.get(0).getTicketCode());
        assertEquals("Matrix", result.get(0).getMovieTitle()); // Verifica mapeo profundo
        verify(ticketRepository, times(1)).findAll();
    }

    @Test
    public void testGetAll_FilterByCustomer() {
        Long customerId = 1L;
        List<Ticket> mockList = List.of(createMockTicket(1L));

        when(ticketRepository.findByCustomerId(customerId)).thenReturn(mockList);

        List<TicketOutDto> result = ticketService.getAll(customerId, null, null);

        assertEquals(1, result.size());
        verify(ticketRepository, times(1)).findByCustomerId(customerId);
        verify(ticketRepository, times(0)).findAll();
    }

    // TEST GET BY ID
    @Test
    public void testGet_Success() {
        Long id = 1L;
        Ticket ticket = createMockTicket(id);

        when(ticketRepository.findById(id)).thenReturn(Optional.of(ticket));

        TicketOutDto result = ticketService.get(id);

        assertNotNull(result);
        assertEquals("CODE-1", result.getTicketCode());
        verify(ticketRepository, times(1)).findById(id);
    }

    @Test
    public void testGet_NotFound() {
        Long id = 99L;
        when(ticketRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class, () -> ticketService.get(id));
    }

    // TEST ADD
    @Test
    public void testAdd_Success() throws ScreeningNotFoundException, CustomerNotFoundException, RateNotFoundException,SeatNotFoundException {
        // Input
        TicketInDto inDto = new TicketInDto();
        inDto.setFinalPricePaid(15.0);
        inDto.setTicketCode("NEW-CODE");
        inDto.setCustomerId(1L);
        inDto.setScreeningId(2L);
        inDto.setSeatId(3L);
        inDto.setRateId(4L);

        //Mocks de dependencias
        Customer mockCustomer = new Customer(); mockCustomer.setId(1L);
        Screening mockScreening = new Screening(); mockScreening.setId(2L);
        Seat mockSeat = new Seat(); mockSeat.setId(3L);
        Rate mockRate = new Rate(); mockRate.setId(4L);

        // Ticket devuelto por save() (Debe tener estructura completa para el convertToOutDto)
        Ticket savedTicket = createMockTicket(10L);
        savedTicket.setTicketCode("NEW-CODE");
        savedTicket.setCustomer(mockCustomer);
        savedTicket.setScreening(createMockTicket(10L).getScreening());
        savedTicket.setSeat(mockSeat);
        savedTicket.setRate(mockRate);

        // Configuración de mocks
        when(screeningRepository.findById(2L)).thenReturn(Optional.of(mockScreening));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(mockCustomer));
        when(seatRepository.findById(3L)).thenReturn(Optional.of(mockSeat));
        when(rateRepository.findById(4L)).thenReturn(Optional.of(mockRate));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        TicketOutDto result = ticketService.add(inDto);

        assertNotNull(result);
        assertEquals("NEW-CODE", result.getTicketCode());
        verify(screeningRepository).findById(2L);
        verify(customerRepository).findById(1L);
        verify(seatRepository).findById(3L);
        verify(rateRepository).findById(4L);
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    public void testAdd_CustomerNotFound() {
        TicketInDto inDto = new TicketInDto();
        inDto.setScreeningId(2L);
        inDto.setCustomerId(99L);

        when(screeningRepository.findById(2L)).thenReturn(Optional.of(new Screening()));
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> ticketService.add(inDto));

        verify(seatRepository, times(0)).findById(any());
        verify(ticketRepository, times(0)).save(any());
    }

    // TEST MODIFY (UPDATE)
    @Test
    public void testModify_Success() throws Exception {
        Long id = 1L;
        TicketInDto inDto = new TicketInDto();
        inDto.setTicketCode("UPDATED-CODE");
        inDto.setCustomerId(1L);
        inDto.setScreeningId(2L);
        inDto.setSeatId(3L);
        inDto.setRateId(4L);

        // Ticket existente
        Ticket existingTicket = new Ticket(); existingTicket.setId(id);
        // Mocks
        when(ticketRepository.findById(id)).thenReturn(Optional.of(existingTicket));
        when(screeningRepository.findById(2L)).thenReturn(Optional.of(new Screening()));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(new Customer()));
        when(seatRepository.findById(3L)).thenReturn(Optional.of(new Seat()));
        when(rateRepository.findById(4L)).thenReturn(Optional.of(new Rate()));
        // Ticket guardado
        Ticket savedTicket = createMockTicket(id);
        savedTicket.setTicketCode("UPDATED-CODE");

        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        TicketOutDto result = ticketService.modify(id, inDto);

        assertEquals("UPDATED-CODE", result.getTicketCode());
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    public void testModify_TicketNotFound() {
        Long id = 99L;
        TicketInDto inDto = new TicketInDto();
        when(ticketRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(TicketNotFoundException.class, () -> ticketService.modify(id, inDto));
        verify(screeningRepository, times(0)).findById(any());
        verify(ticketRepository, times(0)).save(any());
    }

    // TEST DELETE
    @Test
    public void testDelete_Success() throws TicketNotFoundException {
        Long id = 1L;
        Ticket ticket = new Ticket(); ticket.setId(id);
        when(ticketRepository.findById(id)).thenReturn(Optional.of(ticket));
        ticketService.delete(id);
        verify(ticketRepository).findById(id);
        verify(ticketRepository).delete(ticket);
    }

    @Test
    public void testDelete_NotFound() {
        Long id = 99L;
        when(ticketRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(TicketNotFoundException.class, () -> ticketService.delete(id));
        verify(ticketRepository, times(0)).delete(any());
    }
}

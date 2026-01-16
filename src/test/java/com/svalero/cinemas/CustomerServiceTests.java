package com.svalero.cinemas;

import com.svalero.cinemas.domain.Customer;
import com.svalero.cinemas.domain.dto.CustomerInDto;
import com.svalero.cinemas.domain.dto.CustomerOutDto;
import com.svalero.cinemas.exception.CustomerNotFoundException;
import com.svalero.cinemas.repository.CustomerRepository;
import com.svalero.cinemas.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTests {

    @InjectMocks
    private CustomerService customerService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ModelMapper modelMapper;

    // TEST GET ALL (Lógica de filtros)
    @Test
    public void testGetAll_NoFilters() {
        //Datos Mock
        List<Customer> mockCustomers = List.of(
                new Customer(1L, "Juan", "Perez", "Calle A", 0.0, 0.0, LocalDate.now(), "juan@mail.com", false, false, false, false, false, null),
                new Customer(2L, "Maria", "Lopez", "Calle B", 0.0, 0.0, LocalDate.now(), "maria@mail.com", true, false, false, false, false, null)
        );
        List<CustomerOutDto> mockOutDtos = List.of(
                new CustomerOutDto(1L, "Juan", "Perez", "Calle A", 0.0, 0.0, LocalDate.now(), "juan@mail.com", false, false, false, false, false),
                new CustomerOutDto(2L, "Maria", "Lopez", "Calle B", 0.0, 0.0, LocalDate.now(), "maria@mail.com", true, false, false, false, false)
        );

        // Cuando los filtros son cadenas vacías (""), debe ir al findAll() general
        when(customerRepository.findAll()).thenReturn(mockCustomers);
        when(modelMapper.map(mockCustomers, new TypeToken<List<CustomerOutDto>>() {}.getType())).thenReturn(mockOutDtos);

        List<CustomerOutDto> result = customerService.getAll("", "", "");

        assertEquals(2, result.size());
        assertEquals("Juan", result.get(0).getName());

        verify(customerRepository, times(1)).findAll();
        //NO llamó a filtros específicos
        verify(customerRepository, times(0)).findByName(anyString());
    }

    @Test
    public void testGetAll_FilterByName() {
        // Mock solo por nombre
        List<Customer> mockCustomers = List.of(
                new Customer(1L, "Juan", "Perez", "Calle A", 0.0, 0.0, LocalDate.now(), "juan@mail.com", false, false, false, false, false, null)
        );
        List<CustomerOutDto> mockOutDtos = List.of(
                new CustomerOutDto(1L, "Juan", "Perez", "Calle A", 0.0, 0.0, LocalDate.now(), "juan@mail.com", false, false, false, false, false)
        );

        // Filtro por nombre="Juan", resto vacíos
        when(customerRepository.findByName("Juan")).thenReturn(mockCustomers);
        when(modelMapper.map(mockCustomers, new TypeToken<List<CustomerOutDto>>() {}.getType())).thenReturn(mockOutDtos);

        List<CustomerOutDto> result = customerService.getAll("Juan", "", "");

        assertEquals(1, result.size());
        verify(customerRepository, times(1)).findByName("Juan");
        verify(customerRepository, times(0)).findAll();
    }

    // TEST GET BY ID
    @Test
    public void testGet_Success() throws CustomerNotFoundException {
        Long id = 1L;
        Customer mockCustomer = new Customer(id, "Ana", "Ruiz", "Calle C", 0.0, 0.0, LocalDate.now(), "ana@mail.com", false, false, false, false, false, null);

        when(customerRepository.findById(id)).thenReturn(Optional.of(mockCustomer));

        Customer result = customerService.get(id);

        assertNotNull(result);
        assertEquals("Ana", result.getName());
        verify(customerRepository, times(1)).findById(id);
    }

    @Test
    public void testGet_NotFound() {
        Long id = 99L;
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.get(id));
    }

    // TEST GET ALL AD
    @Test
    public void testGetAllAd() {
        boolean admitsAd = true;
        List<Customer> mockCustomers = List.of(new Customer());
        List<CustomerOutDto> mockOutDtos = List.of(new CustomerOutDto());

        when(customerRepository.findAllUsersByAdmitsAdvertising(admitsAd)).thenReturn(mockCustomers);
        when(modelMapper.map(mockCustomers, new TypeToken<List<CustomerOutDto>>() {}.getType())).thenReturn(mockOutDtos);

        List<CustomerOutDto> result = customerService.getAllAd(admitsAd);

        assertEquals(1, result.size());
        verify(customerRepository, times(1)).findAllUsersByAdmitsAdvertising(admitsAd);
    }

    // TEST ADD (CREATE)
    @Test
    public void testAdd_Success() {
        CustomerInDto inDto = new CustomerInDto("Luis", "Gomez", "Avda 1", LocalDate.now(), "luis@mail.com", 0.0, 0.0, true, false, false, false, false);
        Customer customerEntity = new Customer();
        customerEntity.setName("Luis");

        Customer savedCustomer = new Customer();
        savedCustomer.setId(1L);
        savedCustomer.setName("Luis");

        CustomerOutDto outDto = new CustomerOutDto();
        outDto.setId(1L);
        outDto.setName("Luis");

        //Map DTO -> Entity
        when(modelMapper.map(inDto, Customer.class)).thenReturn(customerEntity);
        //Save
        when(customerRepository.save(customerEntity)).thenReturn(savedCustomer);
        //Map Entity -> OutDto
        when(modelMapper.map(savedCustomer, CustomerOutDto.class)).thenReturn(outDto);

        CustomerOutDto result = customerService.add(inDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Luis", result.getName());
        verify(customerRepository, times(1)).save(customerEntity);
    }

    // TEST MODIFY (UPDATE)
    @Test
    public void testModify_Success() throws CustomerNotFoundException {
        Long id = 1L;
        CustomerInDto inDto = new CustomerInDto("Luis Modificado", "Gomez", "Avda 2", LocalDate.now(), "luis@mail.com", 0.0, 0.0, true, false, false, false, false);

        Customer existingCustomer = new Customer();
        existingCustomer.setId(id);
        existingCustomer.setName("Luis Antiguo");

        CustomerOutDto outDto = new CustomerOutDto();
        outDto.setId(id);
        outDto.setName("Luis Modificado");

        //Buscar existente
        when(customerRepository.findById(id)).thenReturn(Optional.of(existingCustomer));
        // Map void (Volcar datos del DTO a la entidad existente)
        doNothing().when(modelMapper).map(inDto, existingCustomer);
        //Save
        when(customerRepository.save(existingCustomer)).thenReturn(existingCustomer);
        // Map salida
        when(modelMapper.map(existingCustomer, CustomerOutDto.class)).thenReturn(outDto);
        CustomerOutDto result = customerService.modify(id, inDto);

        assertEquals("Luis Modificado", result.getName());
        verify(customerRepository, times(1)).findById(id);
        verify(customerRepository, times(1)).save(existingCustomer);
        verify(modelMapper, times(1)).map(inDto, existingCustomer);
    }

    @Test
    public void testModify_NotFound() {
        Long id = 99L;
        CustomerInDto inDto = new CustomerInDto();

        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.modify(id, inDto));

        verify(customerRepository, times(0)).save(any());
    }

    // TEST DELETE
    @Test
    public void testDelete_Success() throws CustomerNotFoundException {
        Long id = 1L;
        Customer customer = new Customer();
        customer.setId(id);

        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        customerService.delete(id);

        verify(customerRepository, times(1)).findById(id);
        verify(customerRepository, times(1)).delete(customer);
    }

    @Test
    public void testDelete_NotFound() {
        Long id = 99L;
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> customerService.delete(id));

        verify(customerRepository, times(0)).delete(any());
    }
}
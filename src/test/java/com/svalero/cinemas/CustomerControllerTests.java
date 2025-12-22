package com.svalero.cinemas;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.cinemas.controller.CustomerController;
import com.svalero.cinemas.domain.Customer;
import com.svalero.cinemas.domain.dto.CustomerInDto;
import com.svalero.cinemas.domain.dto.CustomerOutDto;
import com.svalero.cinemas.exception.CustomerNotFoundException;
import com.svalero.cinemas.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
public class CustomerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private ModelMapper modelMapper;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------------------------------------------------------------------------------
    // TEST GET ALL
    // ---------------------------------------------------------------------------------

    @Test
    public void testGetAllCustomers() throws Exception {
        List<CustomerOutDto> customersOutDto = List.of(
                new CustomerOutDto(1L, "Juan", "Perez", "Calle A", 0.0, 0.0, LocalDate.of(1990, 1, 1), "juan@mail.com", true, false, false, false, true),
                new CustomerOutDto(2L, "Maria", "Lopez", "Calle B", 0.0, 0.0, LocalDate.of(1995, 5, 5), "maria@mail.com", false, true, true, false, false)
        );

        // Mockeo la llamada con filtros vacíos (valores por defecto del controller)
        when(customerService.getAll(anyString(), anyString(), anyString())).thenReturn(customersOutDto);


        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/customers")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        List<CustomerOutDto> responseList = objectMapper.readValue(jsonResponse, new TypeReference<>(){});

        assertNotNull(responseList);
        assertEquals(2, responseList.size());
        assertEquals("Juan", responseList.getFirst().getName());
    }

    // ---------------------------------------------------------------------------------
    // TEST GET BY ID
    // ---------------------------------------------------------------------------------

    @Test // Caso 200 OK
    public void testGetUserById() throws Exception {
        Long customerId = 1L;
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setName("Carlos");
        customer.setSurname("Ruiz");
        customer.setMail("carlos@mail.com");
        customer.setAddress("Calle C");
        customer.setBirthDate(LocalDate.now());

        when(customerService.get(customerId)).thenReturn(customer);

        mockMvc.perform(MockMvcRequestBuilders.get("/customers/{customerId}", customerId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    Customer response = objectMapper.readValue(json, Customer.class);
                    assertEquals("Carlos", response.getName());
                    assertEquals("carlos@mail.com", response.getMail());
                });
    }

    @Test // Caso 404 Not Found
    public void testGetUserByIdNotFound() throws Exception {
        Long customerId = 99L;
        when(customerService.get(customerId)).thenThrow(new CustomerNotFoundException("Customer not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/customers/{customerId}", customerId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------------------------
    // TEST GET BY ADVERTISING (Endpoint Extra)
    // ---------------------------------------------------------------------------------

    @Test
    public void testGetUserByAd() throws Exception {
        boolean admitsAd = true;
        List<CustomerOutDto> customers = List.of(
                new CustomerOutDto(1L, "Juan", "Perez", "Calle A", 0.0, 0.0, LocalDate.now(), "juan@mail.com", true, false, false, false, true)
        );

        when(customerService.getAllAd(admitsAd)).thenReturn(customers);

        mockMvc.perform(MockMvcRequestBuilders.get("/customers/admitsAdvertising/{admitsAdvertising}", admitsAd)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    List<CustomerOutDto> response = objectMapper.readValue(json, new TypeReference<>(){});
                    assertEquals(1, response.size());
                    assertEquals(true, response.getFirst().isAdmitsAdvertising());
                });
    }

    // ---------------------------------------------------------------------------------
    // TEST POST (ADD USER)
    // ---------------------------------------------------------------------------------

    @Test // Caso 201 Created
    public void testAddUser() throws Exception {
        // 1. Input válido (Cumpliendo @NotBlank y @NotNull)
        CustomerInDto inputDto = new CustomerInDto();
        inputDto.setName("Laura");
        inputDto.setSurname("Gomez");
        inputDto.setAddress("Avenida Principal");
        inputDto.setMail("laura@test.com");
        inputDto.setBirthDate(LocalDate.of(2000, 1, 1));
        inputDto.setStudent(true);

        // 2. Output esperado
        CustomerOutDto outputDto = new CustomerOutDto(1L, "Laura", "Gomez", "Avenida Principal", 0.0, 0.0, LocalDate.of(2000, 1, 1), "laura@test.com", false, false, true, false, false);

        // 3. Mock
        when(customerService.add(any(CustomerInDto.class))).thenReturn(outputDto);

        // 4. Ejecución
        mockMvc.perform(MockMvcRequestBuilders.post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(result -> {
                    CustomerOutDto response = objectMapper.readValue(result.getResponse().getContentAsString(), CustomerOutDto.class);
                    assertNotNull(response.getId());
                    assertEquals("Laura", response.getName());
                });
    }

    @Test // Caso 400 Bad Request
    public void testAddUserBadRequest() throws Exception {
        // Envio objeto vacío para provocar fallo de validación
        CustomerInDto invalidDto = new CustomerInDto();

        mockMvc.perform(MockMvcRequestBuilders.post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------------------------
    // TEST PUT (MODIFY USER)
    // ---------------------------------------------------------------------------------

    @Test // Caso 200 OK
    public void testModifyUser() throws Exception {
        Long customerId = 1L;

        // 1. Input válido
        CustomerInDto inputDto = new CustomerInDto();
        inputDto.setName("Laura Editada");
        inputDto.setSurname("Gomez");
        inputDto.setAddress("Nueva Dirección");
        inputDto.setMail("laura@test.com");
        inputDto.setBirthDate(LocalDate.of(2000, 1, 1));

        // 2. Output esperado
        CustomerOutDto outputDto = new CustomerOutDto(customerId, "Laura Editada", "Gomez", "Nueva Dirección", 0.0, 0.0, LocalDate.of(2000, 1, 1), "laura@test.com", false, false, false, false, false);

        // 3. Mock
        when(customerService.modify(eq(customerId), any(CustomerInDto.class))).thenReturn(outputDto);

        // 4. Ejecución
        mockMvc.perform(MockMvcRequestBuilders.put("/customers/{customerId}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    CustomerOutDto response = objectMapper.readValue(result.getResponse().getContentAsString(), CustomerOutDto.class);
                    assertEquals("Laura Editada", response.getName());
                    assertEquals("Nueva Dirección", response.getAddress());
                });
    }

    @Test // Caso 404 Not Found
    public void testModifyUserNotFound() throws Exception {
        Long customerId = 99L;

        CustomerInDto inputDto = new CustomerInDto();
        inputDto.setName("Fantasma");
        inputDto.setSurname("Casper");
        inputDto.setAddress("Calle 0");
        inputDto.setMail("ghost@mail.com");
        inputDto.setBirthDate(LocalDate.now());

        doThrow(new CustomerNotFoundException("Customer not found"))
                .when(customerService).modify(eq(customerId), any(CustomerInDto.class));

        mockMvc.perform(MockMvcRequestBuilders.put("/customers/{customerId}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test // Caso 400 Bad Request
    public void testModifyUserBadRequest() throws Exception {
        Long customerId = 1L;
        // Objeto vacío para que salten los @NotBlank/@NotNull
        CustomerInDto invalidDto = new CustomerInDto();

        mockMvc.perform(MockMvcRequestBuilders.put("/customers/{customerId}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------------------------
    // TEST DELETE
    // ---------------------------------------------------------------------------------

    @Test // Caso 204 No Content
    public void testDeleteUser() throws Exception {
        Long customerId = 1L;
        // Mock implícito (doNothing)

        mockMvc.perform(MockMvcRequestBuilders.delete("/customers/{customerId}", customerId))
                .andExpect(status().isNoContent());
    }

    @Test // Caso 404 Not Found
    public void testDeleteUserNotFound() throws Exception {
        Long customerId = 99L;

        doThrow(new CustomerNotFoundException("Customer not found"))
                .when(customerService).delete(customerId);

        mockMvc.perform(MockMvcRequestBuilders.delete("/customers/{customerId}", customerId))
                .andExpect(status().isNotFound());
    }
}

package com.svalero.cinemas;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.svalero.cinemas.controller.RateController;
import com.svalero.cinemas.domain.Rate;
import com.svalero.cinemas.domain.dto.RateInDto;
import com.svalero.cinemas.domain.dto.RateOutDto;
import com.svalero.cinemas.exception.RateNotFoundException;
import com.svalero.cinemas.service.RateService;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RateController.class)
public class RateControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RateService rateService;

    @MockitoBean
    private ModelMapper modelMapper;

    @Autowired
    private ObjectMapper objectMapper;


    // TEST GET ALL
    @Test
    public void testGetAllRates() throws Exception {
        List<RateOutDto> ratesOutDto = List.of(
                new RateOutDto(1L, LocalDate.now(), "Día del Espectador", BigDecimal.valueOf(5.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(1.0), BigDecimal.valueOf(1.0), BigDecimal.valueOf(1.0), true),
                new RateOutDto(2L, LocalDate.now().plusDays(1), "Fin de Semana", BigDecimal.valueOf(9.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(0.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(2.0), false)
        );

        when(rateService.getAll(any(), any(), any())).thenReturn(ratesOutDto);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/rates")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        List<RateOutDto> responseList = objectMapper.readValue(jsonResponse, new TypeReference<>(){});

        assertNotNull(responseList);
        assertEquals(2, responseList.size());
        assertEquals("Día del Espectador", responseList.getFirst().getNameDayRate());
    }


    // TEST GET BY ID
    // Caso 200 OK
    @Test
    public void testGetRateById() throws Exception {
        Long rateId = 1L;
        Rate rate = new Rate();
        rate.setId(rateId);
        rate.setRateDate(LocalDate.now());
        rate.setNameDayRate("Estreno");
        rate.setYoungDiscount(BigDecimal.valueOf(1.50));

        when(rateService.get(rateId)).thenReturn(rate);

        mockMvc.perform(MockMvcRequestBuilders.get("/rates/{id}", rateId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    Rate response = objectMapper.readValue(json, Rate.class);
                    assertEquals("Estreno", response.getNameDayRate());
                    assertEquals(rateId, response.getId());
                });
    }

    // Caso 404 Not Found
    @Test
    public void testGetRateByIdNotFound() throws Exception {
        Long rateId = 99L;
        when(rateService.get(rateId)).thenThrow(new RateNotFoundException("Rate not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/rates/{id}", rateId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    // TEST POST
    // Caso 201 Created
    @Test
    public void testAddRate() throws Exception {
        //Input válido (cumpliendo @NotNull en rateDate)
        RateInDto inputDto = new RateInDto();
        inputDto.setRateDate(LocalDate.now());
        inputDto.setNameDayRate("Fiesta del Cine");
        inputDto.setPromoDay(true);
        //Output esperado
        RateOutDto outputDto = new RateOutDto();
        outputDto.setId(1L);
        outputDto.setRateDate(inputDto.getRateDate());
        outputDto.setNameDayRate("Fiesta del Cine");
        //Mock
        when(rateService.add(any(RateInDto.class))).thenReturn(outputDto);
        //Ejecución
        mockMvc.perform(MockMvcRequestBuilders.post("/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(result -> {
                    RateOutDto response = objectMapper.readValue(result.getResponse().getContentAsString(), RateOutDto.class);
                    assertNotNull(response.getId());
                    assertEquals("Fiesta del Cine", response.getNameDayRate());
                });
    }

    // Caso 400 Bad Request
    @Test
    public void testAddRateBadRequest() throws Exception {
        // Enviamos objeto inválido: rateDate es null (fallará @NotNull)
        RateInDto invalidDto = new RateInDto();
        invalidDto.setNameDayRate("Sin Fecha");

        mockMvc.perform(MockMvcRequestBuilders.post("/rates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // TEST PUT
    // Caso 200 OK
    @Test
    public void testModifyRate() throws Exception {
        Long rateId = 1L;
        //Input válido
        RateInDto inputDto = new RateInDto();
        inputDto.setRateDate(LocalDate.now().plusDays(5));
        inputDto.setNameDayRate("Modificado");
        //Output esperado
        RateOutDto outputDto = new RateOutDto();
        outputDto.setId(rateId);
        outputDto.setRateDate(inputDto.getRateDate());
        outputDto.setNameDayRate("Modificado");
        //Mock
        when(rateService.modify(eq(rateId), any(RateInDto.class))).thenReturn(outputDto);
        //Ejecución
        mockMvc.perform(MockMvcRequestBuilders.put("/rates/{id}", rateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    RateOutDto response = objectMapper.readValue(result.getResponse().getContentAsString(), RateOutDto.class);
                    assertEquals("Modificado", response.getNameDayRate());
                });
    }

    // Caso 404 Not Found
    @Test
    public void testModifyRateNotFound() throws Exception {
        Long rateId = 99L;

        // IMPORTANTE: Objeto VÁLIDO para pasar el @Valid del controller
        RateInDto inputDto = new RateInDto();
        inputDto.setRateDate(LocalDate.now());
        inputDto.setNameDayRate("Fantasma");

        doThrow(new RateNotFoundException("Rate not found"))
                .when(rateService).modify(eq(rateId), any(RateInDto.class));

        mockMvc.perform(MockMvcRequestBuilders.put("/rates/{id}", rateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // Caso 400 Bad Request
    @Test
    public void testModifyRateBadRequest() throws Exception {
        Long rateId = 1L;
        // Objeto inválido (sin rateDate)
        RateInDto invalidDto = new RateInDto();

        mockMvc.perform(MockMvcRequestBuilders.put("/rates/{id}", rateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // TEST DELETE
    // Caso 204 No Content
    @Test
    public void testDeleteRate() throws Exception {
        Long rateId = 1L;
        // Mock implícito (doNothing)
        mockMvc.perform(MockMvcRequestBuilders.delete("/rates/{id}", rateId))
                .andExpect(status().isNoContent());
    }

    // Caso 404 Not Found
    @Test
    public void testDeleteRateNotFound() throws Exception {
        Long rateId = 99L;
        doThrow(new RateNotFoundException("Rate not found"))
                .when(rateService).delete(rateId);
        mockMvc.perform(MockMvcRequestBuilders.delete("/rates/{id}", rateId))
                .andExpect(status().isNotFound());
    }
}

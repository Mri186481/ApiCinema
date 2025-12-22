package com.svalero.cinemas;

import com.svalero.cinemas.domain.Rate;
import com.svalero.cinemas.domain.dto.RateInDto;
import com.svalero.cinemas.domain.dto.RateOutDto;
import com.svalero.cinemas.exception.RateNotFoundException;
import com.svalero.cinemas.repository.RateRepository;
import com.svalero.cinemas.service.RateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RateServiceTests {

    @InjectMocks
    private RateService rateService;

    @Mock
    private RateRepository rateRepository;

    @Mock
    private ModelMapper modelMapper;

    // ---------------------------------------------------------------------------------
    // TEST GET ALL
    // ---------------------------------------------------------------------------------

    @Test
    public void testGetAll_NoFilters() {
        // Datos Mock
        List<Rate> mockRates = List.of(
                new Rate(1L, LocalDate.now(), "Dia del Espectador", BigDecimal.valueOf(5.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(1.0), BigDecimal.valueOf(1.0), BigDecimal.valueOf(1.0), true, null),
                new Rate(2L, LocalDate.now().plusDays(1), "Normal", BigDecimal.valueOf(2.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(0.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(2.0), false, null)
        );
        List<RateOutDto> mockOutDtos = List.of(
                new RateOutDto(1L, LocalDate.now(), "Dia del Espectador", BigDecimal.valueOf(5.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(1.0), BigDecimal.valueOf(1.0), BigDecimal.valueOf(1.0), true),
                new RateOutDto(2L, LocalDate.now().plusDays(1), "Normal", BigDecimal.valueOf(2.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(0.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(2.0), false)
        );

        // Cuando nameDayRate es "" y los otros null, va al findAll() general
        when(rateRepository.findAll()).thenReturn(mockRates);
        when(modelMapper.map(mockRates, new TypeToken<List<RateOutDto>>() {}.getType())).thenReturn(mockOutDtos);

        // Paso "" en el String para evitar NullPointerException en el service (.isEmpty)
        List<RateOutDto> result = rateService.getAll(null, "", null);

        assertEquals(2, result.size());
        assertEquals("Dia del Espectador", result.get(0).getNameDayRate());

        verify(rateRepository, times(1)).findAll();
        verify(rateRepository, times(0)).findByRateDate(any());
    }

    @Test
    public void testGetAll_FilterByName() {
        // Mock solo por nombre
        List<Rate> mockRates = List.of(
                new Rate(1L, LocalDate.now(), "Promo", BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, true, null)
        );
        List<RateOutDto> mockOutDtos = List.of(
                new RateOutDto(1L, LocalDate.now(), "Promo", BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, true)
        );

        // Simulo filtro por nombre="Promo"
        when(rateRepository.findByNameDayRate("Promo")).thenReturn(mockRates);
        when(modelMapper.map(mockRates, new TypeToken<List<RateOutDto>>() {}.getType())).thenReturn(mockOutDtos);

        List<RateOutDto> result = rateService.getAll(null, "Promo", null);

        assertEquals(1, result.size());
        verify(rateRepository, times(1)).findByNameDayRate("Promo");
        verify(rateRepository, times(0)).findAll();
    }

    // ---------------------------------------------------------------------------------
    // TEST GET BY ID
    // ---------------------------------------------------------------------------------

    @Test
    public void testGet_Success() throws RateNotFoundException {
        Long id = 1L;
        Rate mockRate = new Rate();
        mockRate.setId(id);
        mockRate.setNameDayRate("Fin de Semana");
        mockRate.setRateDate(LocalDate.now());

        when(rateRepository.findById(id)).thenReturn(Optional.of(mockRate));

        Rate result = rateService.get(id);

        assertNotNull(result);
        assertEquals("Fin de Semana", result.getNameDayRate());
        verify(rateRepository, times(1)).findById(id);
    }

    @Test
    public void testGet_NotFound() {
        Long id = 99L;
        when(rateRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RateNotFoundException.class, () -> rateService.get(id));
    }

    // ---------------------------------------------------------------------------------
    // TEST ADD (CREATE)
    // ---------------------------------------------------------------------------------

    @Test
    public void testAdd_Success() {
        RateInDto inDto = new RateInDto();
        inDto.setRateDate(LocalDate.now());
        inDto.setNameDayRate("Estreno");
        inDto.setPromoDay(false);

        Rate rateEntity = new Rate();
        rateEntity.setNameDayRate("Estreno");

        Rate savedRate = new Rate();
        savedRate.setId(1L);
        savedRate.setNameDayRate("Estreno");

        RateOutDto outDto = new RateOutDto();
        outDto.setId(1L);
        outDto.setNameDayRate("Estreno");

        // 1. Map DTO -> Entity
        when(modelMapper.map(inDto, Rate.class)).thenReturn(rateEntity);
        // 2. Save
        when(rateRepository.save(rateEntity)).thenReturn(savedRate);
        // 3. Map Entity -> OutDto
        when(modelMapper.map(savedRate, RateOutDto.class)).thenReturn(outDto);

        RateOutDto result = rateService.add(inDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Estreno", result.getNameDayRate());
        verify(rateRepository, times(1)).save(rateEntity);
    }

    // ---------------------------------------------------------------------------------
    // TEST MODIFY (UPDATE)
    // ---------------------------------------------------------------------------------

    @Test
    public void testModify_Success() throws RateNotFoundException {
        Long id = 1L;
        RateInDto inDto = new RateInDto();
        inDto.setNameDayRate("Modificado");

        Rate existingRate = new Rate();
        existingRate.setId(id);
        existingRate.setNameDayRate("Original");

        RateOutDto outDto = new RateOutDto();
        outDto.setId(id);
        outDto.setNameDayRate("Modificado");

        // 1. Buscar existente
        when(rateRepository.findById(id)).thenReturn(Optional.of(existingRate));

        // 2. Map void (Volcar datos del DTO a la entidad existente)
        doNothing().when(modelMapper).map(inDto, existingRate);

        // 3. Save
        when(rateRepository.save(existingRate)).thenReturn(existingRate);

        // 4. Map salida
        when(modelMapper.map(existingRate, RateOutDto.class)).thenReturn(outDto);

        RateOutDto result = rateService.modify(id, inDto);

        assertEquals("Modificado", result.getNameDayRate());
        verify(rateRepository, times(1)).findById(id);
        verify(rateRepository, times(1)).save(existingRate);
        verify(modelMapper, times(1)).map(inDto, existingRate);
    }

    @Test
    public void testModify_NotFound() {
        Long id = 99L;
        RateInDto inDto = new RateInDto();

        when(rateRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RateNotFoundException.class, () -> rateService.modify(id, inDto));

        verify(rateRepository, times(0)).save(any());
    }

    // ---------------------------------------------------------------------------------
    // TEST DELETE
    // ---------------------------------------------------------------------------------

    @Test
    public void testDelete_Success() throws RateNotFoundException {
        Long id = 1L;
        Rate rate = new Rate();
        rate.setId(id);

        when(rateRepository.findById(id)).thenReturn(Optional.of(rate));

        rateService.delete(id);

        verify(rateRepository, times(1)).findById(id);
        verify(rateRepository, times(1)).delete(rate);
    }

    @Test
    public void testDelete_NotFound() {
        Long id = 99L;
        when(rateRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RateNotFoundException.class, () -> rateService.delete(id));

        verify(rateRepository, times(0)).delete(any());
    }
}

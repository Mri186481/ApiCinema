package com.svalero.cinemas.service;

import com.svalero.cinemas.domain.Rate;
import com.svalero.cinemas.domain.dto.RateInDto;
import com.svalero.cinemas.domain.dto.RateOutDto;
import com.svalero.cinemas.exception.RateNotFoundException;
import com.svalero.cinemas.repository.RateRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class RateService {

    @Autowired
    private RateRepository rateRepository;
    @Autowired
    private ModelMapper modelMapper;

    // Obtener todos las tarifas
    public List<RateOutDto> getAll(LocalDate rateDate, String nameDayRate, Boolean promoDay) {
        List<Rate> rateList;
//
        boolean hasRateDate = rateDate != null;
        boolean hasNameDayRate = nameDayRate != null && !nameDayRate.isEmpty();
        boolean hasPromoDay = promoDay != null;

        if (hasRateDate && hasNameDayRate && hasPromoDay) {
            rateList = rateRepository.findByRateDateAndNameDayRateAndPromoDay(rateDate, nameDayRate, promoDay);
        } else if (hasRateDate && hasNameDayRate) {
            rateList = rateRepository.findByRateDateAndNameDayRate(rateDate,nameDayRate);
        } else if (hasRateDate && hasPromoDay) {
            rateList = rateRepository.findByRateDateAndPromoDay(rateDate,promoDay);
        } else if (hasNameDayRate && hasPromoDay) {
            rateList = rateRepository.findByNameDayRateAndPromoDay(nameDayRate, promoDay);
        } else if (hasRateDate) {
            rateList = rateRepository.findByRateDate(rateDate);
        } else if (hasNameDayRate) {
            rateList = rateRepository.findByNameDayRate(nameDayRate);
        } else if (hasPromoDay) {
            rateList = rateRepository.findByPromoDay(promoDay);
        } else {
            rateList = rateRepository.findAll();
        }

        return modelMapper.map(rateList, new TypeToken<List<RateOutDto>>() {}.getType());
    }

    // Get by ID

    public Rate get(long id) throws RateNotFoundException {
        return rateRepository.findById(id)
                .orElseThrow(RateNotFoundException::new);
    }

    // Dar de alta una Tarifa
    public RateOutDto add(RateInDto rateInDto) {
        Rate rate = modelMapper.map(rateInDto, Rate.class);
        rate = rateRepository.save(rate);
        return modelMapper.map(rate, RateOutDto.class);
    }
    // Modificar una Tarifa
    public RateOutDto modify(long rateId, RateInDto rateInDto) throws RateNotFoundException {
        Rate rate = rateRepository.findById(rateId).orElseThrow(RateNotFoundException::new);
        modelMapper.map(rateInDto, rate);
        rateRepository.save(rate);
        return modelMapper.map(rate, RateOutDto.class);
    }
    //Modificar parcialmente una tarifa
    public RateOutDto modifyPartial(Long rateId, Map<String, Object> updates) {
        Rate rate = rateRepository.findById(rateId).orElseThrow(RateNotFoundException::new);
        modelMapper.map(updates, rate);
        rateRepository.save(rate);
        return modelMapper.map(rate, RateOutDto.class);
    }


    public void delete(long rateId) throws RateNotFoundException {
        Rate rate = rateRepository.findById(rateId).orElseThrow(RateNotFoundException::new);
        rateRepository.delete(rate);
    }

}

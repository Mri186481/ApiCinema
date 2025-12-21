package com.svalero.cinemas.repository;

import com.svalero.cinemas.domain.Rate;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RateRepository extends CrudRepository<Rate, Long> {
    // Método para obtener todos los usuarios
    List<Rate> findAll();

    Optional<Rate> findById(Long id);

    List<Rate> findByRateDate(LocalDate rateDate);

    List<Rate> findByNameDayRate(String nameDayRate);

    List<Rate> findByPromoDay(boolean promoDay);

    List<Rate> findByRateDateAndNameDayRateAndPromoDay(LocalDate rateDate, String nameDayRate, boolean promoDay);

    List<Rate> findByRateDateAndNameDayRate(LocalDate rateDate, String nameDayRate);

    List<Rate> findByRateDateAndPromoDay(LocalDate rateDate, boolean promoDay);

    List<Rate> findByNameDayRateAndPromoDay(String nameDayRate, boolean promoDay);
}

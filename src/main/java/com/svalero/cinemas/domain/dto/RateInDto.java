package com.svalero.cinemas.domain.dto;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RateInDto {
    @NotNull(message = "rate_date is a mandatory field")
    private LocalDate rateDate;
    private String nameDayRate;
    private BigDecimal youngDiscount;
    private BigDecimal studentDiscount;
    private BigDecimal seniorDiscount;
    private BigDecimal promoDayDiscount;
    private BigDecimal memberDiscount;
    private BigDecimal room3dPlus;
    private BigDecimal roomAtmosPlus;
    private BigDecimal roomLaserPlus;
    private boolean promoDay;

}

package com.svalero.cinemas.domain.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatInDto {

    @Min(value = 1, message = "La fila debe ser un número positivo.")
    private int seatRow;

    @Min(value = 1, message = "La columna debe ser un número positivo.")
    private int seatColumn;

    private boolean seatAccesible;
    private String status;
    private double priceSurcharge;
    private LocalDate lastMaintenanceDate;

}
package com.svalero.cinemas.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatOutDto {
    private long id;
    private int seatRow;
    private int seatcolumn;
    private boolean seatAccesible;
    private String status;
    private double priceSurcharge;
    private LocalDate lastMaintenanceDate;
    private long roomId;
    private String roomName;
}

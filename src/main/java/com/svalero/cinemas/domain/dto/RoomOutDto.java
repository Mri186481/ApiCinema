package com.svalero.cinemas.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomOutDto {
    private long id;
    private String roomName;
    private LocalDate openingDate;
    private int capacity;
    private boolean room3d;
    private boolean roomAtmos;
    private boolean roomLaser;
//    private List<Seat> seats;
}

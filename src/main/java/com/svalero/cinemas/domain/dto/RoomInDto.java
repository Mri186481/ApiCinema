package com.svalero.cinemas.domain.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomInDto {

    @NotBlank(message = "Name Room is a mandatory field")
    private String roomName;

    @NotNull(message = "Opening date is a mandatory field")
    private LocalDate openingDate;
    @NotNull(message = "Capacity is a mandatory field")
    private int capacity;
    private boolean room3d;
    private boolean roomAtmos;
    private boolean roomLaser;
  
}

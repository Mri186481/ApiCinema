package com.svalero.cinemas.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScreeningInDto {

    @NotNull(message = "Screening time is required")
    private LocalDateTime screeningTime;

    @NotNull(message = "Theater room is required")
    private String theaterRoom;

    @NotNull(message = "Ticket price is required")
    private Double ticketPrice;

    private boolean subtitled;

    @NotNull(message = "Movie ID is required")
    private Long movieId;
}

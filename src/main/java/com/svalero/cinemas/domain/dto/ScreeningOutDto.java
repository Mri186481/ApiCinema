package com.svalero.cinemas.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScreeningOutDto {
    private Long id;
    private LocalDateTime screeningTime;
    private double ticketPrice;
    private boolean subtitled;
    private Long movieId;
    private Long roomId;
    private String movieTitle;
    private String roomName;
}

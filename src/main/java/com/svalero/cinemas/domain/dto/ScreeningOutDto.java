package com.svalero.cinemas.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
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

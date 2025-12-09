package com.svalero.cinemas.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieOutDto {

    private Long id;
    private String movieTitle;
    private String genre;
    private int durationMinutes;
    private double filmingLatitude;
    private double filmingLongitude;
    private LocalDate releaseDate;
    private boolean currentlyShowing;
  
}

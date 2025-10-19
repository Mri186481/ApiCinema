package com.svalero.cinemas.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieInDto {

    private Long id;
    @NotBlank
    private String title;
    @NotBlank
    private String genre;
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private int durationMinutes;
    @NotNull(message = "Release date is required")
    private LocalDate releaseDate;
    private boolean currentlyShowing;
  
}

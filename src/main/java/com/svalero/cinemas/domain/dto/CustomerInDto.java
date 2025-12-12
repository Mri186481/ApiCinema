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
public class CustomerInDto {


    @NotBlank(message = "Name is a mandatory field")
    private String name;

    @NotBlank(message = "Username is a mandatory field")
    private String surname;

    @NotBlank(message = "Address is a mandatory field")
    private String address;

    @NotNull(message = "Birthdate is a mandatory field")
    private LocalDate birthDate;

    @NotBlank(message = "Mail is a mandatory field")
    private String mail;
    private double latitude;
    private double longitude;
    // Por defecto son false
    private boolean admitsAdvertising;
    private boolean young;
    private boolean student;
    private boolean senior;
    private boolean member;
  
}

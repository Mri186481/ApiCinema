package com.svalero.cinemas.domain.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOutDto {
    private long id;
    private String name;
    private String surname;
    private String address;
    private double latitude;
    private double longitude;
    private LocalDate birthDate;
    private String mail;
    private boolean admitsAdvertising;
    private boolean young;
    private boolean student;
    private boolean senior;
    private boolean member;
}

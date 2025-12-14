package com.svalero.cinemas.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketOutDto {
    private Long id;
    private LocalDateTime saleDate;
    private double finalPricePaid;
    private String ticketCode;
//    Customer
    private String name;
    private String mail;
    private boolean young;
    private boolean student;
    private boolean senior;
    private boolean member;
//    Screening
    private String movieTitle;
    private double ticketPrice;
    private String roomName;
    private LocalDateTime screeningTime;
//     Seat
    private int seatRow;
    private int seatcolumn;
    private boolean seatAccesible;
//     Room
    private boolean room3d;
    private boolean roomAtmos;
    private boolean roomLaser;


}

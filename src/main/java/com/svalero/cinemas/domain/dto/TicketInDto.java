package com.svalero.cinemas.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketInDto {
    @NotNull(message = "Final Price is required")
    private double finalPricePaid;
    private boolean scanned;
    @NotNull(message = "tickedCode is required")
    private String ticketCode;
    private String status;
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    @NotNull(message = "Rate ID is required")
    private Long rateId;
    @NotNull(message = "Screening ID is required")
    private Long screeningId;
    @NotNull(message = "Seat ID is required")
    private Long seatId;
}
package com.svalero.cinemas.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "Ticket")
@Table(name="tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime saleDate;

    @Column(name = "final_price_paid")
    private double finalPricePaid;
    //Precio final exacto pagado por el ticket

    @Column(name = "scanned")
    @ColumnDefault("FALSE")
    private boolean scanned = false;
    //Indica si el ticket ha sido escaneado o validado

    @Column(name = "ticket_code", unique = true, nullable = false)
    private String ticketCode;
    //Identificador UNICO del ticket que se le entrga al cliente para ser escaneado

    @Column(name = "status", length = 20)
    @ColumnDefault("'ACTIVE'")
    private String status = "ACTIVE";
    //Estado del ticket: ACTIVE(activo), REFUNDED(devuelto), CANCELLED(anualdo).

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    //
    @ManyToOne
    @JoinColumn(name = "rate_id")
    private Rate rate;

    @ManyToOne
    @JoinColumn(name = "screening_id")
    private Screening screening;

    @ManyToOne
    @JoinColumn(name = "seat_id")
    private Seat seat;
}

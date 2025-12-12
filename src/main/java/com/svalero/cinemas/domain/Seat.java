package com.svalero.cinemas.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "Seat")
@Table(name="seats")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name ="seat_row", nullable = false)
    private int seatRow;

    @Column(name ="seat_column", nullable = false)
    private int seatColumn;

    @Column(name ="seat_accesible")
    @ColumnDefault("FALSE")
    private boolean seatAccesible;

    @Column(name = "status", length = 20)
    @ColumnDefault("'OPERATIONAL'")
    private String status = "OPERATIONAL";
   //Estado operativo de la butaca (ej. "OPERATIONAL", "BROKEN", "MAINTENANCE")

    @Column(name = "price_surcharge", precision = 5, scale = 2)
    @ColumnDefault("0.0")
    private BigDecimal priceSurcharge;
    //Suplemento de precio si la butaca es especial(VIP, reclinable...)

    @Column(name = "last_maintenance_date")
    private LocalDate lastMaintenanceDate;
    //Fecha de la ultima revision o mantenimiento, puede ser nulo si nunca se ha revisado

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @OneToMany(mappedBy = "seat")
    @JsonBackReference(value="seats_tickets")
    private List<Ticket> tickets;

}

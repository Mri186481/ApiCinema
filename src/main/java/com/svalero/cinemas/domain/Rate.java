package com.svalero.cinemas.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "Rate")
@Table(name="rates")

public class Rate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "rate_date", nullable = false)
    private LocalDate rateDate;
    @Column(name = "young_discount", nullable = false)
    private double youngDiscount;
    @Column(name = "student_discount", nullable = false)
    private double studentDiscount;
    @Column(name = "senior_discount", nullable = false)
    private double seniorDiscount;
    @Column(name = "promo_day_discount", nullable = false)
    private double promoDayDiscount;
    @Column(name = "member_discount", nullable = false)
    private double memberDiscount;
    @Column(name = "room_3d_plus", nullable = false)
    private double room3dPlus;
    @Column(name = "room_Atmos_plus", nullable = false)
    private double roomAtmosPlus;
    @Column(name = "room_Laser_plus", nullable = false)
    private double roomLaserPlus;
    @Column(name = "promo_day")
    @ColumnDefault("FALSE")
    private boolean promoDay;
    //Asi seria con Rate:
//    @OneToMany(mappedBy = "rate")
//    @JsonBackReference(value="rates_tickets")
//    private List<Ticket> tickets;
//de momento no tiene relacion con ninguna tabla, CREO QUE DEBERIA TENER UNA RELACION 1-->N  CON TICKETS
//se podria hacer que tuviese relacion directa, como customer con tickets,
//habria que poner su id como campo clave, al igual que customers en tickets
//de esa manera en tickets tb se tiene informacion de los descuentos que hay
//pero creo que seria mejor acceder y ya, o es mejor tb ponerlo en el ticketOutDto?
//esto seria una buena pregunta para Santi

//seria una tabla donde se generaria un registro de tarifas diario
//y aqui es donde a traves del campo promoDay se puede aclarar si es dia del espectador
}

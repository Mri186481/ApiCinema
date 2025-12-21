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
@Entity(name = "Rate")
@Table(name="rates")

public class Rate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "rate_date", nullable = false)
    private LocalDate rateDate;
    @Column(name = "name_day_rate")
    @ColumnDefault("'Normal'")
    private String nameDayRate;
    @Column(name = "young_discount", precision = 5, scale = 2)
    @ColumnDefault("2.0")
    private BigDecimal youngDiscount = BigDecimal.valueOf(2.0);
    @Column(name = "student_discount", precision = 5, scale =2)
    @ColumnDefault("2.0")
    private BigDecimal studentDiscount = BigDecimal.valueOf(2.0);
    @Column(name = "senior_discount", precision = 5, scale = 2)
    @ColumnDefault("2.0")
    private BigDecimal seniorDiscount = BigDecimal.valueOf(2.0);
    @Column(name = "promo_day_discount", precision = 5, scale = 2)
    @ColumnDefault("2.0")
    private BigDecimal promoDayDiscount = BigDecimal.valueOf(2.0);
    @Column(name = "member_discount", precision = 5, scale = 2)
    @ColumnDefault("2.0")
    private BigDecimal memberDiscount = BigDecimal.valueOf(2.0);
    @Column(name = "room_3d_plus", precision = 5, scale = 2)
    @ColumnDefault("2.0")
    private BigDecimal room3dPlus = BigDecimal.valueOf(2.0);
    @Column(name = "room_Atmos_plus", precision = 5, scale = 2)
    @ColumnDefault("2.0")
    private BigDecimal roomAtmosPlus = BigDecimal.valueOf(2.0);
    @Column(name = "room_Laser_plus", precision = 5, scale = 2)
    @ColumnDefault("2.0")
    private BigDecimal roomLaserPlus = BigDecimal.valueOf(2.0);
    @Column(name = "promo_day")
    @ColumnDefault("FALSE")
    private boolean promoDay = false;

    @OneToMany(mappedBy = "rate")
    @JsonBackReference(value="rates_tickets")
    private List<Ticket> tickets;

}

package com.svalero.cinemas.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "Room")
@Table(name="rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name ="room_name", nullable = false)
    private String roomName;

    @Column(name = "opening_date", nullable = false)
    private LocalDate openingDate;

    @Column(name = "capacity")
    @ColumnDefault("0")
    private int capacity;

    @Column(name ="room_3d")
    @ColumnDefault("FALSE")
    private boolean room3d;

    @Column(name ="room_atmos")
    @ColumnDefault("FALSE")
    private boolean roomAtmos;

    @Column(name ="room_laser")
    @ColumnDefault("FALSE")
    private boolean roomLaser;

    @OneToMany(mappedBy = "room")
    @JsonBackReference(value="rooms_seats")
    private List<Seat> seats;

    @OneToMany(mappedBy = "room")
    @JsonBackReference(value="rooms_screenings")
    private List<Screening> screenings;


}

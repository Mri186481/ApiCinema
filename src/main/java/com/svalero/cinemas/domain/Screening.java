package com.svalero.cinemas.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Screening")
@Table(name = "screenings")
public class Screening {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @NotNull(message = "Screening time is required")
    private LocalDateTime screeningTime;

    @NotNull(message = "Price is required")
    @Column(nullable = false)
    private double ticketPrice;

    @Column
    @ColumnDefault("FALSE")
    private boolean subtitled;

    @Column(name = "audio_language", length = 5) // Longitud para códigos de idioma (ej. "es" o "es-ES")
    private String audioLanguage = "es-ES";

    @Column(name = "ad_duration_minutes")
    @ColumnDefault("5")
    private int adDurationMinutes = 5;

    @Column(name = "has_intermission")
    @ColumnDefault("FALSE")
    private boolean hasIntermission = false;


    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @OneToMany(mappedBy = "screening")
    @JsonBackReference(value="screenings_tickets")
    private List<Ticket> tickets;
}

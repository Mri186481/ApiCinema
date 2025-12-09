package com.svalero.cinemas.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "movies")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String movieTitle;

    @NotNull
    private String genre;

    private int durationMinutes;

    @ColumnDefault("0")
    private double filmingLatitude;

    @ColumnDefault("0")
    private double filmingLongitude;

    private LocalDate releaseDate;


    private boolean currentlyShowing;
    //Acoplamiento FUERTE(ciclo de vida dependiente, si se borra un registro movie se borran sus registros screenings asociados)
    //Solo he considerado conveniente ponerla aqui, las demas relaciones son acoplamiento debil(solo relacion)
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("movie")
    private List<Screening> screenings;
}

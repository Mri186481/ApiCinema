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
@Entity(name = "Customer")
@Table(name="customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Column(nullable = false)
    private String address;

    @ColumnDefault("0.0")
    private double latitude;

    @ColumnDefault("0.0")
    private double longitude;

    @Column(name = "birthdate", nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false)
    private String mail;

    @Column(name = "admits_advertising")
    @ColumnDefault("FALSE")
    private boolean admitsAdvertising;

    @Column // No se necesita 'name' si coincide con el campo
    @ColumnDefault("FALSE")
    private boolean young;

    @Column
    @ColumnDefault("FALSE")
    private boolean student;

    @Column
    @ColumnDefault("FALSE")
    private boolean senior;

    @Column
    @ColumnDefault("FALSE")
    private boolean member;

    @OneToMany(mappedBy = "customer")
    @JsonBackReference(value="customers_tickets")
    private List<Ticket> tickets;
//Si elimino un cliente no tengo porque borrar las entradas asociadas a ese cliente



}

package ru.danil.springtest.model;

import jakarta.persistence.*;

import java.util.UUID;

@Table(name = "passport")
public class Passport {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "passport_number")
    private int passportNumber;

    @OneToOne
    @JoinColumn(name = "person_id", referencedColumnName = "id")
    private Person person;
}

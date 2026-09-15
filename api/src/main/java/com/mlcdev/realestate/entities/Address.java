package com.mlcdev.realestate.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Embeddable
public class Address {

    @Column(nullable = false, length = 255)
    private String street;
    @Column(nullable = false, length = 20)
    private String number;
    @Column(length = 255)
    private String complement;
    @Column(nullable = false, length = 255)
    private String neighborhood;
    @Column(nullable = false, length = 255)
    private String city;
    @Column(nullable = false, length = 2)
    private String state;
    @Column(nullable = false, length = 8)
    private String zipCode;
}

package com.nithin.addressmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    // A normal VARCHAR has a limited length.
    //
    // Addresses can potentially be long:
    // tells MySQL to use a TEXT column.
    private String rawAddress;
    private String houseNumber;
    private String buildingName;
    private String street;
    private String landmark;
    private String area;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Double confidenceScore;
    private String processingMessage;
    @Enumerated(EnumType.STRING)
    // Without careful configuration, enums can be stored as numbers:
    // EnumType.STRING stores readable values in the database.
    private AddressStatus status;

    // Many addresses can belong to one customer.
    @ManyToOne(fetch = FetchType.LAZY)
    // This tells Hibernate to create: customer_id inside the addresses table.
    // it becomes the foreign key
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.nithin.addressmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;


    // mappedBy = customer
    // it refers to the filed inside address
    // private Customer customer;
    @OneToMany(
            mappedBy = "customer",
            // This means certain operations on a Customer cascade to its Addresses.
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Address> addresses = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

// foreign key (customer_id)
//       references customers (id)
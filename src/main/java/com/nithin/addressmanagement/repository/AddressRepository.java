package com.nithin.addressmanagement.repository;

import com.nithin.addressmanagement.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {

    // custom mySQL
    // Spring Data JPA reads this method name and understands: find By Customer Id

    List<Address> findByCustomerId(Long customerId);
}

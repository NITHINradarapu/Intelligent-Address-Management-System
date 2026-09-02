package com.nithin.addressmanagement.repository;

import com.nithin.addressmanagement.entity.PostalCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostalCodeRepository
        extends JpaRepository<PostalCode, Long> {

    Optional<PostalCode> findByCode(String code);
}
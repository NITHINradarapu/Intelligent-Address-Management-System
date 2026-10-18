package com.nithin.addressmanagement.repository;

import com.nithin.addressmanagement.entity.PostalCode;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostalCodeRepository
        extends JpaRepository<PostalCode, Long> {

    @Cacheable("postalCodes")
    Optional<PostalCode> findByCode(String code);
}
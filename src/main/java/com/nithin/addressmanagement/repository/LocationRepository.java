package com.nithin.addressmanagement.repository;

import com.nithin.addressmanagement.entity.Location;
import com.nithin.addressmanagement.entity.LocationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationRepository
        extends JpaRepository<Location, Long> {

    List<Location> findByType(LocationType type);
}
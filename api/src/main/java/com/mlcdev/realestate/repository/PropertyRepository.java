package com.mlcdev.realestate.repository;

import com.mlcdev.realestate.entities.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID>, JpaSpecificationExecutor<Property> {

    @Query("SELECT p.available from Property p WHERE p.id = :propertyId")
    Optional<Boolean> isAvailableById(UUID propertyId);
}

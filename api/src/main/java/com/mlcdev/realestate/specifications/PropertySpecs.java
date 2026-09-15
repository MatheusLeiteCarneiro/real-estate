package com.mlcdev.realestate.specifications;

import com.mlcdev.realestate.entities.Property;
import com.mlcdev.realestate.entities.PropertyCategory;
import com.mlcdev.realestate.entities.TransactionType;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.UUID;

public class PropertySpecs {

    private PropertySpecs() {
    }

    public static PredicateSpecification<Property> searchContains(String search){
        return (root, criteriaBuilder) -> {
            if (ObjectUtils.isEmpty(search)){return null;}
            String pattern = "%" + search.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("address").get("city")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("address").get("neighborhood")), pattern)
            );
        };
    }

    public static PredicateSpecification<Property> minPrice(BigDecimal minPrice){
        return (root, criteriaBuilder) -> {
            if (ObjectUtils.isEmpty(minPrice)){return null;}
            return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
        };
    }

    public static PredicateSpecification<Property> maxPrice(BigDecimal maxPrice){
        return (root, criteriaBuilder) -> {
            if (ObjectUtils.isEmpty(maxPrice)){return null;}
            return criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }

    public static PredicateSpecification<Property> minArea(BigDecimal minArea){
        return (root, criteriaBuilder) -> {
            if (ObjectUtils.isEmpty(minArea)){return null;}
            return criteriaBuilder.greaterThanOrEqualTo(root.get("area"), minArea);
        };
    }

    public static PredicateSpecification<Property> maxArea(BigDecimal maxArea){
        return (root, criteriaBuilder) -> {
            if (ObjectUtils.isEmpty(maxArea)){return null;}
            return criteriaBuilder.lessThanOrEqualTo(root.get("area"), maxArea);
        };
    }

    public static PredicateSpecification<Property> transactionTypeEquals(TransactionType transactionType){
        return (root, criteriaBuilder) -> {
            if (ObjectUtils.isEmpty(transactionType)){return null;}
            return criteriaBuilder.equal(root.get("transactionType"), transactionType);
        };
    }

    public static PredicateSpecification<Property> categoryEquals(PropertyCategory category){
        return (root, criteriaBuilder) -> {
            if (ObjectUtils.isEmpty(category)){return null;}
            return criteriaBuilder.equal(root.get("category"), category);
        };
    }

    public static PredicateSpecification<Property> minBedrooms(Integer minBedrooms){
        return (root, criteriaBuilder) -> {
            if (ObjectUtils.isEmpty(minBedrooms)){return null;}
            return criteriaBuilder.greaterThanOrEqualTo(root.get("bedrooms"), minBedrooms);
        };
    }

    public static PredicateSpecification<Property> maxBedrooms(Integer maxBedrooms){
        return (root, criteriaBuilder) -> {
            if (ObjectUtils.isEmpty(maxBedrooms)){return null;}
            return criteriaBuilder.lessThanOrEqualTo(root.get("bedrooms"), maxBedrooms);
        };
    }

    public static PredicateSpecification<Property> minBathrooms(Integer minBathrooms){
        return (root, criteriaBuilder) -> {
            if (ObjectUtils.isEmpty(minBathrooms)){return null;}
            return criteriaBuilder.greaterThanOrEqualTo(root.get("bathrooms"), minBathrooms);
        };
    }

    public static PredicateSpecification<Property> minSuites(Integer minSuites){
        return (root, criteriaBuilder) -> {
            if (ObjectUtils.isEmpty(minSuites)){return null;}
            return criteriaBuilder.greaterThanOrEqualTo(root.get("suites"), minSuites);
        };
    }

    public static PredicateSpecification<Property> minParkingSpots(Integer minParkingSpots){
        return (root, criteriaBuilder) -> {
            if (ObjectUtils.isEmpty(minParkingSpots)){return null;}
            return criteriaBuilder.greaterThanOrEqualTo(root.get("parkingSpots"), minParkingSpots);
        };
    }

    public static PredicateSpecification<Property> brokerIdEquals(UUID brokerId){
        return (root, criteriaBuilder) -> {
            if (ObjectUtils.isEmpty(brokerId)){return null;}
            return criteriaBuilder.equal(root.get("broker").get("id"), brokerId);
        };
    }

    public static PredicateSpecification<Property> isAvailable() {
        return (root, criteriaBuilder) ->
                criteriaBuilder.isTrue(root.get("available"));
    }
}

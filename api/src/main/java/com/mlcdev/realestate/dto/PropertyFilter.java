package com.mlcdev.realestate.dto;

import com.mlcdev.realestate.entities.PropertyCategory;
import com.mlcdev.realestate.entities.TransactionType;

import java.math.BigDecimal;

public record PropertyFilter(
        String search,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        BigDecimal minArea,
        BigDecimal maxArea,
        TransactionType transactionType,
        PropertyCategory category,
        Integer minBedrooms,
        Integer maxBedrooms,
        Integer minBathrooms,
        Integer minSuites,
        Integer minParkingSpots
) {
}

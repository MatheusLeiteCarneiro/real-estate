package com.mlcdev.realestate.dto;

import com.mlcdev.realestate.entities.PropertyCategory;
import com.mlcdev.realestate.entities.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class PropertySummaryDTO {

    private UUID id;
    private String title;
    private String description;
    private BigDecimal price;

    private TransactionType transactionType;
    private PropertyCategory category;


    private Integer suites;
    private Integer bedrooms;
    private Integer bathrooms;
    private BigDecimal area;
    private Integer parkingSpots;

    private String neighborhood;
    private String city;
    private String state;

    private ImageDTO primaryImage;

    private boolean available;


}

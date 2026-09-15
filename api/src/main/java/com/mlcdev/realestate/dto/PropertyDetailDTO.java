package com.mlcdev.realestate.dto;

import com.mlcdev.realestate.entities.PropertyCategory;
import com.mlcdev.realestate.entities.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class PropertyDetailDTO {

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

    private AddressDTO address;

    @Builder.Default
    private List<ImageDTO> images = new ArrayList<>();

    private Instant createdAt;

    private boolean available;

}

package com.mlcdev.realestate.dto;

import com.mlcdev.realestate.entities.PropertyCategory;
import com.mlcdev.realestate.entities.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "PropertyCreateRequest", description = "Request body used to create a property listing.")
public class PropertyCreateDTO {

    @NotBlank(message = "This field should not be left blank")
    @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
    @Schema(description = "Public title for the property listing.", example = "Modern family house", minLength = 5, maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "This field should not be left blank")
    @Size(min = 10, message = "The description should contain at least 10 characters")
    @Schema(description = "Detailed property description.", example = "Spacious property near schools, parks, and local shops.", minLength = 10, requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;


    @NotNull(message = "This field should not be left null")
    @Positive(message = "This field should be greater than 0")
    @Schema(description = "Listing price.", example = "750000.00", minimum = "0.01", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    @NotNull(message = "Transaction type is required")
    @Schema(description = "Commercial transaction type.", example = "SALE", requiredMode = Schema.RequiredMode.REQUIRED)
    private TransactionType transactionType;

    @NotNull(message = "Category is required")
    @Schema(description = "Property category.", example = "HOUSE", requiredMode = Schema.RequiredMode.REQUIRED)
    private PropertyCategory category;

    @PositiveOrZero(message = "This field cannot be negative")
    @Schema(description = "Number of suites.", example = "1", minimum = "0")
    private Integer suites;

    @PositiveOrZero(message = "This field cannot be negative")
    @Schema(description = "Number of bedrooms.", example = "3", minimum = "0")
    private Integer bedrooms;

    @PositiveOrZero(message = "This field cannot be negative")
    @Schema(description = "Number of bathrooms.", example = "2", minimum = "0")
    private Integer bathrooms;

    @NotNull(message = "This field should not be left null")
    @Positive(message = "This field should be greater than 0")
    @Schema(description = "Total private area in square meters.", example = "120.50", minimum = "0.01", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal area;

    @PositiveOrZero(message = "This field cannot be negative")
    @Schema(description = "Number of parking spots.", example = "2", minimum = "0")
    private Integer parkingSpots;

    @Valid
    @NotNull(message = "This field should not be left null")
    @Schema(description = "Property address.", requiredMode = Schema.RequiredMode.REQUIRED)
    private AddressDTO address;
}

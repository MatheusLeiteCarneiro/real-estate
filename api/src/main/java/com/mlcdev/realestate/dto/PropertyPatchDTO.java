package com.mlcdev.realestate.dto;

import com.mlcdev.realestate.entities.PropertyCategory;
import com.mlcdev.realestate.entities.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "PropertyPatchRequest", description = "Partial request body used to update a property listing. Only provided fields are updated.")
public class PropertyPatchDTO {

    @Pattern(regexp = "^\\s*\\S.*$", message = "This field should not be left blank")
    @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
    @Schema(description = "New public title for the property listing.", example = "Updated family house", minLength = 5, maxLength = 255)
    private String title;

    @Pattern(regexp = "^\\s*\\S.*$", message = "This field should not be left blank")
    @Size(min = 10, message = "The description should contain at least 10 characters")
    @Schema(description = "New detailed property description.", example = "Updated property description with current amenities.", minLength = 10)
    private String description;

    @Positive(message = "This field should be greater than 0")
    @Schema(description = "New listing price.", example = "690000.00", minimum = "0.01")
    private BigDecimal price;

    @Schema(description = "New commercial transaction type.", example = "RENT")
    private TransactionType transactionType;

    @Schema(description = "New property category.", example = "APARTMENT")
    private PropertyCategory category;

    @PositiveOrZero(message = "This field cannot be negative")
    @Schema(description = "New number of suites.", example = "1", minimum = "0")
    private Integer suites;

    @PositiveOrZero(message = "This field cannot be negative")
    @Schema(description = "New number of bedrooms.", example = "2", minimum = "0")
    private Integer bedrooms;

    @PositiveOrZero(message = "This field cannot be negative")
    @Schema(description = "New number of bathrooms.", example = "2", minimum = "0")
    private Integer bathrooms;

    @Positive(message = "This field should be greater than 0")
    @Schema(description = "New total private area in square meters.", example = "95.75", minimum = "0.01")
    private BigDecimal area;

    @PositiveOrZero(message = "This field cannot be negative")
    @Schema(description = "New number of parking spots.", example = "1", minimum = "0")
    private Integer parkingSpots;

    @Valid
    @Schema(description = "Partial address update.")
    private AddressPatchDTO address;
}

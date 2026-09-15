package com.mlcdev.realestate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Partial address payload. Only provided fields are updated.")
public class AddressPatchDTO {

    @Size(max = 255, message = "Street must not exceed 255 characters")
    @Schema(description = "New street name.", example = "Updated Main Street", maxLength = 255)
    private String street;

    @Size(max = 20, message = "Street number must not exceed 20 characters")
    @Schema(description = "New street number.", example = "200", maxLength = 20)
    private String number;

    @Size(max = 255, message = "Complement must not exceed 255 characters")
    @Schema(description = "New additional address information.", example = "Suite 5", maxLength = 255)
    private String complement;

    @Size(max = 255, message = "Neighborhood must not exceed 255 characters")
    @Schema(description = "New neighborhood name.", example = "Central District", maxLength = 255)
    private String neighborhood;

    @Size(max = 255, message = "City must not exceed 255 characters")
    @Schema(description = "New city name.", example = "Updated City", maxLength = 255)
    private String city;

    @Pattern(regexp = "^[a-zA-Z]{2}$", message = "State must contain 2 letters")
    @Schema(description = "New two-letter state abbreviation.", example = "SP", minLength = 2, maxLength = 2)
    private String state;

    @Pattern(regexp = "^(\\d{8}|\\d{5}-\\d{3})$", message = "Invalid zipcode. Use 12345678 or 12345-678")
    @Schema(description = "ZIP code can use 12345678 or 12345-678 formats.", example = "12345678", minLength = 8, maxLength = 9)
    private String zipCode;


}

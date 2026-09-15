package com.mlcdev.realestate.dto;

import com.mlcdev.realestate.validation.StrongPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Schema(name = "UserPatchRequest", description = "Partial request body used to update a user. Only provided fields are updated.")
public class UserPatchDTO {

    @Pattern(regexp = "^\\s*\\S.*$", message = "This field should not be left blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "New unique username for the user account.", example = "updated.broker", minLength = 3, maxLength = 50)
    private String username;

    @StrongPassword
    @Schema(description = "New user password with uppercase, lowercase, number, and special character.", example = "N3w@Password", format = "password", minLength = 8)
    private String password;

}

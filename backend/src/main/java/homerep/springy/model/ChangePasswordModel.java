package homerep.springy.model;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record ChangePasswordModel(
        @NotBlank @Length(min = 8, max = 64) String currentPassword,
        @NotBlank @Length(min = 8, max = 64) String newPassword
) {
}

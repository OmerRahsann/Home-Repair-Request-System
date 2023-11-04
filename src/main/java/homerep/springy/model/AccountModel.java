package homerep.springy.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record AccountModel(
        @NotBlank @Email String email,
        @NotBlank @Length(min = 8, max = 64) String password
) {
}

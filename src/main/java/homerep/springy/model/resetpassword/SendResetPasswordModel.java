package homerep.springy.model.resetpassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendResetPasswordModel(@NotBlank @Email String email) {
}

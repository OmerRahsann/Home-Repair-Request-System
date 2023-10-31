package homerep.springy.model.resetpassword;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record ResetPasswordModel(
        @NotBlank String token,
        @NotBlank @Length(min = 8) String password // TODO password strength and other requirements?
) {
}

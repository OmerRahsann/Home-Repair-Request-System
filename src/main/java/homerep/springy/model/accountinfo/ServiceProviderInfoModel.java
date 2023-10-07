package homerep.springy.model.accountinfo;

import homerep.springy.validator.annotation.PhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ServiceProviderInfoModel(
        @NotBlank String name,
        @NotBlank String description,
        @NotNull @NotEmpty List<@NotBlank String> services,
        @NotBlank @PhoneNumber String phoneNumber,
        @NotBlank String address,
        @NotBlank @Email String contactEmailAddress
) implements AccountInfoModel {
}

package homerep.springy.model.accountinfo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ServiceProviderInfoModel(
        @NotBlank String name,
        @NotBlank String description,
        @NotNull @NotEmpty List<@NotBlank String> services,
        @NotBlank String phoneNumber, // TODO how do you validate phone numbers?
        @NotBlank String address,
        @NotBlank @Email String contactEmailAddress
) implements AccountInfoModel {
}

package homerep.springy.model.accountinfo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// TODO max length??
public record CustomerInfoModel(
        @NotBlank String firstName,
        @NotNull String middleName,
        @NotBlank String lastName,
        @NotBlank String address,
        @NotBlank String phoneNumber // TODO how do you validate phone numbers?
) implements AccountInfoModel {
}

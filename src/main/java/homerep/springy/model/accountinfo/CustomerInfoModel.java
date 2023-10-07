package homerep.springy.model.accountinfo;

import homerep.springy.validator.annotation.PhoneNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// TODO max length??
public record CustomerInfoModel(
        @NotBlank String firstName,
        @NotNull String middleName,
        @NotBlank String lastName,
        @NotBlank String address,
        @NotBlank @PhoneNumber String phoneNumber
) implements AccountInfoModel {
}

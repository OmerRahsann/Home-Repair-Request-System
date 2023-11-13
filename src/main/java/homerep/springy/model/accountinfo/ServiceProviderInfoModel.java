package homerep.springy.model.accountinfo;

import homerep.springy.entity.ServiceProvider;
import homerep.springy.validator.annotation.PhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public record ServiceProviderInfoModel(
        @NotBlank String name,
        @NotBlank String description,
        @NotNull @NotEmpty List<@NotBlank String> services,
        @NotBlank @PhoneNumber String phoneNumber,
        @NotBlank String address,
        @NotBlank @Email String contactEmailAddress
) implements AccountInfoModel {
    public static ServiceProviderInfoModel fromEntity(ServiceProvider serviceProvider) {
        return new ServiceProviderInfoModel(
                serviceProvider.getName(),
                serviceProvider.getDescription(),
                new ArrayList<>(serviceProvider.getServices()), // Must convert from JPA collection
                serviceProvider.getPhoneNumber(),
                serviceProvider.getAddress(),
                serviceProvider.getAccount().getEmail() // TODO actual contact email address?
        );
    }
}

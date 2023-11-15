package homerep.springy.model.accountinfo;

import homerep.springy.entity.Customer;
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
    public static CustomerInfoModel fromEntity(Customer customer) {
        return new CustomerInfoModel(
                customer.getFirstName(),
                customer.getMiddleName(),
                customer.getLastName(),
                customer.getAddress(),
                customer.getPhoneNumber()
        );
    }
}

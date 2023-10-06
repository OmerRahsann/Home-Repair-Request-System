package homerep.springy.model.accountinfo;

import java.util.List;

import static homerep.springy.util.ValidationUtil.isFilled;
import static homerep.springy.util.ValidationUtil.validateEmail;

public record ServiceProviderInfoModel(
        String name,
        String description,
        List<String> services,
        String phoneNumber,
        String address,
        String contactEmailAddress
) implements AccountInfoModel {
    @Override
    public boolean isValid() {
        if (services == null || services.isEmpty()) {
            return false;
        }
        return isFilled(name) && isFilled(description) && isFilled(phoneNumber) && isFilled(address) && // TODO how do you validate phone numbers?
                validateEmail(contactEmailAddress);
    }
}

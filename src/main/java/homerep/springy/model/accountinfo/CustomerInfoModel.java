package homerep.springy.model.accountinfo;

import static homerep.springy.util.ValidationUtil.isFilled;

public record CustomerInfoModel(
        String firstName,
        String middleName,
        String lastName,
        String address,
        String phoneNumber
) implements AccountInfoModel {
    @Override
    public boolean isValid() {
        // TODO is there a way to simplify validation???
        // TODO max length??
        return isFilled(firstName) && middleName != null &&
                isFilled(lastName) && isFilled(address) &&
                isFilled(phoneNumber); // TODO how do you validate phone numbers?
    }
}

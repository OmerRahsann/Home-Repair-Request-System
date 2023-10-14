package homerep.springy.validator;

import homerep.springy.model.RegisterModel;
import homerep.springy.model.accountinfo.CustomerInfoModel;
import homerep.springy.model.accountinfo.ServiceProviderInfoModel;
import homerep.springy.validator.annotation.ValidRegisterModel;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RegisterModelValidator implements ConstraintValidator<ValidRegisterModel, RegisterModel> {

    @Override
    public boolean isValid(RegisterModel value, ConstraintValidatorContext context) {
        if (value.type() == null) {
            return false;
        }
        return switch (value.type()) {
            case CUSTOMER -> value.accountInfo() instanceof CustomerInfoModel;
            case SERVICE_PROVIDER -> value.accountInfo() instanceof ServiceProviderInfoModel;
        };
    }
}

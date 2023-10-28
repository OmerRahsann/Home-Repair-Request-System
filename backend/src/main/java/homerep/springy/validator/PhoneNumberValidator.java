package homerep.springy.validator;

import homerep.springy.validator.annotation.PhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        if (value == null) {
            return true;
        }
        boolean isValid = true;
        if (value.length() < 10 || 13 < value.length()) {
            context.buildConstraintViolationWithTemplate("must be 10 to 13 digits")
                    .addConstraintViolation();
            isValid = false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                context.buildConstraintViolationWithTemplate("must only contain digits")
                        .addConstraintViolation();
                isValid = false;
                break;
            }
        }
        return isValid;
    }
}

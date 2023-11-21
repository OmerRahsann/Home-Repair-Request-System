package homerep.springy.validator;

import homerep.springy.model.appointment.CreateAppointmentModel;
import homerep.springy.validator.annotation.ValidCreateAppointmentModel;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CreateAppointmentModelValidator implements ConstraintValidator<ValidCreateAppointmentModel, CreateAppointmentModel> {
    @Override
    public boolean isValid(CreateAppointmentModel model, ConstraintValidatorContext context) {
        if (model.startTime() == null || model.endTime() == null) {
            return true;
        }
        if (!model.endTime().isAfter(model.startTime())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("endTime must be after startTime")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}

package homerep.springy.validator.annotation;

import homerep.springy.validator.CreateAppointmentModelValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CreateAppointmentModelValidator.class)
public @interface ValidCreateAppointmentModel {
    String message() default "must be a valid CreateAppointmentModel";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

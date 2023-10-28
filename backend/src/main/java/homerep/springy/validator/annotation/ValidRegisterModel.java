package homerep.springy.validator.annotation;

import homerep.springy.validator.RegisterModelValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RegisterModelValidator.class)
public @interface ValidRegisterModel {
    String message() default "Account info type must match account type";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

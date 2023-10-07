package homerep.springy;

import homerep.springy.model.accountinfo.CustomerInfoModel;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomerModelValidationTest {
    private static Validator validator;

    @BeforeAll
    public static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    CustomerInfoModel customerWithPhoneNumber(String phoneNumber) {
        return new CustomerInfoModel(
                "Zoey", "", "Proasheck",
                "201 Mullica Hill Rd, Glassboro, NJ 08028",
                phoneNumber
        );
    }

    @Test
    public void customerInfoPhoneNumber() {
        Set<ConstraintViolation<CustomerInfoModel>> violations;
        // No country code
        violations = validator.validate(customerWithPhoneNumber("8562564000"));
        assertTrue(violations.isEmpty());
        // US country code
        violations = validator.validate(customerWithPhoneNumber("18562564000"));
        assertTrue(violations.isEmpty());
        // Unassigned 3 digit country code
        violations = validator.validate(customerWithPhoneNumber("9998562564000"));
        assertTrue(violations.isEmpty());

        // Too many digits
        violations = validator.validate(customerWithPhoneNumber("99998562564000"));
        assertSingleViolation(violations, "99998562564000", "must be 10 to 13 digits");
        // Not enough digits
        violations = validator.validate(customerWithPhoneNumber("562564000"));
        assertSingleViolation(violations, "562564000", "must be 10 to 13 digits");

        // + is not allowed
        violations = validator.validate(customerWithPhoneNumber("+18562564000"));
        assertSingleViolation(violations, "+18562564000", "must only contain digits");
        // - is not allowed
        violations = validator.validate(customerWithPhoneNumber("856-256-4000"));
        assertSingleViolation(violations, "856-256-4000", "must only contain digits");
        // Whitespace is not allowed
        violations = validator.validate(customerWithPhoneNumber("856 256 4000"));
        assertSingleViolation(violations, "856 256 4000", "must only contain digits");
    }

    void assertSingleViolation(Set<ConstraintViolation<CustomerInfoModel>> violations, Object value, String message) {
        assertEquals(1, violations.size());
        ConstraintViolation<?> violation = violations.iterator().next();
        assertEquals(value, violation.getInvalidValue());
        assertEquals(message, violation.getMessage());
    }
}

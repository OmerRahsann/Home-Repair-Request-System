package homerep.springy.model.error;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class ValidationErrorModel extends ApiErrorModel {
    @JsonProperty
    private final List<FieldErrorModel> fieldErrors;

    @JsonProperty
    private final List<ObjectErrorModel> objectErrors;

    public ValidationErrorModel(
            List<FieldErrorModel> fieldErrors,
            List<ObjectErrorModel> objectErrors
    ) {
        super("validation_error", "Provided data was invalid");
        this.fieldErrors = fieldErrors;
        this.objectErrors = objectErrors;
    }

    public List<FieldErrorModel> fieldErrors() {
        return fieldErrors;
    }

    public List<ObjectErrorModel> objectErrors() {
        return objectErrors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ValidationErrorModel that = (ValidationErrorModel) o;
        return Objects.equals(fieldErrors, that.fieldErrors) && Objects.equals(objectErrors, that.objectErrors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fieldErrors, objectErrors);
    }
}

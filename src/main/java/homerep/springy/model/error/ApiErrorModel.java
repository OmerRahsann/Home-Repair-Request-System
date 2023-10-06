package homerep.springy.model.error;

import java.util.List;

public record ApiErrorModel(
        long timestamp,
        List<FieldErrorModel> fieldErrors,
        List<ObjectErrorModel> objectErrors
) {
}

package homerep.springy.model.error;

public record FieldErrorModel(
        String field,
        String message
) {
}

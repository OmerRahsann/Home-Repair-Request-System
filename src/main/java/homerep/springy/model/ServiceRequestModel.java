package homerep.springy.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ServiceRequestModel(
    @NotBlank String title,
    @NotBlank String description,
    @Positive int dollars
) {
}

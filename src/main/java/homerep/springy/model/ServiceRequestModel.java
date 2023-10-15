package homerep.springy.model;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ServiceRequestModel(
    @Nullable Integer id,
    @NotBlank String title,
    @NotBlank String description,
    @Positive int dollars, // TODO minimum quote?
    @NotBlank String address
    ) {

    public ServiceRequestModel(String title, String description, int dollars, String address) {
        this(null, title, description, dollars, address);
    }
}

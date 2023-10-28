package homerep.springy.model;

import homerep.springy.entity.ServiceRequest;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.Date;
import java.util.List;

public record ServiceRequestModel(
        @Nullable Integer id, // Nullable properties are sent in responses and ignored in requests.
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String service,
        @Nullable ServiceRequest.Status status,
        @Positive int dollars, // TODO minimum quote?
        @NotBlank String address,
        @Nullable List<String> pictures,
        @Nullable Date creationDate
) {

    public ServiceRequestModel(String title, String description, String service, int dollars, String address) {
        this(null, title, description, service, null, dollars, address, null, null);
    }
}

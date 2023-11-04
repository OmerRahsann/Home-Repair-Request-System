package homerep.springy.model;

import homerep.springy.entity.ServiceRequest;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.With;

import java.util.Date;
import java.util.List;

@With
public record ServiceRequestModel(
        @Nullable Integer id, // Nullable properties are sent in responses and ignored in requests.
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String service,
        @Nullable ServiceRequest.Status status,

        @Positive int dollars, // TODO minimum quote?
        @NotBlank String address,
        @Nullable List<String> pictures,
        @Nullable Date creationDate,

        @Nullable Double latitude,

        @Nullable Double longitude
        ) {

    public ServiceRequestModel(String title, String description, String service, int dollars, String address) {
        this(null, title, description, service, null, dollars, address, null, null, null, null);
    }

    /**
     * Create a ServiceRequestModel from the ServiceRequest entity
     * This does not filter out any information!
     * @param serviceRequest the ServiceRequest
     * @return the model representation of the ServiceRequest
     */
    public static ServiceRequestModel fromEntity(ServiceRequest serviceRequest) {
        return new ServiceRequestModel(
                serviceRequest.getId(),
                serviceRequest.getTitle(),
                serviceRequest.getDescription(),
                serviceRequest.getService(),
                serviceRequest.getStatus(),
                serviceRequest.getDollars(),
                serviceRequest.getAddress(),
                serviceRequest.getImagesUUIDs(),
                serviceRequest.getCreationDate(),
                serviceRequest.getLatitude(),
                serviceRequest.getLongitude()
        );
    }
}

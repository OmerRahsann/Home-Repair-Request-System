package homerep.springy.model.emailrequest;

import homerep.springy.entity.ServiceRequest;
import homerep.springy.model.ServiceRequestModel;
import homerep.springy.model.accountinfo.CustomerInfoModel;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record EmailRequestModel(
        @NotNull ServiceRequestModel serviceRequest,
        @Nullable CustomerInfoModel customer,
        @Nullable String email,
        @NotNull EmailRequestStatus status,
        @NotNull Instant updateTimestamp
        ) {
    public EmailRequestModel(ServiceRequest serviceRequest, EmailRequestStatus status) {
        this(ServiceRequestModel.fromEntity(serviceRequest), null, null, status, null);
    }

    public EmailRequestModel(ServiceRequest serviceRequest, @Nullable CustomerInfoModel customer, @Nullable String email, EmailRequestStatus status, Instant updateTimestamp) {
        this(ServiceRequestModel.fromEntity(serviceRequest), customer, email, status, updateTimestamp);
        if (status == EmailRequestStatus.ACCEPTED) {
            if (email == null || customer == null) {
                throw new IllegalStateException("Expected email and customer info for ACCEPTED Email Request");
            }
        } else {
            if (email != null || customer != null) {
                throw new IllegalStateException("Unexpected email and customer info for non ACCEPTED Email Request");
            }
        }
    }
}

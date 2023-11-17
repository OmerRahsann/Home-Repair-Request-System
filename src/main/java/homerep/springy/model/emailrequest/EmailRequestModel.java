package homerep.springy.model.emailrequest;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record EmailRequestModel(
        int serviceRequestId,
        @Nullable String email,
        @NotNull EmailRequestStatus status,
        @NotNull Instant updateTimestamp
        ) {
    public EmailRequestModel(int serviceRequestId, EmailRequestStatus status) {
        this(serviceRequestId, null, status, null);
    }

    public EmailRequestModel(int serviceRequestId, @Nullable String email, EmailRequestStatus status, Instant updateTimestamp) {
        this.serviceRequestId = serviceRequestId;
        this.email = email;
        this.status = status;
        if (status == EmailRequestStatus.ACCEPTED && email == null) {
            throw new IllegalStateException("Expected email for ACCEPTED Email Request");
        }
        this.updateTimestamp = updateTimestamp;
    }
}

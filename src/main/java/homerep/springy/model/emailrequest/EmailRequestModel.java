package homerep.springy.model.emailrequest;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public record EmailRequestModel(
        @Nullable String email,
        @Nonnull EmailRequestStatus status
) {
    public EmailRequestModel(@Nonnull EmailRequestStatus status) {
        this(null, status);
    }

    public EmailRequestModel(@Nullable String email, @Nonnull EmailRequestStatus status) {
        this.email = email;
        this.status = status;
        if (status == EmailRequestStatus.ACCEPTED && email == null) {
            throw new IllegalStateException("Expected email for ACCEPTED Email Request");
        }
    }
}

package homerep.springy.model.emailrequest;

import homerep.springy.model.accountinfo.ServiceProviderInfoModel;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record EmailRequestInfoModel(
        long id,
        int serviceRequestId,
        @NotNull ServiceProviderInfoModel serviceProvider,
        @NotNull EmailRequestStatus status,
        @NotNull Instant requestTimestamp
) {
}

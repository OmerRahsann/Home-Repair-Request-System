package homerep.springy.model.emailrequest;

import homerep.springy.model.accountinfo.ServiceProviderInfoModel;
import jakarta.validation.constraints.NotNull;

public record EmailRequestInfoModel(
        long id,
        @NotNull ServiceProviderInfoModel serviceProvider,
        @NotNull EmailRequestStatus status
) {
}

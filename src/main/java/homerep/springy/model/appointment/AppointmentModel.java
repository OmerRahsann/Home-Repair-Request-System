package homerep.springy.model.appointment;

import com.fasterxml.jackson.annotation.JsonInclude;
import homerep.springy.model.accountinfo.CustomerInfoModel;
import homerep.springy.model.accountinfo.ServiceProviderInfoModel;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;


public record AppointmentModel(
        long appointmentId,
        @JsonInclude(value = JsonInclude.Include.NON_NULL)
        @Nullable
        CustomerInfoModel customerInfoModel,
        @JsonInclude(value = JsonInclude.Include.NON_NULL)
        @Nullable
        ServiceProviderInfoModel serviceProviderInfoModel,
        int serviceRequestId,
        @NotNull LocalDate date,
        @Nullable AppointmentStatus status,
        @Nullable String message
) {
}

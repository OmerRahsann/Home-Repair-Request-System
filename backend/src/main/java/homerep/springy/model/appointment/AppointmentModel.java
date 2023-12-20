package homerep.springy.model.appointment;

import com.fasterxml.jackson.annotation.JsonInclude;
import homerep.springy.entity.Appointment;
import homerep.springy.model.accountinfo.CustomerInfoModel;
import homerep.springy.model.accountinfo.ServiceProviderInfoModel;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;


public record AppointmentModel(
        long appointmentId,
        @JsonInclude(value = JsonInclude.Include.NON_NULL)
        @Nullable
        CustomerInfoModel customerInfoModel,
        @JsonInclude(value = JsonInclude.Include.NON_NULL)
        @Nullable
        ServiceProviderInfoModel serviceProviderInfoModel,
        int serviceRequestId,
        @NotNull Instant creationTimestamp,
        @Nullable Instant updateTimestamp,
        @NotNull Instant startTime,
        @NotNull Instant endTime,
        @Nullable AppointmentStatus status,
        @Nullable String message
) {
    public static AppointmentModel fromEntity(Appointment appointment) {
        return new AppointmentModel(
                appointment.getId(),
                new CustomerInfoModel(
                        appointment.getCustomer().getFirstName(),
                        appointment.getCustomer().getMiddleName(),
                        appointment.getCustomer().getLastName(),
                        null, // Use the address in the service request
                        appointment.getCustomer().getPhoneNumber()
                ),
                ServiceProviderInfoModel.fromEntity(appointment.getServiceProvider()),
                appointment.getServiceRequest().getId(),
                appointment.getCreationTimestamp(),
                appointment.getUpdateTimestamp(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus(),
                appointment.getMessage()
        );
    }
}

package homerep.springy.model.appointment;

import homerep.springy.validator.annotation.ValidCreateAppointmentModel;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@ValidCreateAppointmentModel
public record CreateAppointmentModel(
        @NotNull @Future Instant startTime,
        @NotNull @Future Instant endTime, // TODO validate endTime > startTime and limit to 24hrs
        @Nullable String message
) {
}

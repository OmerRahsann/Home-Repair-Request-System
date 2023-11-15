package homerep.springy.model.appointment;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateAppointmentModel(
        @NotNull @Future LocalDate date,
        @Nullable String message
) {
}

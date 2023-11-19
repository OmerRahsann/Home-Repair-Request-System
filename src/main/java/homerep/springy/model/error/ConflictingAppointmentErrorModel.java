package homerep.springy.model.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import homerep.springy.model.appointment.AppointmentModel;

import java.util.List;

public class ConflictingAppointmentErrorModel extends ApiErrorModel {
    @JsonProperty
    private final List<AppointmentModel> conflictingAppointments;

    public ConflictingAppointmentErrorModel(List<AppointmentModel> conflictingAppointments) {
        super("conflicting_appointment", "Appointment conflicts with other appointments");
        this.conflictingAppointments = conflictingAppointments;
    }

    public List<AppointmentModel> conflictingAppointments() {
        return conflictingAppointments;
    }
}

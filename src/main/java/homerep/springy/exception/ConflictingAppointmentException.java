package homerep.springy.exception;

import homerep.springy.entity.Appointment;

import java.util.List;

public class ConflictingAppointmentException extends Exception {
    private final List<Appointment> conflictingAppointments;

    public ConflictingAppointmentException(List<Appointment> conflictingAppointments) {
        super("Appointment conflicts with other appointments");
        this.conflictingAppointments = conflictingAppointments;
    }

    public List<Appointment> getConflictingAppointments() {
        return conflictingAppointments;
    }
}

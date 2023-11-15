package homerep.springy.exception;

public class NonExistentAppointmentException extends ApiException {
    public NonExistentAppointmentException() {
        super("non_existent_appointment", "Appointment not found.");
    }
}

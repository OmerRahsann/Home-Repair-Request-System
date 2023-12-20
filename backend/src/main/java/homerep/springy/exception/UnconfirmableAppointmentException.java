package homerep.springy.exception;

public class UnconfirmableAppointmentException extends Exception {
    public UnconfirmableAppointmentException() {
        super("Can't confirm appointment as it has expired, been cancelled, or has already been completed.");
    }
}

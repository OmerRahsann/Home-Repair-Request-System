package homerep.springy.model.appointment;

public enum AppointmentStatus {
    UNCONFIRMED, CONFIRMED, CANCELLED,
    EXPIRED, COMPLETED;

    /**
     * @return The new status after the appointment time has passed
     */
    public AppointmentStatus toExpiredStatus() {
        return switch (this) {
            case UNCONFIRMED -> EXPIRED;
            case CONFIRMED -> COMPLETED;
            default -> this;
        };
    }
}

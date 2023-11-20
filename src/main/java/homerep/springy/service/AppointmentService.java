package homerep.springy.service;

import homerep.springy.entity.Appointment;
import homerep.springy.entity.Customer;
import homerep.springy.entity.ServiceProvider;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.exception.ConflictingAppointmentException;
import homerep.springy.exception.UnconfirmableAppointmentException;
import homerep.springy.model.appointment.AppointmentModel;
import homerep.springy.model.appointment.CreateAppointmentModel;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

public interface AppointmentService {
    Appointment createAppointment(ServiceProvider serviceProvider, ServiceRequest serviceRequest, CreateAppointmentModel createAppointmentModel) throws ConflictingAppointmentException;

    void cancelAppointment(Appointment appointment); // TODO need cancellation message?

    void confirmAppointment(Appointment appointment) throws ConflictingAppointmentException, UnconfirmableAppointmentException;

    List<AppointmentModel> getConflictingCustomerAppointments(Appointment appointment);

    List<AppointmentModel> getAppointmentsByMonth(Customer customer, YearMonth yearMonth, ZoneId zoneId);

    List<AppointmentModel> getAppointmentsByMonth(ServiceProvider serviceProvider, YearMonth yearMonth, ZoneId zoneId);

    List<AppointmentModel> getAppointmentsFor(ServiceRequest serviceRequest);

    List<AppointmentModel> getUnconfirmedAppointments(Customer customer);

    List<AppointmentModel> getUpdatedAppointments(ServiceProvider serviceProvider);
}

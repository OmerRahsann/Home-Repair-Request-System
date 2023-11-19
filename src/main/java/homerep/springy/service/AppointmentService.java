package homerep.springy.service;

import homerep.springy.entity.Appointment;
import homerep.springy.entity.Customer;
import homerep.springy.entity.ServiceProvider;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.model.appointment.AppointmentModel;
import homerep.springy.model.appointment.CreateAppointmentModel;

import java.time.YearMonth;
import java.util.List;

public interface AppointmentService {
    Appointment createAppointment(ServiceProvider serviceProvider, ServiceRequest serviceRequest, CreateAppointmentModel createAppointmentModel);

    void cancelAppointment(Appointment appointment); // TODO need cancellation message?

    boolean confirmAppointment(Appointment appointment);

    List<AppointmentModel> getAppointmentsByMonth(Customer customer, YearMonth yearMonth);

    List<AppointmentModel> getAppointmentsByMonth(ServiceProvider serviceProvider, YearMonth yearMonth);

    List<AppointmentModel> getAppointmentsFor(ServiceRequest serviceRequest);

    List<AppointmentModel> getUnconfirmedAppointments(Customer customer);

    List<AppointmentModel> getUpdatedAppointments(ServiceProvider serviceProvider);
}

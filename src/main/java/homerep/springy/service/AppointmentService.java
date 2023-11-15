package homerep.springy.service;

import homerep.springy.entity.Appointment;
import homerep.springy.entity.Customer;
import homerep.springy.entity.ServiceProvider;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.model.appointment.CreateAppointmentModel;

import java.time.YearMonth;
import java.util.List;

public interface AppointmentService {
    Appointment createAppointment(ServiceProvider serviceProvider, ServiceRequest serviceRequest, CreateAppointmentModel createAppointmentModel);

    void cancelAppointment(Appointment appointment); // TODO need cancelation message?

    boolean confirmAppointment(Appointment appointment);

    List<Appointment> listAppointments(Customer customer, YearMonth yearMonth);

    List<Appointment> listAppointments(ServiceProvider serviceProvider, YearMonth yearMonth);
}

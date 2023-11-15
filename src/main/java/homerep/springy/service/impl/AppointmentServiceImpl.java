package homerep.springy.service.impl;

import homerep.springy.entity.Appointment;
import homerep.springy.entity.Customer;
import homerep.springy.entity.ServiceProvider;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.model.appointment.AppointmentStatus;
import homerep.springy.model.appointment.CreateAppointmentModel;
import homerep.springy.repository.AppointmentRepository;
import homerep.springy.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {
    // TODO mark old unconfirmed appointments as cancelled or some other status
    @Autowired
    private AppointmentRepository appointmentRepository;

    @Override
    public Appointment createAppointment(ServiceProvider serviceProvider, ServiceRequest serviceRequest, CreateAppointmentModel createAppointmentModel) {
        Appointment appointment = new Appointment();
        appointment.setServiceProvider(serviceProvider);
        appointment.setServiceRequest(serviceRequest);
        appointment.setCustomer(serviceRequest.getCustomer());
        appointment.setDate(createAppointmentModel.date());
        appointment.setStatus(AppointmentStatus.UNCONFIRMED);
        appointment.setMessage(createAppointmentModel.message());
        appointment = appointmentRepository.save(appointment);
        // TODO check for failure?
        // TODO send notification/email
        return appointment;
    }

    @Override
    public void cancelAppointment(Appointment appointment) {
        // TODO prevent cancelling past appointments
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return;
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment = appointmentRepository.save(appointment);
        // TODO send notification/email
    }

    @Override
    public boolean confirmAppointment(Appointment appointment) {
        // TODO prevent confirming old appointments
        if (appointment.getStatus() == AppointmentStatus.CONFIRMED) {
            // Already confirmed
            return true;
        }
        if (appointment.getStatus() != AppointmentStatus.UNCONFIRMED) {
            return false;
        }
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment = appointmentRepository.save(appointment);
        return true;
        // TODO send notification/email
    }

    @Override
    public List<Appointment> listAppointments(Customer customer, YearMonth yearMonth) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        // TODO include time, need timezones
        return appointmentRepository.findAllByCustomerAndDateBetween(customer, start, end);
    }

    @Override
    public List<Appointment> listAppointments(ServiceProvider serviceProvider, YearMonth yearMonth) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        // TODO include time, need timezones
        return appointmentRepository.findAllByServiceProviderAndDateBetween(serviceProvider, start, end);
    }
}

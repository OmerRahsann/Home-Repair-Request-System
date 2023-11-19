package homerep.springy.service.impl;

import homerep.springy.entity.Appointment;
import homerep.springy.entity.Customer;
import homerep.springy.entity.ServiceProvider;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.model.appointment.AppointmentModel;
import homerep.springy.model.appointment.AppointmentStatus;
import homerep.springy.model.appointment.CreateAppointmentModel;
import homerep.springy.repository.AppointmentRepository;
import homerep.springy.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {
    @Autowired
    private AppointmentRepository appointmentRepository;

    @Override
    public Appointment createAppointment(ServiceProvider serviceProvider, ServiceRequest serviceRequest, CreateAppointmentModel createAppointmentModel) {
        Appointment appointment = new Appointment();
        appointment.setServiceProvider(serviceProvider);
        appointment.setServiceRequest(serviceRequest);
        appointment.setCustomer(serviceRequest.getCustomer());
        appointment.setCreationTimestamp(Instant.now());
        appointment.setDate(createAppointmentModel.date());
        appointment.setStatus(AppointmentStatus.UNCONFIRMED);
        appointment.setMessage(createAppointmentModel.message());
        appointment = appointmentRepository.save(appointment);

        serviceRequest.getAppointments().add(appointment);
        // TODO check for failure?
        // TODO send notification/email
        return appointment;
    }

    @Override
    public void cancelAppointment(Appointment appointment) {
        if (appointment.getStatus() == AppointmentStatus.UNCONFIRMED || appointment.getStatus() == AppointmentStatus.CONFIRMED) {
            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointment.setUpdateTimestamp(Instant.now());
            appointment = appointmentRepository.save(appointment);
            // TODO send notification/email
        }
    }

    @Override
    public boolean confirmAppointment(Appointment appointment) {
        if (appointment.getStatus() == AppointmentStatus.CONFIRMED) {
            // Already confirmed
            return true;
        }
        if (appointment.getStatus() != AppointmentStatus.UNCONFIRMED) {
            return false;
        }
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setUpdateTimestamp(Instant.now());
        appointment = appointmentRepository.save(appointment);
        return true;
        // TODO send notification/email
    }

    @Override
    public List<AppointmentModel> getAppointmentsByMonth(Customer customer, YearMonth yearMonth) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        // TODO include time, need timezones
        List<Appointment> appointments = appointmentRepository.findAllByCustomerAndDateBetween(customer, start, end);
        return toModels(appointments);
    }

    @Override
    public List<AppointmentModel> getAppointmentsByMonth(ServiceProvider serviceProvider, YearMonth yearMonth) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        // TODO include time, need timezones
        List<Appointment> appointments = appointmentRepository.findAllByServiceProviderAndDateBetween(serviceProvider, start, end);
        return toModels(appointments);
    }

    @Override
    public List<AppointmentModel> getAppointmentsFor(ServiceRequest serviceRequest) {
        return toModels(serviceRequest.getAppointments());
    }

    @Override
    public List<AppointmentModel> getUnconfirmedAppointments(Customer customer) {
        List<Appointment> appointments = appointmentRepository.findAllByCustomerAndStatus(
                customer,
                AppointmentStatus.UNCONFIRMED,
                Sort.by(Sort.Direction.DESC, "creationTimestamp")
        ); // TODO this needs a limit
        return toModels(appointments);
    }

    @Override
    public List<AppointmentModel> getUpdatedAppointments(ServiceProvider serviceProvider) {
        List<Appointment> appointments = appointmentRepository.findAllByServiceProviderAndStatusIn(
                serviceProvider,
                List.of(AppointmentStatus.CANCELLED, AppointmentStatus.CONFIRMED),
                Sort.by(Sort.Direction.DESC, "updateTimestamp")
        ); // TODO this needs a limit
        return toModels(appointments);
    }

    private List<AppointmentModel> toModels(List<Appointment> appointments) {
        List<AppointmentModel> appointmentModels = new ArrayList<>(appointments.size());
        for (Appointment appointment : appointments) {
            appointmentModels.add(AppointmentModel.fromEntity(appointment));
        }
        return appointmentModels;
    }
}

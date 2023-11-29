package homerep.springy.service.impl;

import homerep.springy.entity.Appointment;
import homerep.springy.entity.Customer;
import homerep.springy.entity.ServiceProvider;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.exception.ConflictingAppointmentException;
import homerep.springy.exception.UnconfirmableAppointmentException;
import homerep.springy.model.appointment.AppointmentModel;
import homerep.springy.model.appointment.AppointmentStatus;
import homerep.springy.model.appointment.CreateAppointmentModel;
import homerep.springy.repository.AppointmentRepository;
import homerep.springy.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {
    @Autowired
    private AppointmentRepository appointmentRepository;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Appointment createAppointment(ServiceProvider serviceProvider, ServiceRequest serviceRequest, CreateAppointmentModel model) throws ConflictingAppointmentException {
        List<Appointment> conflicting = appointmentRepository.findAllByServiceProviderAndStatusInAndPeriodIn(
                serviceProvider,
                List.of(AppointmentStatus.UNCONFIRMED, AppointmentStatus.CONFIRMED),
                model.startTime(), model.endTime()
        );
        if (!conflicting.isEmpty()) {
            throw new ConflictingAppointmentException(conflicting);
        }

        Appointment appointment = new Appointment();
        appointment.setServiceProvider(serviceProvider);
        appointment.setServiceRequest(serviceRequest);
        appointment.setCustomer(serviceRequest.getCustomer());
        appointment.setCreationTimestamp(Instant.now());
        appointment.setStartTime(model.startTime());
        appointment.setEndTime(model.endTime());
        appointment.setStatus(AppointmentStatus.UNCONFIRMED);
        appointment.setMessage(model.message());
        appointment = appointmentRepository.save(appointment);

        serviceRequest.getAppointments().add(appointment);
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
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void confirmAppointment(Appointment appointment) throws ConflictingAppointmentException, UnconfirmableAppointmentException {
        if (appointment.getStatus() == AppointmentStatus.CONFIRMED) {
            // Already confirmed
            return;
        }
        if (appointment.getStatus() != AppointmentStatus.UNCONFIRMED) {
            throw new UnconfirmableAppointmentException();
        }
        List<Appointment> conflicting = appointmentRepository.findAllByCustomerAndStatusAndPeriodIn(
                appointment.getCustomer(),
                AppointmentStatus.CONFIRMED,
                appointment.getStartTime(), appointment.getEndTime()
        );
        if (!conflicting.isEmpty()) {
            throw new ConflictingAppointmentException(conflicting);
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setUpdateTimestamp(Instant.now());
        appointment = appointmentRepository.save(appointment);
        // TODO send notification/email
    }

    @Override
    public List<AppointmentModel> getConflictingCustomerAppointments(Appointment appointment) {
        if (appointment.getStatus() == AppointmentStatus.CONFIRMED) {
            return List.of(); // Already confirmed, there shouldn't be any conflicts
        }
        List<Appointment> conflicting = appointmentRepository.findAllByCustomerAndStatusAndPeriodIn(
                appointment.getCustomer(),
                AppointmentStatus.CONFIRMED,
                appointment.getStartTime(), appointment.getEndTime()
        );
        return toModels(conflicting);
    }

    @Override
    public List<AppointmentModel> getAppointmentsByMonth(Customer customer, YearMonth yearMonth, boolean weekEnds, ZoneId zoneId) {
        InstantPeriod period = getMonthPeriod(yearMonth, weekEnds, zoneId);
        List<Appointment> appointments = appointmentRepository.findAllByCustomerAndPeriodIn(customer, period.start, period.end);
        return toModels(appointments);
    }

    @Override
    public List<AppointmentModel> getAppointmentsByMonth(ServiceProvider serviceProvider, YearMonth yearMonth, boolean weekEnds, ZoneId zoneId) {
        InstantPeriod period = getMonthPeriod(yearMonth, weekEnds, zoneId);
        List<Appointment> appointments = appointmentRepository.findAllByServiceProviderAndPeriodIn(serviceProvider, period.start, period.end);
        return toModels(appointments);
    }

    @Override
    public List<AppointmentModel> getAppointmentsFor(ServiceRequest serviceRequest) {
        return toModels(serviceRequest.getAppointments());
    }

    @Override
    public List<AppointmentModel> getUnconfirmedAppointments(Customer customer) {
        cleanUpOldAppointments();
        List<Appointment> appointments = appointmentRepository.findAllByCustomerAndStatus(
                customer,
                AppointmentStatus.UNCONFIRMED,
                Sort.by(Sort.Direction.DESC, "creationTimestamp")
        ); // TODO this needs a limit
        return toModels(appointments);
    }

    @Override
    public List<AppointmentModel> getUpdatedAppointments(ServiceProvider serviceProvider) {
        cleanUpOldAppointments();
        List<Appointment> appointments = appointmentRepository.findAllByServiceProviderAndStatusIn(
                serviceProvider,
                List.of(AppointmentStatus.CANCELLED, AppointmentStatus.CONFIRMED),
                Sort.by(Sort.Direction.DESC, "updateTimestamp")
        ); // TODO this needs a limit
        return toModels(appointments);
    }

    private void cleanUpOldAppointments() {
        // TODO this feels like a hack, just filter from relevant queries?
        List<Appointment> appointments = appointmentRepository.findAllByStatusInAndEndTimeBefore(
                List.of(AppointmentStatus.CONFIRMED, AppointmentStatus.CANCELLED, AppointmentStatus.UNCONFIRMED),
                Instant.now()
        );
        appointmentRepository.saveAll(appointments);
    }

    private List<AppointmentModel> toModels(List<Appointment> appointments) {
        List<AppointmentModel> appointmentModels = new ArrayList<>(appointments.size());
        for (Appointment appointment : appointments) {
            appointmentModels.add(AppointmentModel.fromEntity(appointment));
        }
        return appointmentModels;
    }

    private record InstantPeriod(
            Instant start,
            Instant end
    ) {
    }

    private InstantPeriod getMonthPeriod(YearMonth yearMonth, boolean weekEnds, ZoneId zoneId) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        if (weekEnds) {
            // Add a week of padding to the start and end
            start = start.minusDays(7);
            end = end.plusDays(7);
        }
        return new InstantPeriod(
                start.atStartOfDay(zoneId).toInstant(),
                ZonedDateTime.of(end, LocalTime.MAX, zoneId).toInstant()
        );
    }
}

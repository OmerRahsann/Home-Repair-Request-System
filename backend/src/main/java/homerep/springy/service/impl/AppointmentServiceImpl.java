package homerep.springy.service.impl;

import homerep.springy.authorities.AccountType;
import homerep.springy.entity.Appointment;
import homerep.springy.entity.Customer;
import homerep.springy.entity.ServiceProvider;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.exception.ConflictingAppointmentException;
import homerep.springy.exception.UnconfirmableAppointmentException;
import homerep.springy.model.appointment.AppointmentModel;
import homerep.springy.model.appointment.AppointmentStatus;
import homerep.springy.model.appointment.CreateAppointmentModel;
import homerep.springy.model.notification.NotificationType;
import homerep.springy.repository.AppointmentRepository;
import homerep.springy.service.AppointmentService;
import homerep.springy.service.EmailService;
import homerep.springy.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AppointmentServiceImpl implements AppointmentService {
    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    private static final ZoneId TIME_ZONE = ZoneId.of("America/New_York"); // TODO this should not be a constant

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

        String period = formatAppointmentPeriod(appointment, TIME_ZONE); // TODO base on provider timezone
        String message = appointment.getMessage() == null ? "" : appointment.getMessage();
        emailService.sendEmail(
                serviceRequest.getCustomer().getAccount().getEmail(),
                "appointment-created",
                Map.of(
                        "service-provider-name", serviceProvider.getName(),
                        "service-request-title", serviceRequest.getTitle(),
                        "appointment-period", period,
                        "appointment-message", message
                )
        );
        notificationService.sendNotification(
                serviceRequest.getCustomer().getAccount(),
                serviceProvider.getName() + " created an appointment",
                "For service request: " + serviceRequest.getTitle() + "\nPeriod: " + period,
                NotificationType.NEW_APPOINTMENT
        );
        return appointment;
    }

    @Override
    public void cancelAppointment(Appointment appointment, AccountType canceller) {
        if (appointment.getStatus() == AppointmentStatus.UNCONFIRMED || appointment.getStatus() == AppointmentStatus.CONFIRMED) {
            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointment.setUpdateTimestamp(Instant.now());
            appointment = appointmentRepository.save(appointment);

            String period = formatAppointmentPeriod(appointment, TIME_ZONE); // TODO base on customer/provider timezone
            String message = appointment.getMessage() == null ? "" : appointment.getMessage();
            switch (canceller) {
                case CUSTOMER -> {
                    Customer customer = appointment.getCustomer();
                    emailService.sendEmail(
                            appointment.getServiceProvider().getAccount().getEmail(),
                            "appointment-cancelled-by-customer",
                            Map.of(
                                    "customer-first-name", customer.getFirstName(),
                                    "customer-last-name", customer.getLastName(),
                                    "service-request-title", appointment.getServiceRequest().getTitle(),
                                    "appointment-period", period,
                                    "appointment-message", message
                            )
                    );
                    notificationService.sendNotification(
                            appointment.getServiceProvider().getAccount(),
                            customer.getFirstName() + " " + customer.getLastName() + " cancelled an appointment",
                            "For service request: " + appointment.getServiceRequest().getTitle() + "\nPeriod: " + period,
                            NotificationType.CANCELLED_APPOINTMENT
                    );
                }
                case SERVICE_PROVIDER -> {
                    emailService.sendEmail(
                            appointment.getCustomer().getAccount().getEmail(),
                            "appointment-cancelled-by-provider",
                            Map.of(
                                    "service-provider-name", appointment.getServiceProvider().getName(),
                                    "service-request-title", appointment.getServiceRequest().getTitle(),
                                    "appointment-period", period,
                                    "appointment-message", message
                            )
                    );
                    notificationService.sendNotification(
                            appointment.getCustomer().getAccount(),
                            appointment.getServiceProvider().getName() + " cancelled an appointment",
                            "For service request: " + appointment.getServiceRequest().getTitle() + "\nPeriod: " + period,
                            NotificationType.CANCELLED_APPOINTMENT
                    );
                }
            }
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

        String period = formatAppointmentPeriod(appointment, TIME_ZONE); // TODO base on customer timezone
        String message = appointment.getMessage() == null ? "" : appointment.getMessage();
        Customer customer = appointment.getCustomer();
        emailService.sendEmail(
                appointment.getServiceProvider().getAccount().getEmail(),
                "appointment-confirmed",
                Map.of(
                        "customer-first-name", customer.getFirstName(),
                        "customer-last-name", customer.getLastName(),
                        "service-request-title", appointment.getServiceRequest().getTitle(),
                        "appointment-period", period,
                        "appointment-message", message
                )
        );
        notificationService.sendNotification(
                appointment.getServiceProvider().getAccount(),
                customer.getFirstName() + " " + customer.getLastName() + " confirmed an appointment",
                "For service request: " + appointment.getServiceRequest().getTitle() + "\nPeriod: " + period,
                NotificationType.CONFIRMED_APPOINTMENT
        );
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

    private String formatAppointmentPeriod(Appointment appointment, ZoneId zoneId) {
        // TODO somehow account for locale in this?
        LocalDateTime start = LocalDateTime.ofInstant(appointment.getStartTime(), zoneId);
        LocalDateTime end = LocalDateTime.ofInstant(appointment.getEndTime(), zoneId);
        if (start.toLocalDate().equals(end.toLocalDate())) {
            // Same day
            return start.toLocalDate() + " ⋅ " + start.toLocalTime() + " - " + end.toLocalTime();
        } else {
            return start.toLocalDate() + " " + start.toLocalTime() + " - " + end.toLocalDate() + " " + end.toLocalTime();
        }
    }
}

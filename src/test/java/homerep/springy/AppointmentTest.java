package homerep.springy;

import com.icegreen.greenmail.spring.GreenMailBean;
import com.icegreen.greenmail.store.FolderException;
import homerep.springy.authorities.AccountType;
import homerep.springy.component.DummyDataComponent;
import homerep.springy.config.TestDatabaseConfig;
import homerep.springy.config.TestMailConfig;
import homerep.springy.config.TestStorageConfig;
import homerep.springy.controller.customer.CustomerAppointmentController;
import homerep.springy.controller.provider.ServiceProviderAppointmentController;
import homerep.springy.entity.*;
import homerep.springy.exception.*;
import homerep.springy.model.appointment.AppointmentModel;
import homerep.springy.model.appointment.AppointmentStatus;
import homerep.springy.model.appointment.CreateAppointmentModel;
import homerep.springy.model.notification.NotificationModel;
import homerep.springy.model.notification.NotificationType;
import homerep.springy.repository.AppointmentRepository;
import homerep.springy.repository.ServiceRequestRepository;
import homerep.springy.service.AppointmentService;
import homerep.springy.service.EmailRequestService;
import homerep.springy.service.NotificationService;
import homerep.springy.type.User;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.*;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestDatabaseConfig
@Import({TestMailConfig.class, TestStorageConfig.class})
public class AppointmentTest {
    @Autowired
    private DummyDataComponent dummyDataComponent;

    @Autowired
    private CustomerAppointmentController customerAppointmentController;

    @Autowired
    private ServiceProviderAppointmentController serviceProviderAppointmentController;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private EmailRequestService emailRequestService;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private GreenMailBean greenMailBean;

    private Customer customer;
    private User customerUser;
    private ServiceProvider serviceProvider;
    private User serviceProviderUser;
    private ServiceRequest serviceRequest;

    private static final String CUSTOMER_EMAIL = "test@localhost";

    private static final String SERVICE_PROVIDER_EMAIL = "example@example.com";

    private static final ZoneId TIME_ZONE = ZoneId.of("America/New_York");

    @BeforeEach
    void setup() throws FolderException {
        greenMailBean.getGreenMail().purgeEmailFromAllMailboxes();

        customer = dummyDataComponent.createCustomer(CUSTOMER_EMAIL);
        customerUser = new User(customer.getAccount());
        serviceRequest = dummyDataComponent.createServiceRequest(customer);
        serviceProvider = dummyDataComponent.createServiceProvider(SERVICE_PROVIDER_EMAIL);
        serviceProviderUser = new User(serviceProvider.getAccount());
    }

    private CreateAppointmentModel appointmentAt(LocalTime startTime, Duration duration) {
        LocalDate date = LocalDate.now().plusDays(2);
        Instant start = ZonedDateTime.of(date, startTime, TIME_ZONE).toInstant();
        Instant end = start.plus(duration);
        return new CreateAppointmentModel(
                start,
                end,
                dummyDataComponent.generateDummySentence()
        );
    }

    @Test
    void createAppointment() throws ConflictingAppointmentException {
        Instant start = Instant.now();
        CreateAppointmentModel model = appointmentAt(LocalTime.NOON, Duration.ofHours(2));
        // Create an appointment
        Appointment appointment = appointmentService.createAppointment(serviceProvider, serviceRequest, model);
        assertNotNull(appointment);
        // Appointment is stored in the repository with the correct information
        assertTrue(appointmentRepository.findById(appointment.getId()).isPresent());
        assertEquals(serviceProvider, appointment.getServiceProvider());
        assertEquals(serviceRequest, appointment.getServiceRequest());
        assertEquals(customer, appointment.getCustomer());
        // creationTimestamp is filled out and has a sensible time
        assertNotNull(appointment.getCreationTimestamp());
        assertTrue(appointment.getCreationTimestamp().isAfter(start));
        // updateTimestamp starts out as null
        assertNull(appointment.getUpdateTimestamp());
        assertEquals(model.startTime(), appointment.getStartTime());
        assertEquals(model.endTime(), appointment.getEndTime());
        // Appointment start as UNCONFIRMED
        assertEquals(AppointmentStatus.UNCONFIRMED, appointment.getStatus());
        assertEquals(model.message(), appointment.getMessage());
    }

    private static List<Duration> provideConflictingOffsets() {
        return List.of(Duration.ofMinutes(0), Duration.ofMinutes(30), Duration.ofHours(2).minusSeconds(1));
    }

    @ParameterizedTest
    @MethodSource("provideConflictingOffsets")
    void createConflictingAppointment(Duration offset) throws ConflictingAppointmentException, UnconfirmableAppointmentException {
        CreateAppointmentModel model = appointmentAt(LocalTime.NOON, Duration.ofHours(2));
        // Create an appointment
        Appointment appointment = appointmentService.createAppointment(serviceProvider, serviceRequest, model);
        assertNotNull(appointment);
        // Trying to create an appointment conflicting with the time period fails
        CreateAppointmentModel offsetModel = appointmentAt(LocalTime.NOON.plus(offset), Duration.ofHours(2));
        ConflictingAppointmentException exception = assertThrows(ConflictingAppointmentException.class,
                () -> appointmentService.createAppointment(serviceProvider, serviceRequest, offsetModel));
        // due to the first appointment
        assertEquals(1, exception.getConflictingAppointments().size());
        assertEquals(appointment.getId(), exception.getConflictingAppointments().get(0).getId());
        // No other appointments were created
        assertEquals(1, appointmentRepository.findAll().size());

        // Accept the appointment
        appointmentService.confirmAppointment(appointment);
        // Trying to create an appointment conflicting with the time period still fails
        exception = assertThrows(ConflictingAppointmentException.class, () -> appointmentService.createAppointment(serviceProvider, serviceRequest, offsetModel));
        // due to the first appointment
        assertEquals(1, exception.getConflictingAppointments().size());
        assertEquals(appointment.getId(), exception.getConflictingAppointments().get(0).getId());

        // Cancel the appointment to free up the time slot
        appointmentService.cancelAppointment(appointment, AccountType.SERVICE_PROVIDER);
        // Creating the other appointment is successful
        assertDoesNotThrow(() -> appointmentService.createAppointment(serviceProvider, serviceRequest, offsetModel));
        assertEquals(2, appointmentRepository.findAll().size());
    }

    @Test
    void createCancelAppointment() throws ConflictingAppointmentException {
        CreateAppointmentModel model = appointmentAt(LocalTime.NOON, Duration.ofHours(2));
        // Create an appointment
        Appointment appointment = appointmentService.createAppointment(serviceProvider, serviceRequest, model);
        assertNotNull(appointment);
        // Cancel the appointment
        appointmentService.cancelAppointment(appointment, AccountType.SERVICE_PROVIDER);
        // Status is updated
        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
        // updateTimestamp is populated with the current time
        Instant updateTimestamp = appointment.getUpdateTimestamp();
        assertNotNull(updateTimestamp);
        assertTrue(updateTimestamp.isAfter(appointment.getCreationTimestamp()));
        // Cancelling a second time does nothing for the service provider
        appointmentService.cancelAppointment(appointment, AccountType.SERVICE_PROVIDER);
        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
        assertEquals(updateTimestamp, appointment.getUpdateTimestamp());
        // cancelling a second time does nothing for the customer
        appointmentService.cancelAppointment(appointment, AccountType.CUSTOMER);
        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
        assertEquals(updateTimestamp, appointment.getUpdateTimestamp());
    }

    @Test
    void createConfirmAppointment() throws ConflictingAppointmentException, UnconfirmableAppointmentException {
        CreateAppointmentModel model = appointmentAt(LocalTime.NOON, Duration.ofHours(2));
        // Create an appointment
        Appointment appointment = appointmentService.createAppointment(serviceProvider, serviceRequest, model);
        assertNotNull(appointment);
        // Confirm the appointment
        appointmentService.confirmAppointment(appointment);
        // Status is updated
        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
        // updateTimestamp is populated with the current time
        Instant updateTimestamp = appointment.getUpdateTimestamp();
        assertNotNull(updateTimestamp);
        assertTrue(updateTimestamp.isAfter(appointment.getCreationTimestamp()));
        // Confirming a second time does nothing
        appointmentService.confirmAppointment(appointment);
        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
        assertEquals(updateTimestamp, appointment.getUpdateTimestamp());
    }

    @Test
    void cancelConfirmedAppointment() throws ConflictingAppointmentException, UnconfirmableAppointmentException {
        CreateAppointmentModel model = appointmentAt(LocalTime.NOON, Duration.ofHours(2));
        // Create an appointment
        Appointment appointment = appointmentService.createAppointment(serviceProvider, serviceRequest, model);
        assertNotNull(appointment);
        // Confirm the appointment
        appointmentService.confirmAppointment(appointment);
        // Status is updated
        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
        // updateTimestamp is populated with the current time
        Instant updateTimestamp = appointment.getUpdateTimestamp();
        assertNotNull(updateTimestamp);
        assertTrue(updateTimestamp.isAfter(appointment.getCreationTimestamp()));

        // Cancelling the appointment works
        appointmentService.cancelAppointment(appointment, AccountType.CUSTOMER);
        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
        // updateTimestamp is updated again
        assertTrue(appointment.getUpdateTimestamp().isAfter(updateTimestamp));
    }

    @Test
    void confirmInvalidAppointment() throws ConflictingAppointmentException {
        CreateAppointmentModel model = appointmentAt(LocalTime.NOON, Duration.ofHours(2));
        // Create an appointment
        Appointment appointment = appointmentService.createAppointment(serviceProvider, serviceRequest, model);
        assertNotNull(appointment);
        // Cancel the appointment
        appointmentService.cancelAppointment(appointment, AccountType.SERVICE_PROVIDER);
        // updateTimestamp is populated
        Instant updateTimestamp = appointment.getUpdateTimestamp();
        assertNotNull(updateTimestamp);
        assertTrue(updateTimestamp.isAfter(appointment.getCreationTimestamp()));
        // Trying to confirm a CANCELLED, EXPIRED, or COMPLETED appointment fails
        AppointmentStatus[] notConfirmableStatuses = new AppointmentStatus[]{AppointmentStatus.CANCELLED, AppointmentStatus.EXPIRED, AppointmentStatus.COMPLETED};
        for (AppointmentStatus status : notConfirmableStatuses) {
            appointment.setStatus(status);
            // Confirming does not work
            assertThrows(UnconfirmableAppointmentException.class, () -> appointmentService.confirmAppointment(appointment));
            // Status is unchanged
            assertEquals(status, appointment.getStatus());
            // updateTimestamp is not changed
            assertEquals(updateTimestamp, appointment.getUpdateTimestamp());
        }
    }

    private Map<YearMonth, List<Appointment>> createAppointmentsByMonth() {
        Map<YearMonth, List<Appointment>> appointmentsByMonth = new HashMap<>();
        for (int i = 0; i <= 12; i++) {
            YearMonth yearMonth = YearMonth.now().plusMonths(i);
            appointmentsByMonth.put(yearMonth, dummyDataComponent.createAppointmentsFor(serviceProvider, serviceRequest, yearMonth, TIME_ZONE));
        }
        return appointmentsByMonth;
    }

    @Test
    @Transactional
    void customerGetAppointmentsByMonth() {
        // Generate a bunch of appointments over a 12-month period
        Map<YearMonth, List<Appointment>> appointmentsByMonth = createAppointmentsByMonth();
        // For each month
        for (Map.Entry<YearMonth, List<Appointment>> entry : appointmentsByMonth.entrySet()) {
            // Check that the appointments queried for that month matches the generated appointments
            YearMonth yearMonth = entry.getKey();
            List<AppointmentModel> expectedAppointments = entry.getValue().stream()
                    .sorted(Comparator.comparing(Appointment::getId)) // Sort by id so that indices can match
                    .map(AppointmentModel::fromEntity)
                    .toList();
            List<AppointmentModel> actualAppointments = appointmentService.getAppointmentsByMonth(customer, yearMonth, false, TIME_ZONE);
            actualAppointments.sort(Comparator.comparing(AppointmentModel::appointmentId)); // Sort by id so that indices can match
            assertEquals(expectedAppointments, actualAppointments);
        }
    }

    @Test
    @Transactional
    void serviceProviderGetAppointmentsByMonth() {
        // Generate a bunch of appointments over a 12-month period
        Map<YearMonth, List<Appointment>> appointmentsByMonth = createAppointmentsByMonth();
        // For each month
        for (Map.Entry<YearMonth, List<Appointment>> entry : appointmentsByMonth.entrySet()) {
            // Check that the appointments queried for that month matches the generated appointments
            YearMonth yearMonth = entry.getKey();
            List<AppointmentModel> expectedAppointments = entry.getValue().stream()
                    .sorted(Comparator.comparing(Appointment::getId)) // Sort by id so that indices can match
                    .map(AppointmentModel::fromEntity)
                    .toList();
            List<AppointmentModel> actualAppointments = appointmentService.getAppointmentsByMonth(serviceProvider, yearMonth, false, TIME_ZONE);
            actualAppointments.sort(Comparator.comparing(AppointmentModel::appointmentId)); // Sort by id so that indices can match
            assertArrayEquals(expectedAppointments.toArray(), actualAppointments.toArray());
        }
    }

    @Test
    @Transactional
    void getAppointmentsWithPadding() {
        YearMonth yearMonth = YearMonth.now().plusMonths(4);

        Instant periodStart = yearMonth.atDay(1).minusDays(7).atStartOfDay(TIME_ZONE).toInstant();
        CreateAppointmentModel startModel = new CreateAppointmentModel(
                periodStart,
                periodStart.plus(dummyDataComponent.randomDuration()),
                dummyDataComponent.generateDummySentence()
        );
        assertDoesNotThrow(() -> appointmentService.createAppointment(serviceProvider, serviceRequest, startModel));

        Instant periodEnd = ZonedDateTime.of(yearMonth.atEndOfMonth().plusDays(7), LocalTime.MAX, TIME_ZONE).toInstant();
        CreateAppointmentModel endModel = new CreateAppointmentModel(
                periodEnd.minus(dummyDataComponent.randomDuration()),
                periodEnd,
                dummyDataComponent.generateDummySentence()
        );
        assertDoesNotThrow(() -> appointmentService.createAppointment(serviceProvider, serviceRequest, endModel));

        // Appointments are not included if we don't include week ends for the customer
        List<AppointmentModel> appointments = appointmentService.getAppointmentsByMonth(customer, yearMonth, false, TIME_ZONE);
        assertTrue(appointments.isEmpty());
        // and the service provider
        appointments = appointmentService.getAppointmentsByMonth(serviceProvider, yearMonth, false, TIME_ZONE);
        assertTrue(appointments.isEmpty());
        // Appointments in the week before and after a month are included if we include week ends
        appointments = appointmentService.getAppointmentsByMonth(customer, yearMonth, true, TIME_ZONE);
        assertEquals(2, appointments.size());
        // and the service provider
        appointments = appointmentService.getAppointmentsByMonth(serviceProvider, yearMonth, true, TIME_ZONE);
        assertEquals(2, appointments.size());
    }

    @Test
    @Transactional
    void customerGetUnconfirmedAppointments() {
        YearMonth nextMonth = YearMonth.now().plusMonths(1);
        // Generate unconfirmed appointments
        List<Appointment> appointments = dummyDataComponent.createAppointmentsFor(serviceProvider, serviceRequest, nextMonth, TIME_ZONE);
        // Generate confirmed and cancelled appointments to make sure they're being filtered out
        List<Appointment> confirmedAppointments = dummyDataComponent.createAppointmentsFor(serviceProvider, serviceRequest, nextMonth, TIME_ZONE);
        confirmedAppointments.forEach(x -> assertDoesNotThrow(() -> appointmentService.confirmAppointment(x)));
        List<Appointment> cancelledAppointments = dummyDataComponent.createAppointmentsFor(serviceProvider, serviceRequest, nextMonth, TIME_ZONE);
        cancelledAppointments.forEach(appointment -> appointmentService.cancelAppointment(appointment, AccountType.SERVICE_PROVIDER));

        AppointmentModel[] expectedModels = appointments.stream()
                // Make sure unconfirmed appointments are sorted by creationTimestamp descending
                .sorted(Comparator.comparing(Appointment::getCreationTimestamp).reversed())
                .map(AppointmentModel::fromEntity)
                .toArray(AppointmentModel[]::new);
        List<AppointmentModel> actualModels = appointmentService.getUnconfirmedAppointments(customer);
        assertArrayEquals(expectedModels, actualModels.toArray(AppointmentModel[]::new));
    }

    @Test
    @Transactional
    void serviceProviderGetUpdatedAppointments() {
        YearMonth nextMonth = YearMonth.now().plusMonths(1);
        // Generate unconfirmed appointments to make sure they're being filtered out
        dummyDataComponent.createAppointmentsFor(serviceProvider, serviceRequest, nextMonth, TIME_ZONE);
        // Generate confirmed and cancelled appointments
        List<Appointment> confirmedAppointments = dummyDataComponent.createAppointmentsFor(serviceProvider, serviceRequest, nextMonth, TIME_ZONE);
        confirmedAppointments.forEach(x -> assertDoesNotThrow(() -> appointmentService.confirmAppointment(x)));
        List<Appointment> cancelledAppointments = dummyDataComponent.createAppointmentsFor(serviceProvider, serviceRequest, nextMonth, TIME_ZONE);
        cancelledAppointments.forEach(appointment -> appointmentService.cancelAppointment(appointment, AccountType.SERVICE_PROVIDER));

        AppointmentModel[] expectedModels = Stream.concat(confirmedAppointments.stream(), cancelledAppointments.stream())
                // Make sure confirmed & cancelled appointments are sorted by updateTimestamp descending
                .sorted(Comparator.comparing(Appointment::getUpdateTimestamp).reversed())
                .map(AppointmentModel::fromEntity)
                .toArray(AppointmentModel[]::new);
        List<AppointmentModel> actualModels = appointmentService.getUpdatedAppointments(serviceProvider);
        assertArrayEquals(expectedModels, actualModels.toArray(AppointmentModel[]::new));
    }

    @Test
    @Transactional
    void getAppointmentsForServiceRequest() {
        YearMonth nextMonth = YearMonth.now().plusMonths(1);
        List<Appointment> appointments = dummyDataComponent.createAppointmentsFor(serviceProvider, serviceRequest, nextMonth, TIME_ZONE);
        AppointmentModel[] expectedModels = appointments.stream()
                .sorted(Comparator.comparing(Appointment::getId)) // Sort by id so that indices can match
                .map(AppointmentModel::fromEntity)
                .toArray(AppointmentModel[]::new);

        serviceRequest = serviceRequestRepository.findById(serviceRequest.getId()).orElseThrow();
        List<AppointmentModel> actualModels = appointmentService.getAppointmentsFor(serviceRequest);
        actualModels.sort(Comparator.comparing(AppointmentModel::appointmentId)); // Sort by id so that indices can match
        assertArrayEquals(expectedModels, actualModels.toArray(AppointmentModel[]::new));
    }

    @Test
    @Transactional
    void conflictingAppointments() throws ConflictingAppointmentException, UnconfirmableAppointmentException {
        ServiceProvider otherServiceProvider = dummyDataComponent.createServiceProvider("example2@example.com");

        CreateAppointmentModel model = appointmentAt(LocalTime.NOON, Duration.ofHours(2));
        // Create 2 appointments from 2 different service providers on the same day
        Appointment appointmentA = appointmentService.createAppointment(serviceProvider, serviceRequest, model);
        Appointment appointmentB = appointmentService.createAppointment(otherServiceProvider, serviceRequest, model);
        // There's no conflicting appointments as nothing is confirmed yet
        List<AppointmentModel> conflicting = appointmentService.getConflictingCustomerAppointments(appointmentA);
        assertTrue(conflicting.isEmpty());
        // Confirm appointmentA
        appointmentService.confirmAppointment(appointmentA);
        // There's no appointments conflicting with appointmentA
        conflicting = appointmentService.getConflictingCustomerAppointments(appointmentA);
        assertTrue(conflicting.isEmpty());
        // appointmentA conflicts with appointmentB
        conflicting = appointmentService.getConflictingCustomerAppointments(appointmentB);
        assertEquals(1, conflicting.size());
        assertEquals(AppointmentModel.fromEntity(appointmentA), conflicting.get(0));

        // Attempting to confirm appointmentB fails due to the conflict with appointmentA
        ConflictingAppointmentException exception = assertThrows(ConflictingAppointmentException.class,
                () -> appointmentService.confirmAppointment(appointmentB));
        assertEquals(1, exception.getConflictingAppointments().size());
        assertEquals(appointmentA.getId(), exception.getConflictingAppointments().get(0).getId());
    }

    @Test
    void nonExistentAppointment() {
        assertThrows(NonExistentAppointmentException.class,
                () -> customerAppointmentController.getAppointment(Integer.MAX_VALUE, customerUser));
        assertThrows(NonExistentAppointmentException.class,
                () -> customerAppointmentController.getConflictingAppointments(Integer.MAX_VALUE, customerUser));
        assertThrows(NonExistentAppointmentException.class,
                () -> customerAppointmentController.confirmAppointment(Integer.MAX_VALUE, customerUser));
        assertThrows(NonExistentAppointmentException.class,
                () -> customerAppointmentController.cancelAppointment(Integer.MAX_VALUE, customerUser));

        assertThrows(NonExistentAppointmentException.class,
                () -> serviceProviderAppointmentController.getAppointment(Integer.MAX_VALUE, serviceProviderUser));
        assertThrows(NonExistentAppointmentException.class,
                () -> serviceProviderAppointmentController.cancelAppointment(Integer.MAX_VALUE, serviceProviderUser));
    }

    @Test
    void nonExistentPost() {
        assertThrows(NonExistentPostException.class,
                () -> customerAppointmentController.getAppointmentsFor(Integer.MAX_VALUE, customerUser));
        assertThrows(NonExistentPostException.class,
                () -> serviceProviderAppointmentController.createAppointment(Integer.MAX_VALUE, null, serviceProviderUser));
    }

    @Test
    @Transactional
    void requireConfirmedEmailRequest() {
        CreateAppointmentModel model = appointmentAt(LocalTime.NOON, Duration.ofHours(2));
        // Trying to create an appointment for a service request without an accepted email request fails
        ApiException exception = assertThrows(ApiException.class,
                () -> serviceProviderAppointmentController.createAppointment(serviceRequest.getId(), model, serviceProviderUser));
        assertEquals("missing_accepted_email_request", exception.getType());

        // Send an email request
        assertTrue(emailRequestService.sendEmailRequest(serviceRequest, serviceProvider));
        // Email request has not been accepted yet, so creating an appointment should still fail
        exception = assertThrows(ApiException.class,
                () -> serviceProviderAppointmentController.createAppointment(serviceRequest.getId(), model, serviceProviderUser));
        assertEquals("missing_accepted_email_request", exception.getType());

        // Accept the email request
        assertTrue(emailRequestService.acceptEmailRequest(serviceRequest.getEmailRequests().iterator().next()));
        // Creating an appointment works
        assertDoesNotThrow(() -> serviceProviderAppointmentController.createAppointment(serviceRequest.getId(), model, serviceProviderUser));

        // Reject the email request
        emailRequestService.rejectEmailRequest(serviceRequest.getEmailRequests().iterator().next());
        // Trying to create an appointment fails
        exception = assertThrows(ApiException.class,
                () -> serviceProviderAppointmentController.createAppointment(serviceRequest.getId(), model, serviceProviderUser));
        assertEquals("missing_accepted_email_request", exception.getType());
    }

    @Test
    void customerConfirmCancelledAppointment() throws ConflictingAppointmentException {
        CreateAppointmentModel model = appointmentAt(LocalTime.NOON, Duration.ofHours(2));
        // Create an UNCONFIRMED appointment
        Appointment appointment = appointmentService.createAppointment(serviceProvider, serviceRequest, model);
        // cancel it
        appointmentService.cancelAppointment(appointment, AccountType.SERVICE_PROVIDER);
        // When the customer tries to confirm it, it fails
        assertThrows(UnconfirmableAppointmentException.class,
                () -> customerAppointmentController.confirmAppointment(serviceRequest.getId(), customerUser));
    }

    @Test
    void createAppointmentValidation() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        // endTime > startTime, startTime is in the past, endTime is in the past
        CreateAppointmentModel model = new CreateAppointmentModel(
                Instant.now().minusSeconds(30),
                Instant.now().minusSeconds(60),
                null
        );
        Set<ConstraintViolation<CreateAppointmentModel>> violations = validator.validate(model);
        assertEquals(3, violations.size());
        // startTime is in the past, endTime is in the past
        model = new CreateAppointmentModel(
                Instant.now().minusSeconds(60),
                Instant.now().minusSeconds(30),
                null
        );
        violations = validator.validate(model);
        assertEquals(2, violations.size());
        // Valid
        model = new CreateAppointmentModel(
                Instant.now().plusSeconds(60),
                Instant.now().plusSeconds(120),
                null
        );
        violations = validator.validate(model);
        assertTrue(violations.isEmpty());
    }

    private String getAppointmentPeriod(Instant start, Instant end) {
        LocalDateTime localStart = LocalDateTime.ofInstant(start, TIME_ZONE);
        LocalDateTime localEnd = LocalDateTime.ofInstant(end, TIME_ZONE);
        if (localStart.toLocalDate().equals(localEnd.toLocalDate())) {
            return localStart.toLocalDate() + " ⋅ " + localStart.toLocalTime() + " - " + localEnd.toLocalTime();
        } else {
            return localStart.toLocalDate() + " " + localStart.toLocalTime() + " - " + localEnd.toLocalDate() + " " + localEnd.toLocalTime();
        }
    }

    @Test
    void createAppointmentEmailNotification() throws Exception {
        assertEquals(0, greenMailBean.getReceivedMessages().length);
        CreateAppointmentModel model = appointmentAt(LocalTime.NOON, Duration.ofHours(2));
        // Create an appointment
        Appointment appointment = appointmentService.createAppointment(serviceProvider, serviceRequest, model);
        assertNotNull(appointment);
        // an email notification is sent to the customer
        assertEquals(1, greenMailBean.getReceivedMessages().length);
        MimeMessage message = greenMailBean.getReceivedMessages()[0];
        // to their registered email
        assertEquals(1, message.getAllRecipients().length);
        assertEquals(CUSTOMER_EMAIL, message.getAllRecipients()[0].toString());
        assertTrue(message.getContent() instanceof String);
        String content = (String) message.getContent();
        // with the service provider's name
        assertTrue(content.contains(serviceProvider.getName()));
        // the title of the service request
        assertTrue(content.contains(serviceRequest.getTitle()));
        // the period
        assertTrue(content.contains(getAppointmentPeriod(appointment.getStartTime(), appointment.getEndTime())));
        // and appointment message
        assertTrue(content.contains(appointment.getMessage()));
    }

    @Test
    void createAppointmentWebNotification() throws Exception {
        assertTrue(notificationService.getNotifications(customer.getAccount()).isEmpty());
        CreateAppointmentModel model = appointmentAt(LocalTime.NOON, Duration.ofHours(2));
        // Create an appointment
        Appointment appointment = appointmentService.createAppointment(serviceProvider, serviceRequest, model);
        assertNotNull(appointment);
        // a web notification is sent to the customer
        List<NotificationModel> notifications = notificationService.getNotifications(customer.getAccount());
        assertEquals(1, notifications.size());
        NotificationModel notification = notifications.get(0);
        // title has the service provider's name
        assertTrue(notification.title().contains(serviceProvider.getName()));
        // title specifies that a new appointment was created
        assertTrue(notification.title().contains("created"));
        // the message has the title of the service request
        assertTrue(notification.message().contains(serviceRequest.getTitle()));
        // and the period
        assertTrue(notification.message().contains(getAppointmentPeriod(appointment.getStartTime(), appointment.getEndTime())));
        // type is NEW_APPOINTMENT
        assertEquals(NotificationType.NEW_APPOINTMENT, notification.type());
    }

    @ParameterizedTest
    @EnumSource(value = AccountType.class)
    void cancelAppointmentEmailNotifications(AccountType canceller) throws Exception {
        CreateAppointmentModel model = appointmentAt(LocalTime.NOON, Duration.ofHours(2));
        // Create an appointment
        Appointment appointment = appointmentService.createAppointment(serviceProvider, serviceRequest, model);
        assertNotNull(appointment);
        // Ignore unrelated emails
        greenMailBean.getGreenMail().purgeEmailFromAllMailboxes();
        assertEquals(0, greenMailBean.getReceivedMessages().length);
        // cancel the appointment
        appointmentService.cancelAppointment(appointment, canceller);
        // an email notification is sent to the other party
        String receiver = canceller == AccountType.CUSTOMER ? SERVICE_PROVIDER_EMAIL : CUSTOMER_EMAIL;
        assertEquals(1, greenMailBean.getReceivedMessages().length);
        MimeMessage message = greenMailBean.getReceivedMessages()[0];
        // to their registered email
        assertEquals(1, message.getAllRecipients().length);
        assertEquals(receiver, message.getAllRecipients()[0].toString());
        assertTrue(message.getContent() instanceof String);
        String content = (String) message.getContent();
        // with the title of the service request
        assertTrue(content.contains(serviceRequest.getTitle()));
        // the period
        assertTrue(content.contains(getAppointmentPeriod(appointment.getStartTime(), appointment.getEndTime())));
        // and appointment message
        assertTrue(content.contains(appointment.getMessage()));

        if (canceller == AccountType.CUSTOMER) {
            // when cancelled by the customer it has the customer's name
            assertTrue(content.contains(customer.getFirstName()));
            assertTrue(content.contains(customer.getLastName()));
        } else {
            // when cancelled by the service provider it has the service provider's name
            assertTrue(content.contains(serviceProvider.getName()));
        }
    }

    @ParameterizedTest
    @EnumSource(value = AccountType.class)
    @Transactional
    void cancelAppointmentWebNotifications(AccountType canceller) throws Exception {
        CreateAppointmentModel model = appointmentAt(LocalTime.NOON, Duration.ofHours(2));
        // Create an appointment
        Appointment appointment = appointmentService.createAppointment(serviceProvider, serviceRequest, model);
        assertNotNull(appointment);
        // Ignore other notifications
        notificationService.clearNotifications(customer.getAccount());
        notificationService.clearNotifications(serviceProvider.getAccount());
        // cancel the appointment
        appointmentService.cancelAppointment(appointment, canceller);
        // a web notification is sent to the other party
        Account receiver = canceller == AccountType.CUSTOMER ? serviceProvider.getAccount() : customer.getAccount();
        List<NotificationModel> notifications = notificationService.getNotifications(receiver);
        assertEquals(1, notifications.size());
        NotificationModel notification = notifications.get(0);
        // The title has the name of the canceller
        if (canceller == AccountType.CUSTOMER) {
            assertTrue(notification.title().contains(customer.getFirstName()));
            assertTrue(notification.title().contains(customer.getLastName()));
        } else {
            assertTrue(notification.title().contains(serviceProvider.getName()));
        }
        // title specifies an appointment was cancelled
        assertTrue(notification.title().contains("cancelled"));
        // message contains the title of the service request
        assertTrue(notification.message().contains(serviceRequest.getTitle()));
        // and the period
        assertTrue(notification.message().contains(getAppointmentPeriod(appointment.getStartTime(), appointment.getEndTime())));
        // type is CANCELLED_APPOINTMENT
        assertEquals(NotificationType.CANCELLED_APPOINTMENT, notification.type());
    }

    @Test
    void confirmAppointmentEmailNotification() throws Exception {
        CreateAppointmentModel model = appointmentAt(LocalTime.NOON, Duration.ofHours(2));
        // Create an appointment
        Appointment appointment = appointmentService.createAppointment(serviceProvider, serviceRequest, model);
        assertNotNull(appointment);
        // Ignore other emails
        greenMailBean.getGreenMail().purgeEmailFromAllMailboxes();
        assertEquals(0, greenMailBean.getReceivedMessages().length);
        // Confirm the appointment
        appointmentService.confirmAppointment(appointment);
        // an email notification is sent to the service provider
        assertEquals(1, greenMailBean.getReceivedMessages().length);
        MimeMessage message = greenMailBean.getReceivedMessages()[0];
        // to their registered email
        assertEquals(1, message.getAllRecipients().length);
        assertEquals(SERVICE_PROVIDER_EMAIL, message.getAllRecipients()[0].toString());
        assertTrue(message.getContent() instanceof String);
        String content = (String) message.getContent();
        // with the customer's name
        assertTrue(content.contains(customer.getFirstName()));
        assertTrue(content.contains(customer.getLastName()));
        // the title of the service request
        assertTrue(content.contains(serviceRequest.getTitle()));
        // the period
        assertTrue(content.contains(getAppointmentPeriod(appointment.getStartTime(), appointment.getEndTime())));
        // and appointment message
        assertTrue(content.contains(appointment.getMessage()));
    }

    @Test
    void confirmAppointmentWebNotification() throws Exception {
        CreateAppointmentModel model = appointmentAt(LocalTime.NOON, Duration.ofHours(2));
        // Create an appointment
        Appointment appointment = appointmentService.createAppointment(serviceProvider, serviceRequest, model);
        assertNotNull(appointment);
        // Ignore other notifications
        notificationService.clearNotifications(serviceProvider.getAccount());
        // Confirm the appointment
        appointmentService.confirmAppointment(appointment);
        // a web notification is sent to the service provider
        List<NotificationModel> notifications = notificationService.getNotifications(serviceProvider.getAccount());
        assertEquals(1, notifications.size());
        NotificationModel notification = notifications.get(0);
        // the title has the customer's name
        assertTrue(notification.title().contains(customer.getFirstName()));
        assertTrue(notification.title().contains(customer.getLastName()));
        // title specifies an appointment was confirmed
        assertTrue(notification.title().contains("confirmed"));
        // the message has the title of the service request
        assertTrue(notification.message().contains(serviceRequest.getTitle()));
        // and the period
        assertTrue(notification.message().contains(getAppointmentPeriod(appointment.getStartTime(), appointment.getEndTime())));
        // type is CONFIRMED_APPOINTMENT
        assertEquals(NotificationType.CONFIRMED_APPOINTMENT, notification.type());
    }
}

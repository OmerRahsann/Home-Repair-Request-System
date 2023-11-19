package homerep.springy;

import homerep.springy.authorities.AccountType;
import homerep.springy.authorities.Verified;
import homerep.springy.component.DummyDataComponent;
import homerep.springy.config.TestDatabaseConfig;
import homerep.springy.config.TestStorageConfig;
import homerep.springy.controller.customer.CustomerAppointmentController;
import homerep.springy.controller.provider.ServiceProviderAppointmentController;
import homerep.springy.entity.Appointment;
import homerep.springy.entity.Customer;
import homerep.springy.entity.ServiceProvider;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.exception.ApiException;
import homerep.springy.exception.NonExistentAppointmentException;
import homerep.springy.exception.NonExistentPostException;
import homerep.springy.model.appointment.AppointmentModel;
import homerep.springy.model.appointment.AppointmentStatus;
import homerep.springy.model.appointment.CreateAppointmentModel;
import homerep.springy.repository.AppointmentRepository;
import homerep.springy.repository.ServiceRequestRepository;
import homerep.springy.service.AppointmentService;
import homerep.springy.service.EmailRequestService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestDatabaseConfig
@Import(TestStorageConfig.class)
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

    private Customer customer;
    private ServiceProvider serviceProvider;
    private ServiceRequest serviceRequest;

    private static final String CUSTOMER_EMAIL = "test@localhost";
    private static final User CUSTOMER_USER = new User(CUSTOMER_EMAIL, "", List.of(AccountType.CUSTOMER, Verified.INSTANCE));

    private static final String SERVICE_PROVIDER_EMAIL = "example@example.com";
    private static final User SERVICE_PROVIDER_USER = new User(SERVICE_PROVIDER_EMAIL, "", List.of(AccountType.SERVICE_PROVIDER, Verified.INSTANCE));

    @BeforeEach
    void setup() {
        customer = dummyDataComponent.createCustomer(CUSTOMER_EMAIL);
        serviceRequest = dummyDataComponent.createServiceRequest(customer);
        serviceProvider = dummyDataComponent.createServiceProvider(SERVICE_PROVIDER_EMAIL);
    }

    @Test
    void createAppointment() {
        Instant start = Instant.now();
        LocalDate date = LocalDate.now().plusDays(2);
        CreateAppointmentModel model = new CreateAppointmentModel(
                date,
                dummyDataComponent.generateDummySentence()
        );
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
        assertEquals(date, appointment.getDate());
        // Appointment start as UNCONFIRMED
        assertEquals(AppointmentStatus.UNCONFIRMED, appointment.getStatus());
        assertEquals(model.message(), appointment.getMessage());
    }

    @Test
    void createCancelAppointment() {
        LocalDate date = LocalDate.now().plusDays(2);
        CreateAppointmentModel model = new CreateAppointmentModel(
                date,
                dummyDataComponent.generateDummySentence()
        );
        // Create an appointment
        Appointment appointment = appointmentService.createAppointment(serviceProvider, serviceRequest, model);
        assertNotNull(appointment);
        // Cancel the appointment
        appointmentService.cancelAppointment(appointment);
        // Status is updated
        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
        // updateTimestamp is populated with the current time
        Instant updateTimestamp = appointment.getUpdateTimestamp();
        assertNotNull(updateTimestamp);
        assertTrue(updateTimestamp.isAfter(appointment.getCreationTimestamp()));
        // Cancelling a second time does nothing
        appointmentService.cancelAppointment(appointment);
        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
        assertEquals(updateTimestamp, appointment.getUpdateTimestamp());
    }

    @Test
    void createConfirmAppointment() {
        LocalDate date = LocalDate.now().plusDays(2);
        CreateAppointmentModel model = new CreateAppointmentModel(
                date,
                dummyDataComponent.generateDummySentence()
        );
        // Create an appointment
        Appointment appointment = appointmentService.createAppointment(serviceProvider, serviceRequest, model);
        assertNotNull(appointment);
        // Confirm the appointment
        assertTrue(appointmentService.confirmAppointment(appointment));
        // Status is updated
        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
        // updateTimestamp is populated with the current time
        Instant updateTimestamp = appointment.getUpdateTimestamp();
        assertNotNull(updateTimestamp);
        assertTrue(updateTimestamp.isAfter(appointment.getCreationTimestamp()));
        // Confirming a second time does nothing
        assertTrue(appointmentService.confirmAppointment(appointment));
        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
        assertEquals(updateTimestamp, appointment.getUpdateTimestamp());
    }

    @Test
    void cancelConfirmedAppointment() {
        LocalDate date = LocalDate.now().plusDays(2);
        CreateAppointmentModel model = new CreateAppointmentModel(
                date,
                dummyDataComponent.generateDummySentence()
        );
        // Create an appointment
        Appointment appointment = appointmentService.createAppointment(serviceProvider, serviceRequest, model);
        assertNotNull(appointment);
        // Confirm the appointment
        assertTrue(appointmentService.confirmAppointment(appointment));
        // Status is updated
        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
        // updateTimestamp is populated with the current time
        Instant updateTimestamp = appointment.getUpdateTimestamp();
        assertNotNull(updateTimestamp);
        assertTrue(updateTimestamp.isAfter(appointment.getCreationTimestamp()));

        // Cancelling the appointment works
        appointmentService.cancelAppointment(appointment);
        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
        // updateTimestamp is updated again
        assertTrue(appointment.getUpdateTimestamp().isAfter(updateTimestamp));
    }

    @Test
    void confirmInvalidAppointment() {
        LocalDate date = LocalDate.now().plusDays(2);
        CreateAppointmentModel model = new CreateAppointmentModel(
                date,
                dummyDataComponent.generateDummySentence()
        );
        // Create an appointment
        Appointment appointment = appointmentService.createAppointment(serviceProvider, serviceRequest, model);
        assertNotNull(appointment);
        // Cancel the appointment
        appointmentService.cancelAppointment(appointment);
        // updateTimestamp is populated
        Instant updateTimestamp = appointment.getUpdateTimestamp();
        assertNotNull(updateTimestamp);
        assertTrue(updateTimestamp.isAfter(appointment.getCreationTimestamp()));
        // Trying to confirm a CANCELLED, EXPIRED, or COMPLETED appointment fails
        AppointmentStatus[] notConfirmableStatuses = new AppointmentStatus[]{AppointmentStatus.CANCELLED, AppointmentStatus.EXPIRED, AppointmentStatus.COMPLETED};
        for (AppointmentStatus status : notConfirmableStatuses) {
            appointment.setStatus(status);
            // Confirming does not work
            assertFalse(appointmentService.confirmAppointment(appointment));
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
            appointmentsByMonth.put(yearMonth, dummyDataComponent.createAppointmentsFor(serviceProvider, serviceRequest, yearMonth));
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
            List<AppointmentModel> actualAppointments = appointmentService.getAppointmentsByMonth(customer, yearMonth);
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
            List<AppointmentModel> actualAppointments = appointmentService.getAppointmentsByMonth(serviceProvider, yearMonth);
            actualAppointments.sort(Comparator.comparing(AppointmentModel::appointmentId)); // Sort by id so that indices can match
            assertArrayEquals(expectedAppointments.toArray(), actualAppointments.toArray());
        }
    }

    @Test
    @Transactional
    void customerGetUnconfirmedAppointments() {
        YearMonth nextMonth = YearMonth.now().plusMonths(1);
        // Generate unconfirmed appointments
        List<Appointment> appointments = dummyDataComponent.createAppointmentsFor(serviceProvider, serviceRequest, nextMonth);
        // Generate confirmed and cancelled appointments to make sure they're being filtered out
        List<Appointment> confirmedAppointments = dummyDataComponent.createAppointmentsFor(serviceProvider, serviceRequest, nextMonth);
        confirmedAppointments.forEach(appointmentService::confirmAppointment);
        List<Appointment> cancelledAppointments = dummyDataComponent.createAppointmentsFor(serviceProvider, serviceRequest, nextMonth);
        cancelledAppointments.forEach(appointmentService::cancelAppointment);

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
        dummyDataComponent.createAppointmentsFor(serviceProvider, serviceRequest, nextMonth);
        // Generate confirmed and cancelled appointments
        List<Appointment> confirmedAppointments = dummyDataComponent.createAppointmentsFor(serviceProvider, serviceRequest, nextMonth);
        confirmedAppointments.forEach(appointmentService::confirmAppointment);
        List<Appointment> cancelledAppointments = dummyDataComponent.createAppointmentsFor(serviceProvider, serviceRequest, nextMonth);
        cancelledAppointments.forEach(appointmentService::cancelAppointment);

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
        List<Appointment> appointments = dummyDataComponent.createAppointmentsFor(serviceProvider, serviceRequest, nextMonth);
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
    void nonExistentAppointment() {
        assertThrows(NonExistentAppointmentException.class,
                () -> customerAppointmentController.getAppointment(Integer.MAX_VALUE, CUSTOMER_USER));
        assertThrows(NonExistentAppointmentException.class,
                () -> customerAppointmentController.confirmAppointment(Integer.MAX_VALUE, CUSTOMER_USER));
        assertThrows(NonExistentAppointmentException.class,
                () -> customerAppointmentController.cancelAppointment(Integer.MAX_VALUE, CUSTOMER_USER));

        assertThrows(NonExistentAppointmentException.class,
                () -> serviceProviderAppointmentController.getAppointment(Integer.MAX_VALUE, SERVICE_PROVIDER_USER));
        assertThrows(NonExistentAppointmentException.class,
                () -> serviceProviderAppointmentController.cancelAppointment(Integer.MAX_VALUE, SERVICE_PROVIDER_USER));
    }

    @Test
    void nonExistentPost() {
        assertThrows(NonExistentPostException.class,
                () -> customerAppointmentController.getAppointmentsFor(Integer.MAX_VALUE, CUSTOMER_USER));
        assertThrows(NonExistentPostException.class,
                () -> serviceProviderAppointmentController.createAppointment(Integer.MAX_VALUE, null, SERVICE_PROVIDER_USER));
    }

    @Test
    @Transactional
    void requireConfirmedEmailRequest() {
        CreateAppointmentModel model = new CreateAppointmentModel(
                LocalDate.now().plusDays(2),
                dummyDataComponent.generateDummySentence()
        );
        // Trying to create an appointment for a service request without an accepted email request fails
        ApiException exception = assertThrows(ApiException.class,
                () -> serviceProviderAppointmentController.createAppointment(serviceRequest.getId(), model, SERVICE_PROVIDER_USER));
        assertEquals("missing_accepted_email_request", exception.getType());

        // Send an email request
        assertTrue(emailRequestService.sendEmailRequest(serviceRequest, serviceProvider));
        // Email request has not been accepted yet, so creating an appointment should still fail
        exception = assertThrows(ApiException.class,
                () -> serviceProviderAppointmentController.createAppointment(serviceRequest.getId(), model, SERVICE_PROVIDER_USER));
        assertEquals("missing_accepted_email_request", exception.getType());

        // Accept the email request
        assertTrue(emailRequestService.acceptEmailRequest(serviceRequest.getEmailRequests().iterator().next()));
        // Creating an appointment works
        assertDoesNotThrow(() -> serviceProviderAppointmentController.createAppointment(serviceRequest.getId(), model, SERVICE_PROVIDER_USER));

        // Reject the email request
        emailRequestService.rejectEmailRequest(serviceRequest.getEmailRequests().iterator().next());
        // Trying to create an appointment fails
        exception = assertThrows(ApiException.class,
                () -> serviceProviderAppointmentController.createAppointment(serviceRequest.getId(), model, SERVICE_PROVIDER_USER));
        assertEquals("missing_accepted_email_request", exception.getType());
    }

    @Test
    void customerConfirmCancelledAppointment() {
        CreateAppointmentModel model = new CreateAppointmentModel(
                LocalDate.now().plusDays(2),
                dummyDataComponent.generateDummySentence()
        );
        // Create an UNCONFIRMED appointment
        Appointment appointment = appointmentService.createAppointment(serviceProvider, serviceRequest, model);
        // cancel it
        appointmentService.cancelAppointment(appointment);
        // When the customer tries to confirm it, it fails
        ApiException exception = assertThrows(ApiException.class,
                () -> customerAppointmentController.confirmAppointment(serviceRequest.getId(), CUSTOMER_USER));
        assertEquals("unconfirmed_appointment", exception.getType());
    }
}

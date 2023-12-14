package homerep.springy.component;

import homerep.springy.authorities.AccountType;
import homerep.springy.entity.*;
import homerep.springy.exception.ConflictingAppointmentException;
import homerep.springy.model.appointment.CreateAppointmentModel;
import homerep.springy.repository.*;
import homerep.springy.service.AppointmentService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Component
public class DummyDataComponent {

    private static final List<String> FIRST_NAMES = List.of("Zoey", "Souta", "Miki", "Marina");
    private static final List<String> LAST_NAMES = List.of("Proasheck", "Sakura", "Amane", "Hale");

    private static final List<String> DUMMY_WORDS = List.of(("Lorem ipsum dolor sit amet, consectetur adipiscing" +
            "elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis" +
            "nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in" +
            "reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat" +
            "cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.").split("\\s"));

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private AppointmentService appointmentService;

    private final Random random = new Random(0);

    public Account createAccount(String email, AccountType type) {
        Account account = new Account();
        account.setEmail(email);
        account.setType(type);
        account.setVerified(true);
        return accountRepository.save(account);
    }

    public Customer createCustomer(String email) {
        Account account = createAccount(email, AccountType.CUSTOMER);
        Customer customer = new Customer(account);
        customer.setFirstName(randomFrom(FIRST_NAMES));
        customer.setMiddleName("");
        customer.setLastName(randomFrom(LAST_NAMES));
        // TODO random address and phone numbers?
        customer.setAddress("201 Mullica Hill Rd, Glassboro, NJ 08028");
        customer.setPhoneNumber("1231231234");
        return customerRepository.save(customer);
    }

    public ServiceProvider createServiceProvider(String email) {
        Account account = createAccount(email, AccountType.SERVICE_PROVIDER);
        ServiceProvider serviceProvider = new ServiceProvider(account);
        serviceProvider.setName(randomFrom(LAST_NAMES) + " HVAC and Plumbing");
        serviceProvider.setDescription(generateDummySentence());
        serviceProvider.setServices(List.of("HVAC", "Plumbing"));
        serviceProvider.setPhoneNumber("1231231234");
        serviceProvider.setAddress("201 Mullica Hill Rd, Glassboro, NJ 08028");
        serviceProvider.setContactEmailAddress(email);
        return serviceProviderRepository.save(serviceProvider);
    }

    public ServiceRequest createServiceRequest(Customer customer) {
        ServiceRequest serviceRequest = new ServiceRequest(customer);
        serviceRequest.setTitle(generateDummySentence());
        serviceRequest.setDescription(generateDummySentence());
        serviceRequest.setService("HVAC");
        serviceRequest.setStatus(ServiceRequest.Status.PENDING);
        serviceRequest.setDollars(random.nextInt(100, 10000));
        serviceRequest.setCreationDate(new Date());
        serviceRequest.setAddress("201 Mullica Hill Rd, Glassboro, NJ 08028");
        serviceRequest.setLongitude(random.nextDouble(-180, 180));
        serviceRequest.setLatitude(random.nextDouble(-180, 180));
        return serviceRequestRepository.save(serviceRequest);
    }

    public List<Appointment> createAppointmentsFor(ServiceProvider serviceProvider, ServiceRequest serviceRequest, YearMonth yearMonth, ZoneId zoneId) {
        int count = random.nextInt(2, 4);
        List<Appointment> appointments = new ArrayList<>(count + 2);

        Instant startOfMonth = yearMonth.atDay(1).atStartOfDay(zoneId).toInstant();
        try {
            CreateAppointmentModel model = new CreateAppointmentModel(
                    startOfMonth,
                    startOfMonth.plus(randomDuration()),
                    generateDummySentence()
            );
            appointments.add(appointmentService.createAppointment(serviceProvider, serviceRequest, model));
        } catch (ConflictingAppointmentException ignored) {
        }

        Instant endOfMonth = ZonedDateTime.of(yearMonth.atEndOfMonth(), LocalTime.MAX, zoneId).toInstant();
        try {
            CreateAppointmentModel model = new CreateAppointmentModel(
                    endOfMonth.minus(randomDuration()),
                    endOfMonth,
                    generateDummySentence()
            );
            appointments.add(appointmentService.createAppointment(serviceProvider, serviceRequest, model));
        } catch (ConflictingAppointmentException ignored) {
        }

        for (int i = 0; i < count; i++) {
            // Try 10 times to generate a non-conflicting time period
            for (int j = 0; j < 10; j++) {
                try {
                    Duration duration = randomDuration();
                    Instant start = randomInstantWithin(startOfMonth, endOfMonth.minus(duration));
                    CreateAppointmentModel model = new CreateAppointmentModel(
                            start,
                            start.plus(duration),
                            generateDummySentence()
                    );
                    appointments.add(appointmentService.createAppointment(serviceProvider, serviceRequest, model));
                    break;
                } catch (ConflictingAppointmentException ignored) {
                }
            }
        }
        return appointments;
    }

    public <T> T randomFrom(List<T> list) {
        return list.get(random.nextInt(0, list.size()));
    }

    private Instant randomInstantWithin(Instant start, Instant end) {
        return Instant.ofEpochSecond(random.nextLong(start.getEpochSecond(), end.getEpochSecond() + 1));
    }

    public Duration randomDuration() {
        return Duration.ofMinutes(random.nextInt(5, 180));
    }

    public String generateDummySentence() {
        List<String> words = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            words.add(randomFrom(DUMMY_WORDS));
        }
        return Strings.join(words, ' ');
    }
}

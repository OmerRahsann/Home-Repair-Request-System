package homerep.springy.component;

import homerep.springy.authorities.AccountType;
import homerep.springy.entity.*;
import homerep.springy.model.appointment.CreateAppointmentModel;
import homerep.springy.repository.*;
import homerep.springy.service.AppointmentService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

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
    private AppointmentRepository appointmentRepository;

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
        customer.setLastName(randomFrom(LAST_NAMES));
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
        serviceProvider.setLongitude(random.nextDouble(-180, 180));
        serviceProvider.setLatitude(random.nextDouble(-180, 180));
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

    public List<Appointment> createAppointmentsFor(ServiceProvider serviceProvider, ServiceRequest serviceRequest, YearMonth yearMonth) {
        Set<LocalDate> usedDates = appointmentRepository.findAll().stream()
                .map(Appointment::getDate)
                .collect(Collectors.toSet());

        int count = random.nextInt(2, 4);
        List<Appointment> appointments = new ArrayList<>(count + 2);

        LocalDate start = yearMonth.atDay(1);
        if (usedDates.add(start)) {
            CreateAppointmentModel model = new CreateAppointmentModel(
                    start,
                    generateDummySentence()
            );
            appointments.add(appointmentService.createAppointment(serviceProvider, serviceRequest, model));
        }

        LocalDate end = yearMonth.atEndOfMonth();
        if (usedDates.add(end)) {
            CreateAppointmentModel model = new CreateAppointmentModel(
                    end,
                    generateDummySentence()
            );
            appointments.add(appointmentService.createAppointment(serviceProvider, serviceRequest, model));
        }

        for (int i = 0; i < count; i++) {
            // Try 10 times to generate a non conflicting date
            for (int j = 0; j < 10; j++) {
                int day = random.nextInt(1, end.getDayOfMonth() + 1);
                LocalDate date = yearMonth.atDay(day);
                if (usedDates.add(date)) {
                    CreateAppointmentModel model = new CreateAppointmentModel(
                            date,
                            generateDummySentence()
                    );
                    appointments.add(appointmentService.createAppointment(serviceProvider, serviceRequest, model));
                    break;
                }
            }
        }
        return appointments;
    }

    private <T> T randomFrom(List<T> list) {
        return list.get(random.nextInt(0, list.size()));
    }

    public String generateDummySentence() {
        List<String> words = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            words.add(randomFrom(DUMMY_WORDS));
        }
        return Strings.join(words, ' ');
    }
}

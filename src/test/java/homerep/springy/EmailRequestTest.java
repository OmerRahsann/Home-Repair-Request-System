package homerep.springy;

import homerep.springy.authorities.AccountType;
import homerep.springy.authorities.Verified;
import homerep.springy.config.TestDatabaseConfig;
import homerep.springy.config.TestStorageConfig;
import homerep.springy.controller.customer.CustomerEmailRequestController;
import homerep.springy.controller.provider.ServiceProviderEmailRequestController;
import homerep.springy.entity.*;
import homerep.springy.exception.ApiException;
import homerep.springy.model.accountinfo.ServiceProviderInfoModel;
import homerep.springy.model.emailrequest.EmailRequestInfoModel;
import homerep.springy.model.emailrequest.EmailRequestModel;
import homerep.springy.model.emailrequest.EmailRequestStatus;
import homerep.springy.repository.*;
import homerep.springy.service.EmailRequestService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestDatabaseConfig
@Import(TestStorageConfig.class)
public class EmailRequestTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private EmailRequestRepository emailRequestRepository;

    @Autowired
    private EmailRequestService emailRequestService;

    @Autowired
    private CustomerEmailRequestController customerEmailRequestController;

    @Autowired
    private ServiceProviderEmailRequestController serviceProviderEmailRequestController;

    private ServiceProvider serviceProvider1;
    private ServiceProvider serviceProvider2;
    private ServiceRequest serviceRequest1;
    private ServiceRequest serviceRequest2;

    private static final String CUSTOMER_EMAIL = "test@localhost";
    private static final User CUSTOMER_USER = new User(CUSTOMER_EMAIL, "", List.of(AccountType.CUSTOMER, Verified.INSTANCE));

    private static final String SERVICE_PROVIDER_1_EMAIL = "example@example.com";
    private static final User SERVICE_PROVIDER_1_USER = new User(SERVICE_PROVIDER_1_EMAIL, "", List.of(AccountType.SERVICE_PROVIDER, Verified.INSTANCE));

    private static final String SERVICE_PROVIDER_2_EMAIL = "example2@example.com";
    private static final User SERVICE_PROVIDER_2_USER = new User(SERVICE_PROVIDER_2_EMAIL, "", List.of(AccountType.SERVICE_PROVIDER, Verified.INSTANCE));

    private ServiceRequest createServiceRequest(Customer customer, String title) {
        ServiceRequest serviceRequest = new ServiceRequest(customer);
        serviceRequest.setTitle(title);
        serviceRequest.setDescription("description");
        serviceRequest.setService("HVAC");
        serviceRequest.setStatus(ServiceRequest.Status.PENDING);
        serviceRequest.setDollars(100);
        serviceRequest.setCreationDate(new Date());
        serviceRequest.setAddress("");
        serviceRequest.setLongitude(0);
        serviceRequest.setLatitude(0);
        return serviceRequestRepository.save(serviceRequest);
    }

    @BeforeEach
    void setupCustomer() {
        Account account = new Account();
        account.setEmail(CUSTOMER_EMAIL);
        account.setType(AccountType.CUSTOMER);
        account.setVerified(true);
        account = accountRepository.save(account);

        Customer customer = new Customer(account);
        customer.setFirstName("Zoey");
        customer.setLastName("Proasheck");
        customer = customerRepository.save(customer);

        serviceRequest1 = createServiceRequest(customer, "Service request 1");
        serviceRequest2 = createServiceRequest(customer, "Service request 2");
    }

    private ServiceProvider createServiceProvider(String email) {
        Account providerAccount = new Account();
        providerAccount.setEmail(email);
        providerAccount.setType(AccountType.SERVICE_PROVIDER);
        providerAccount.setVerified(true);
        providerAccount = accountRepository.save(providerAccount);

        ServiceProvider serviceProvider = new ServiceProvider(providerAccount);
        serviceProvider.setName(email + " HVAC and Plumbing");
        serviceProvider.setDescription("Heating, cooling, and plumbing");
        serviceProvider.setServices(List.of("HVAC", "Plumbing"));
        serviceProvider.setPhoneNumber("1231231234");
        serviceProvider.setAddress("201 Mullica Hill Rd, Glassboro, NJ 08028");
        serviceProvider.setContactEmailAddress(email);
        serviceProvider.setLongitude(39.709824);
        serviceProvider.setLatitude(-75.1206862);
        return serviceProviderRepository.save(serviceProvider);
    }

    @BeforeEach
    void setupServiceProvider() {
        serviceProvider1 = createServiceProvider(SERVICE_PROVIDER_1_EMAIL);
        serviceProvider2 = createServiceProvider(SERVICE_PROVIDER_2_EMAIL);
    }

    @Test
    void getEmailNoRequest() {
        // No email requests
        assertTrue(emailRequestRepository.findAll().isEmpty());
        // Try to get the email without sending a request
        EmailRequestModel emailRequestModel = emailRequestService.getEmail(serviceRequest1, serviceProvider1);
        assertNotNull(emailRequestModel);
        assertEquals(serviceRequest1.getId(), emailRequestModel.serviceRequestId());
        // email is not supplied
        assertNull(emailRequestModel.email());
        assertEquals(EmailRequestStatus.NOT_REQUESTED, emailRequestModel.status());
    }

    @Test
    void getEmailAfterRequested() {
        // No email requests
        assertTrue(emailRequestRepository.findAll().isEmpty());
        Instant start = Instant.now();
        // Send an email request
        emailRequestService.sendEmailRequest(serviceRequest1, serviceProvider1);
        // Email request is created
        assertEquals(1, emailRequestRepository.findAll().size());
        EmailRequest emailRequest = emailRequestRepository.findAll().get(0);
        // status is PENDING
        assertEquals(EmailRequestStatus.PENDING, emailRequest.getStatus());
        // timestamp is sensible
        assertTrue(emailRequest.getRequestTimestamp().isAfter(start));
        // Try to get the email with a request that has not been accepted or denied
        EmailRequestModel emailRequestModel = emailRequestService.getEmail(serviceRequest1, serviceProvider1);
        assertNotNull(emailRequestModel);
        assertEquals(serviceRequest1.getId(), emailRequestModel.serviceRequestId());
        // email is not supplied
        assertNull(emailRequestModel.email());
        assertEquals(EmailRequestStatus.PENDING, emailRequestModel.status());
    }

    @Test
    void getEmailAfterAccepted() {
        // No email requests
        assertTrue(emailRequestRepository.findAll().isEmpty());
        // Send an email request
        emailRequestService.sendEmailRequest(serviceRequest1, serviceProvider1);
        // Email request is created
        assertEquals(1, emailRequestRepository.findAll().size());
        EmailRequest emailRequest = emailRequestRepository.findAll().get(0);
        // status is PENDING
        assertEquals(EmailRequestStatus.PENDING, emailRequest.getStatus());
        // Accept the email request
        emailRequestService.acceptEmailRequest(emailRequest);
        // Email is provided after the request is accepted
        EmailRequestModel emailRequestModel = emailRequestService.getEmail(serviceRequest1, serviceProvider1);
        assertNotNull(emailRequestModel);
        assertEquals(serviceRequest1.getId(), emailRequestModel.serviceRequestId());
        assertEquals(CUSTOMER_EMAIL, emailRequestModel.email());
        assertEquals(EmailRequestStatus.ACCEPTED, emailRequestModel.status());
    }

    @Test
    void getEmailAfterRejected() {
        // No email requests
        assertTrue(emailRequestRepository.findAll().isEmpty());
        // Send an email request
        emailRequestService.sendEmailRequest(serviceRequest1, serviceProvider1);
        // Email request is created
        assertEquals(1, emailRequestRepository.findAll().size());
        EmailRequest emailRequest = emailRequestRepository.findAll().get(0);
        // status is PENDING
        assertEquals(EmailRequestStatus.PENDING, emailRequest.getStatus());
        // Reject the email request
        emailRequestService.rejectEmailRequest(emailRequest);
        // Email is not provided after the request is rejected
        EmailRequestModel emailRequestModel = emailRequestService.getEmail(serviceRequest1, serviceProvider1);
        assertNotNull(emailRequestModel);
        assertEquals(serviceRequest1.getId(), emailRequestModel.serviceRequestId());
        assertNull(emailRequestModel.email());
        assertEquals(EmailRequestStatus.REJECTED, emailRequestModel.status());
    }

    @Test
    void acceptAfterRejectRequest() {
        // Send an email request
        emailRequestService.sendEmailRequest(serviceRequest1, serviceProvider1);
        // Email request is created
        assertEquals(1, emailRequestRepository.findAll().size());
        EmailRequest emailRequest = emailRequestRepository.findAll().get(0);
        // status is PENDING
        assertEquals(EmailRequestStatus.PENDING, emailRequest.getStatus());
        // Reject the email request
        emailRequestService.rejectEmailRequest(emailRequest);
        assertEquals(EmailRequestStatus.REJECTED, emailRequest.getStatus());
        // Trying to accept the rejected email request does not work
        assertFalse(emailRequestService.acceptEmailRequest(emailRequest));
        assertEquals(EmailRequestStatus.REJECTED, emailRequest.getStatus());
    }

    @Test
    @Transactional
    void getEmailRequests() {
        // No email requests
        assertTrue(emailRequestRepository.findAll().isEmpty());
        // The email request list is also empty
        List<EmailRequestInfoModel> models = emailRequestService.getEmailRequests(serviceRequest1);
        assertTrue(models.isEmpty());
        // Send an email request
        emailRequestService.sendEmailRequest(serviceRequest1, serviceProvider1);
        // Email request is created
        assertEquals(1, emailRequestRepository.findAll().size());
        EmailRequest emailRequest = emailRequestRepository.findAll().get(0);
        // List includes the created email request
        models = emailRequestService.getEmailRequests(serviceRequest1);
        assertEquals(1, models.size());
        EmailRequestInfoModel model = models.get(0);
        // Model matches repository information
        assertEquals(emailRequest.getId(), model.id());
        assertEquals(emailRequest.getServiceRequest().getId(), model.serviceRequestId());
        assertEquals(ServiceProviderInfoModel.fromEntity(serviceProvider1), model.serviceProvider());
        assertEquals(EmailRequestStatus.PENDING, model.status()); // Status starts as PENDING
        assertEquals(emailRequest.getStatus(), model.status());
    }

    @Test
    void nonExistentPost() {
        // Trying to get the email for a non-existent post results in an ApiException
        ApiException exception = assertThrows(ApiException.class,
                () -> serviceProviderEmailRequestController.getEmail(Integer.MAX_VALUE, SERVICE_PROVIDER_1_USER));
        assertEquals(exception.getType(), "non_existent_post");

        // Trying to send an email request for a non-existent post results in an ApiException
        exception = assertThrows(ApiException.class,
                () -> serviceProviderEmailRequestController.requestEmail(Integer.MAX_VALUE, SERVICE_PROVIDER_1_USER));
        assertEquals(exception.getType(), "non_existent_post");

        // Trying to list email requests for a non-existent post results in an ApiException
        exception = assertThrows(ApiException.class,
                () -> customerEmailRequestController.getEmailRequests(Integer.MAX_VALUE, CUSTOMER_USER));
        assertEquals(exception.getType(), "non_existent_post");
    }

    @Test
    void nonExistentEmailRequest() {
        // Trying to accept a non-existent email request results in an ApiException
        ApiException exception = assertThrows(ApiException.class,
                () -> customerEmailRequestController.acceptEmailRequest(Integer.MAX_VALUE, CUSTOMER_USER));
        assertEquals(exception.getType(), "non_existent_email_request");
        // Trying to reject a non-existent email request results in an ApiException
        exception = assertThrows(ApiException.class,
                () -> customerEmailRequestController.rejectEmailRequest(Integer.MAX_VALUE, CUSTOMER_USER));
        assertEquals(exception.getType(), "non_existent_email_request");
    }

    @Test
    @Transactional
    void doubleEmailRequest() {
        // No email requests
        assertTrue(emailRequestRepository.findAll().isEmpty());
        // Sending a single email request is successful
        assertDoesNotThrow(() -> serviceProviderEmailRequestController.requestEmail(serviceRequest1.getId(), SERVICE_PROVIDER_1_USER));
        assertEquals(1, emailRequestRepository.findAll().size());
        // Sending another email request results in an error
        ApiException exception = assertThrows(ApiException.class,
                () -> serviceProviderEmailRequestController.requestEmail(serviceRequest1.getId(), SERVICE_PROVIDER_1_USER));
        assertEquals(exception.getType(), "already_requested");
        // no additional email requests are created
        assertEquals(1, emailRequestRepository.findAll().size());
    }

    @Test
    @Transactional
    void customerGetPendingEmailRequests() {
        // Multiple service providers can send an email request
        serviceProviderEmailRequestController.requestEmail(serviceRequest1.getId(), SERVICE_PROVIDER_1_USER);
        serviceProviderEmailRequestController.requestEmail(serviceRequest1.getId(), SERVICE_PROVIDER_2_USER);
        // Customer can get a list of all pending email requests
        List<EmailRequestInfoModel> requests = customerEmailRequestController.getPendingEmailRequests(CUSTOMER_USER);
        assertEquals(2, requests.size());
        // Both requests in the list are pending
        assertEquals(EmailRequestStatus.PENDING, requests.get(0).status());
        assertEquals(EmailRequestStatus.PENDING, requests.get(1).status());
        // request 0 is from serviceProvider2
        assertEquals(SERVICE_PROVIDER_2_EMAIL, requests.get(0).serviceProvider().contactEmailAddress());
        // request 1 is from serviceProvider1
        assertEquals(SERVICE_PROVIDER_1_EMAIL, requests.get(1).serviceProvider().contactEmailAddress());
        // List is sorted by request timestamp in descending order
        Instant request0Timestamp = requests.get(0).requestTimestamp();
        Instant request1Timestamp = requests.get(1).requestTimestamp();
        assertTrue(request0Timestamp.isAfter(request1Timestamp));

        // Accept and reject some requests
        customerEmailRequestController.acceptEmailRequest(requests.get(0).id(), CUSTOMER_USER);
        customerEmailRequestController.rejectEmailRequest(requests.get(1).id(), CUSTOMER_USER);
        // There's no more pending requests
        requests = customerEmailRequestController.getPendingEmailRequests(CUSTOMER_USER);
        assertTrue(requests.isEmpty());
    }

    @Test
    @Transactional
    void serviceProviderGetAcceptedEmailRequests() {
        // Send multiple email requests
        serviceProviderEmailRequestController.requestEmail(serviceRequest1.getId(), SERVICE_PROVIDER_1_USER);
        serviceProviderEmailRequestController.requestEmail(serviceRequest2.getId(), SERVICE_PROVIDER_1_USER);
        // There's no pending email requests were accepted
        List<EmailRequestModel> models = serviceProviderEmailRequestController.getAcceptedEmailRequests(SERVICE_PROVIDER_1_USER);
        assertTrue(models.isEmpty());
        // Accept both requests
        for (EmailRequest emailRequest : emailRequestRepository.findAll()) {
            customerEmailRequestController.acceptEmailRequest(emailRequest.getId(), CUSTOMER_USER);
        }
        // Service provider get a list of accepted email requests
        models = serviceProviderEmailRequestController.getAcceptedEmailRequests(SERVICE_PROVIDER_1_USER);
        assertEquals(2, models.size());
        // List is sorted by status update timestamp in descending order
        Instant request0Timestamp = models.get(0).updateTimestamp();
        Instant request1Timestamp = models.get(1).updateTimestamp();
        assertTrue(request0Timestamp.isAfter(request1Timestamp));
    }
}

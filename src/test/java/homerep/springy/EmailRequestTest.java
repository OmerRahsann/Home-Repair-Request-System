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

    private ServiceProvider serviceProvider;
    private ServiceRequest serviceRequest;

    private static final String CUSTOMER_EMAIL = "test@localhost";
    private static final User CUSTOMER_USER = new User(CUSTOMER_EMAIL, "", List.of(AccountType.CUSTOMER, Verified.INSTANCE));

    private static final String SERVICE_PROVIDER_EMAIL = "example@example.com";
    private static final User SERVICE_PROVIDER_USER = new User(SERVICE_PROVIDER_EMAIL, "", List.of(AccountType.SERVICE_PROVIDER, Verified.INSTANCE));

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

        serviceRequest = new ServiceRequest(customer);
        serviceRequest.setTitle("test");
        serviceRequest.setDescription("description");
        serviceRequest.setService("HVAC");
        serviceRequest.setStatus(ServiceRequest.Status.PENDING);
        serviceRequest.setDollars(100);
        serviceRequest.setCreationDate(new Date());
        serviceRequest.setAddress("");
        serviceRequest.setLongitude(0);
        serviceRequest.setLatitude(0);
        serviceRequest = serviceRequestRepository.save(serviceRequest);
    }

    @BeforeEach
    void setupServiceProvider() {
        Account providerAccount = new Account();
        providerAccount.setEmail(SERVICE_PROVIDER_EMAIL);
        providerAccount.setType(AccountType.SERVICE_PROVIDER);
        providerAccount.setVerified(true);
        providerAccount = accountRepository.save(providerAccount);

        serviceProvider = new ServiceProvider(providerAccount);
        serviceProvider.setName("Sakura HVAC and Plumbing");
        serviceProvider.setDescription("Heating, cooling, and plumbing");
        serviceProvider.setServices(List.of("HVAC", "Plumbing"));
        serviceProvider.setPhoneNumber("1231231234");
        serviceProvider.setAddress("201 Mullica Hill Rd, Glassboro, NJ 08028");
        serviceProvider.setContactEmailAddress(SERVICE_PROVIDER_EMAIL);
        serviceProvider.setLongitude(39.709824);
        serviceProvider.setLatitude(-75.1206862);
        serviceProvider = serviceProviderRepository.save(serviceProvider);
    }

    @Test
    void getEmailNoRequest() {
        // No email requests
        assertTrue(emailRequestRepository.findAll().isEmpty());
        // Try to get the email without sending a request
        EmailRequestModel emailRequestModel = emailRequestService.getEmail(serviceRequest, serviceProvider);
        assertNotNull(emailRequestModel);
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
        emailRequestService.sendEmailRequest(serviceRequest, serviceProvider);
        // Email request is created
        assertEquals(1, emailRequestRepository.findAll().size());
        EmailRequest emailRequest = emailRequestRepository.findAll().get(0);
        // status is REQUESTED
        assertEquals(EmailRequestStatus.REQUESTED, emailRequest.getStatus());
        // timestamp is sensible
        assertTrue(emailRequest.getRequestTimestamp().isAfter(start));
        // Try to get the email with a request that has not been accepted or denied
        EmailRequestModel emailRequestModel = emailRequestService.getEmail(serviceRequest, serviceProvider);
        assertNotNull(emailRequestModel);
        // email is not supplied
        assertNull(emailRequestModel.email());
        assertEquals(EmailRequestStatus.REQUESTED, emailRequestModel.status());
    }

    @Test
    void getEmailAfterAccepted() {
        // No email requests
        assertTrue(emailRequestRepository.findAll().isEmpty());
        // Send an email request
        emailRequestService.sendEmailRequest(serviceRequest, serviceProvider);
        // Email request is created
        assertEquals(1, emailRequestRepository.findAll().size());
        EmailRequest emailRequest = emailRequestRepository.findAll().get(0);
        // status is REQUESTED
        assertEquals(EmailRequestStatus.REQUESTED, emailRequest.getStatus());
        // Accept the email request
        emailRequestService.updateEmailRequestStatus(emailRequest, true);
        // Email is provided after the request is accepted
        EmailRequestModel emailRequestModel = emailRequestService.getEmail(serviceRequest, serviceProvider);
        assertNotNull(emailRequestModel);
        assertEquals(CUSTOMER_EMAIL, emailRequestModel.email());
        assertEquals(EmailRequestStatus.ACCEPTED, emailRequestModel.status());
    }

    @Test
    void getEmailAfterRejected() {
        // No email requests
        assertTrue(emailRequestRepository.findAll().isEmpty());
        // Send an email request
        emailRequestService.sendEmailRequest(serviceRequest, serviceProvider);
        // Email request is created
        assertEquals(1, emailRequestRepository.findAll().size());
        EmailRequest emailRequest = emailRequestRepository.findAll().get(0);
        // status is REQUESTED
        assertEquals(EmailRequestStatus.REQUESTED, emailRequest.getStatus());
        // Reject the email request
        emailRequestService.updateEmailRequestStatus(emailRequest, false);
        // Email is not provided after the request is rejected
        EmailRequestModel emailRequestModel = emailRequestService.getEmail(serviceRequest, serviceProvider);
        assertNotNull(emailRequestModel);
        assertNull(emailRequestModel.email());
        assertEquals(EmailRequestStatus.REJECTED, emailRequestModel.status());
    }

    @Test
    @Transactional
    void listEmailRequests() {
        // No email requests
        assertTrue(emailRequestRepository.findAll().isEmpty());
        // The email request list is also empty
        List<EmailRequestInfoModel> models = emailRequestService.listEmailRequests(serviceRequest);
        assertTrue(models.isEmpty());
        // Send an email request
        emailRequestService.sendEmailRequest(serviceRequest, serviceProvider);
        // Email request is created
        assertEquals(1, emailRequestRepository.findAll().size());
        EmailRequest emailRequest = emailRequestRepository.findAll().get(0);
        // List includes the created email request
        models = emailRequestService.listEmailRequests(serviceRequest);
        assertEquals(1, models.size());
        EmailRequestInfoModel model = models.get(0);
        // Model matches repository information
        assertEquals(emailRequest.getId(), model.id());
        assertEquals(ServiceProviderInfoModel.fromEntity(serviceProvider), model.serviceProvider());
        assertEquals(EmailRequestStatus.REQUESTED, model.status()); // Status starts as REQUESTED
        assertEquals(emailRequest.getStatus(), model.status());
    }

    @Test
    void nonExistentPost() {
        // Trying to get the email for a non-existent post results in an ApiException
        ApiException exception = assertThrows(ApiException.class,
                () -> serviceProviderEmailRequestController.getEmail(Integer.MAX_VALUE, SERVICE_PROVIDER_USER));
        assertEquals(exception.getType(), "non_existent_post");

        // Trying to send an email request for a non-existent post results in an ApiException
        exception = assertThrows(ApiException.class,
                () -> serviceProviderEmailRequestController.requestEmail(Integer.MAX_VALUE, SERVICE_PROVIDER_USER));
        assertEquals(exception.getType(), "non_existent_post");

        // Trying to list email requests for a non-existent post results in an ApiException
        exception = assertThrows(ApiException.class,
                () -> customerEmailRequestController.getEmailRequests(Integer.MAX_VALUE, CUSTOMER_USER));
        assertEquals(exception.getType(), "non_existent_post");

        // Trying to accept an email request for a non-existent post results in an ApiException
        exception = assertThrows(ApiException.class,
                () -> customerEmailRequestController.updateEmailRequestStatus(Integer.MAX_VALUE, 0, true, CUSTOMER_USER));
        assertEquals(exception.getType(), "non_existent_post");
    }

    @Test
    void nonExistentEmailRequest() {
        // Trying to accept a non-existent email request results in an ApiException
        ApiException exception = assertThrows(ApiException.class,
                () -> customerEmailRequestController.updateEmailRequestStatus(serviceRequest.getId(), Integer.MAX_VALUE, true, CUSTOMER_USER));
        assertEquals(exception.getType(), "non_existent_email_request");
    }

    @Test
    @Transactional
    void doubleEmailRequest() {
        // No email requests
        assertTrue(emailRequestRepository.findAll().isEmpty());
        // Sending a single email request is successful
        assertDoesNotThrow(() -> serviceProviderEmailRequestController.requestEmail(serviceRequest.getId(), SERVICE_PROVIDER_USER));
        assertEquals(1, emailRequestRepository.findAll().size());
        // Sending another email request results in an error
        ApiException exception = assertThrows(ApiException.class,
                () -> serviceProviderEmailRequestController.requestEmail(serviceRequest.getId(), SERVICE_PROVIDER_USER));
        assertEquals(exception.getType(), "already_requested");
        // no additional email requests are created
        assertEquals(1, emailRequestRepository.findAll().size());
    }
}

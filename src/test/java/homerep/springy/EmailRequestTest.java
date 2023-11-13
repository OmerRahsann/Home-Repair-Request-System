package homerep.springy;

import com.fasterxml.jackson.databind.ObjectMapper;
import homerep.springy.authorities.AccountType;
import homerep.springy.config.TestDatabaseConfig;
import homerep.springy.config.TestStorageConfig;
import homerep.springy.entity.*;
import homerep.springy.model.accountinfo.ServiceProviderInfoModel;
import homerep.springy.model.emailrequest.EmailRequestInfoModel;
import homerep.springy.model.emailrequest.EmailRequestModel;
import homerep.springy.model.emailrequest.EmailRequestStatus;
import homerep.springy.repository.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestDatabaseConfig
@Import(TestStorageConfig.class)
public class EmailRequestTest {
    @Autowired
    private MockMvc mvc;

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
    private ObjectMapper objectMapper;

    private int serviceRequestId;
    private ServiceProviderInfoModel serviceProviderInfoModel;

    private static final String CUSTOMER_EMAIL = "test@localhost";

    private static final String SERVICE_PROVIDER_EMAIL = "example@example.com";

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

        ServiceRequest serviceRequest = new ServiceRequest(customer);
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
        serviceRequestId = serviceRequest.getId();
    }

    @BeforeEach
    void setupServiceProvider() {
        Account providerAccount = new Account();
        providerAccount.setEmail(SERVICE_PROVIDER_EMAIL);
        providerAccount.setType(AccountType.SERVICE_PROVIDER);
        providerAccount.setVerified(true);
        providerAccount = accountRepository.save(providerAccount);

        ServiceProvider serviceProvider = new ServiceProvider(providerAccount);
        serviceProvider.setName("Sakura HVAC and Plumbing");
        serviceProvider.setDescription("Heating, cooling, and plumbing");
        serviceProvider.setServices(List.of("HVAC", "Plumbing"));
        serviceProvider.setPhoneNumber("1231231234");
        serviceProvider.setAddress("201 Mullica Hill Rd, Glassboro, NJ 08028");
        serviceProvider.setContactEmailAddress(SERVICE_PROVIDER_EMAIL);
        serviceProvider.setLongitude(39.709824);
        serviceProvider.setLatitude(-75.1206862);
        serviceProvider = serviceProviderRepository.save(serviceProvider);
        serviceProviderInfoModel = ServiceProviderInfoModel.fromEntity(serviceProvider);
    }

    @Test
    @WithMockUser(username = SERVICE_PROVIDER_EMAIL, authorities = {"SERVICE_PROVIDER", "VERIFIED"})
    void getEmailNoRequest() throws Exception {
        // No email requests
        assertTrue(emailRequestRepository.findAll().isEmpty());
        // Try to get the email without sending a request
        EmailRequestModel emailRequestModel = getEmailRequest(serviceRequestId);
        assertNotNull(emailRequestModel);
        // email is not supplied
        assertNull(emailRequestModel.email());
        assertEquals(EmailRequestStatus.NOT_REQUESTED, emailRequestModel.status());
    }

    @Test
    @WithMockUser(username = SERVICE_PROVIDER_EMAIL, authorities = {"SERVICE_PROVIDER", "VERIFIED"})
    void getEmailWithRequest() throws Exception {
        // No email requests
        assertTrue(emailRequestRepository.findAll().isEmpty());
        // Send an email request
        this.mvc.perform(post("/api/provider/service_requests/{1}/email/request", serviceRequestId))
                .andExpect(status().isOk());
        assertEquals(1, emailRequestRepository.findAll().size());
        EmailRequest emailRequest = emailRequestRepository.findAll().get(0);
        // status is REQUESTED
        assertEquals(EmailRequestStatus.REQUESTED, emailRequest.getStatus());

        // Try to get the email with a request that has not been accepted or denied
        EmailRequestModel emailRequestModel = getEmailRequest(serviceRequestId);
        assertNotNull(emailRequestModel);
        // email is not supplied
        assertNull(emailRequestModel.email());
        assertEquals(EmailRequestStatus.REQUESTED, emailRequestModel.status());

        emailRequest.setStatus(EmailRequestStatus.ACCEPTED);
        emailRequest = emailRequestRepository.save(emailRequest);
        // Email is provided after the request is accepted
        emailRequestModel = getEmailRequest(serviceRequestId);
        assertNotNull(emailRequestModel);
        assertEquals(CUSTOMER_EMAIL, emailRequestModel.email());
        assertEquals(EmailRequestStatus.ACCEPTED, emailRequestModel.status());

        emailRequest.setStatus(EmailRequestStatus.REJECTED);
        emailRequest = emailRequestRepository.save(emailRequest);
        // Email is not provided when the request is REJECTED
        emailRequestModel = getEmailRequest(serviceRequestId);
        assertNotNull(emailRequestModel);
        // email is not supplied
        assertNull(emailRequestModel.email());
        assertEquals(EmailRequestStatus.REJECTED, emailRequestModel.status());
    }

    @Test
    @WithMockUser(username = SERVICE_PROVIDER_EMAIL, authorities = {"SERVICE_PROVIDER", "VERIFIED"})
    void getEmailNonExistentServiceRequest() throws Exception {
        // Try to get the email for a non-existent post results in an error
        this.mvc.perform(get("/api/provider/service_requests/{1}/email", Integer.MAX_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("non_existent_post"));
    }

    @Test
    @WithMockUser(username = SERVICE_PROVIDER_EMAIL, authorities = {"SERVICE_PROVIDER", "VERIFIED"})
    void doubleEmailRequest() throws Exception {
        // No email requests
        assertTrue(emailRequestRepository.findAll().isEmpty());
        // Sending an email request is successful
        this.mvc.perform(post("/api/provider/service_requests/{1}/email/request", serviceRequestId))
                .andExpect(status().isOk());
        assertEquals(1, emailRequestRepository.findAll().size());
        // Sending another email request results in an error
        this.mvc.perform(post("/api/provider/service_requests/{1}/email/request", serviceRequestId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("already_requested"));
        // no additional email requets are created
        assertEquals(1, emailRequestRepository.findAll().size());
    }

    @Test
    @WithMockUser(username = SERVICE_PROVIDER_EMAIL, authorities = {"SERVICE_PROVIDER", "VERIFIED"})
    @Transactional
    void sendEmailRequest() throws Exception {
        // No email requests
        assertTrue(emailRequestRepository.findAll().isEmpty());
        Instant start = Instant.now();
        // Send an email request
        this.mvc.perform(post("/api/provider/service_requests/{1}/email/request", serviceRequestId))
                .andExpect(status().isOk());
        // Email request is created
        assertEquals(1, emailRequestRepository.findAll().size());
        EmailRequest emailRequest = emailRequestRepository.findAll().get(0);
        // from the current ServiceProvider
        assertEquals(SERVICE_PROVIDER_EMAIL, emailRequest.getServiceProvider().getAccount().getEmail());
        // for the right ServiceRequest
        assertEquals(serviceRequestId, emailRequest.getServiceRequest().getId());
        assertEquals(CUSTOMER_EMAIL, emailRequest.getServiceRequest().getCustomer().getAccount().getEmail());
        // and the current status is REQUESTED
        assertEquals(EmailRequestStatus.REQUESTED, emailRequest.getStatus());
        // with a sensible timestamp
        assertTrue(emailRequest.getRequestTimestamp().isAfter(start));
        // TODO check for notification?
    }

    @Test
    @WithMockUser(username = CUSTOMER_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    void acceptEmailRequest() throws Exception {
        // No email requests
        assertTrue(emailRequestRepository.findAll().isEmpty());
        // Create an email request
        ServiceProvider serviceProvider = serviceProviderRepository.findByAccountEmail(SERVICE_PROVIDER_EMAIL);
        ServiceRequest serviceRequest = serviceRequestRepository.findByIdAndCustomerAccountEmail(serviceRequestId, CUSTOMER_EMAIL);
        EmailRequest emailRequest = new EmailRequest(serviceProvider, serviceRequest);
        emailRequest = emailRequestRepository.save(emailRequest);
        // Customer can get a list of email requests for a ServiceRequest
        EmailRequestInfoModel[] models = listEmailRequests(serviceRequestId);
        assertEquals(1, models.length);
        EmailRequestInfoModel model = models[0];
        // Model matches repository information
        assertEquals(emailRequest.getId(), model.id());
        assertEquals(serviceProviderInfoModel, model.serviceProvider());
        assertEquals(EmailRequestStatus.REQUESTED, model.status()); // Status is REQUESTED
        assertEquals(emailRequest.getStatus(), model.status());
        // Customer can accept an email request
        updateEmailRequestStatus(serviceRequestId, emailRequest.getId(), true);
        // Status is updated
        models = listEmailRequests(serviceRequestId);
        assertEquals(1, models.length);
        model = models[0];
        // to ACCEPTED
        assertEquals(EmailRequestStatus.ACCEPTED, model.status());
        // Customer can reject an email request
        updateEmailRequestStatus(serviceRequestId, emailRequest.getId(), false);
        // Status is updated
        models = listEmailRequests(serviceRequestId);
        assertEquals(1, models.length);
        model = models[0];
        // to DENIED
        assertEquals(EmailRequestStatus.REJECTED, model.status());
        // TODO check for notification?
    }

    @Test
    @WithMockUser(username = CUSTOMER_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    void listEmailRequestNonExistentServiceRequest() throws Exception {
        // Trying to list the email requests for a nonexistent service request fails
        this.mvc.perform(get("/api/customer/service_request/{1}/email_requests", Integer.MAX_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("non_existent_post"));
    }

    @Test
    @WithMockUser(username = CUSTOMER_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    void acceptNonExistentEmailRequest() throws Exception {
        // Trying to accept an email requests that doesn't exist fails
        this.mvc.perform(post("/api/customer/service_request/{service_request_id}/email_requests/{email_request_id}/accepted", serviceRequestId, Integer.MAX_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("true"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("non_existent_email_request"));
    }

    private EmailRequestModel getEmailRequest(int serviceRequestId) throws Exception {
        MvcResult result = this.mvc.perform(get("/api/provider/service_requests/{1}/email", serviceRequestId))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), EmailRequestModel.class);
    }

    private EmailRequestInfoModel[] listEmailRequests(int serviceRequestId) throws Exception {
        MvcResult result = this.mvc.perform(get("/api/customer/service_request/{1}/email_requests", serviceRequestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), EmailRequestInfoModel[].class);
    }

    private void updateEmailRequestStatus(int serviceRequestId, long emailRequestId, boolean accepted) throws Exception {
        this.mvc.perform(post("/api/customer/service_request/{service_request_id}/email_requests/{email_request_id}/accepted", serviceRequestId, emailRequestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(accepted)))
                .andExpect(status().isOk());
    }
}

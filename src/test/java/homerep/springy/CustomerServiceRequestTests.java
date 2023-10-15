package homerep.springy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import homerep.springy.authorities.AccountType;
import homerep.springy.entity.Account;
import homerep.springy.entity.Customer;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.model.ServiceRequestModel;
import homerep.springy.repository.AccountRepository;
import homerep.springy.repository.CustomerRepository;
import homerep.springy.repository.ServiceRequestRepository;
import homerep.springy.service.ResetService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Date;
import java.util.List;

import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CustomerServiceRequestTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private ResetService resetService;

    private static final String TEST_EMAIL = "example@example.com";

    private static final ServiceRequestModel VALID_SERVICE_REQUEST = new ServiceRequestModel(
            "Test", "Description", 32, "201 Mullica Hill Rd, Glassboro, NJ 08028"
    );
    private static final ServiceRequestModel VALID_SERVICE_REQUEST_WITH_ID = new ServiceRequestModel(
            Integer.MAX_VALUE, "Test", "Description", 32, "201 Mullica Hill Rd, Glassboro, NJ 08028"
    );
    private static final ServiceRequestModel MODIFIED_VALID_SERVICE_REQUEST_WITH_ID = new ServiceRequestModel(
            Integer.MAX_VALUE, "Different Title", "Different Description", 42, "201 Rowan Blvd, Glassboro, NJ 08028"
    );

    private static final ServiceRequestModel INVALID_SERVICE_REQUEST = new ServiceRequestModel(
            "", "", -1, ""
    );

    @BeforeEach
    @Transactional
    void reset() {
        resetService.resetAll();

        Account account = new Account();
        account.setEmail(TEST_EMAIL);
        account.setPassword(null); // Not testing auth
        account.setType(AccountType.CUSTOMER);
        account.setVerified(true);
        account = accountRepository.save(account);

        Customer customer = new Customer(account);
        customer.setFirstName("Zoey");
        customer.setMiddleName(""); // TODO remove
        customer.setLastName("Proasheck");
        customer.setAddress("201 Mullica Hill Rd, Glassboro, NJ 08028");
        customerRepository.save(customer);
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    void customerCreateListTest() throws Exception {
        Date now = new Date();
        // Customer can create a service request
        this.mvc.perform(createServiceRequest(VALID_SERVICE_REQUEST))
                .andExpect(status().isOk());
        // It saved to the repository and is associated with the Customer
        List<ServiceRequest> serviceRequests = serviceRequestRepository.findAllByCustomerAccountEmail(TEST_EMAIL);
        assertEquals(1, serviceRequests.size());
        // with all the data
        ServiceRequest serviceRequest = serviceRequests.get(0);
        assertEquals(VALID_SERVICE_REQUEST.title(), serviceRequest.getTitle());
        assertEquals(VALID_SERVICE_REQUEST.description(), serviceRequest.getDescription());
        assertEquals(VALID_SERVICE_REQUEST.dollars(), serviceRequest.getDollars());
        assertNotNull(serviceRequest.getDate());
        assertTrue(serviceRequest.getDate().after(now));
        assertEquals(VALID_SERVICE_REQUEST.address(), serviceRequest.getAddress());
        // Created request is listed
        this.mvc.perform(get("/api/customer/service_request"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].id").value(serviceRequest.getId()))
                .andExpect(jsonPath("$[0].title").value(VALID_SERVICE_REQUEST.title()))
                .andExpect(jsonPath("$[0].description").value(VALID_SERVICE_REQUEST.description()))
                .andExpect(jsonPath("$[0].dollars").value(VALID_SERVICE_REQUEST.dollars()))
                .andExpect(jsonPath("$[0].address").value(VALID_SERVICE_REQUEST.address()));
        // Customer can create a second service request and id is ignored
        this.mvc.perform(createServiceRequest(VALID_SERVICE_REQUEST_WITH_ID))
                .andExpect(status().isOk());
        // Both requests are listed
        this.mvc.perform(get("/api/customer/service_request"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].id").value(not(VALID_SERVICE_REQUEST_WITH_ID.id())))
                .andExpect(jsonPath("$[0].title").value(VALID_SERVICE_REQUEST.title()))
                .andExpect(jsonPath("$[0].description").value(VALID_SERVICE_REQUEST.description()))
                .andExpect(jsonPath("$[0].dollars").value(VALID_SERVICE_REQUEST.dollars()))
                .andExpect(jsonPath("$[0].address").value(VALID_SERVICE_REQUEST.address()))
                .andExpect(jsonPath("$[1].id").isNumber())
                .andExpect(jsonPath("$[1].id").value(not(VALID_SERVICE_REQUEST_WITH_ID.id())))
                .andExpect(jsonPath("$[1].title").value(VALID_SERVICE_REQUEST_WITH_ID.title()))
                .andExpect(jsonPath("$[1].description").value(VALID_SERVICE_REQUEST_WITH_ID.description()))
                .andExpect(jsonPath("$[1].dollars").value(VALID_SERVICE_REQUEST_WITH_ID.dollars()))
                .andExpect(jsonPath("$[0].address").value(VALID_SERVICE_REQUEST_WITH_ID.address()));
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    void createEditDeleteTest() throws Exception {
        Date now = new Date();
        // Customer can create a service request
        this.mvc.perform(createServiceRequest(VALID_SERVICE_REQUEST))
                .andExpect(status().isOk());
        // It saved to the repository and is associated with the Customer
        List<ServiceRequest> serviceRequests = serviceRequestRepository.findAllByCustomerAccountEmail(TEST_EMAIL);
        assertEquals(1, serviceRequests.size());
        // with all the data
        ServiceRequest serviceRequest = serviceRequests.get(0);
        assertEquals(VALID_SERVICE_REQUEST.title(), serviceRequest.getTitle());
        assertEquals(VALID_SERVICE_REQUEST.description(), serviceRequest.getDescription());
        assertEquals(VALID_SERVICE_REQUEST.dollars(), serviceRequest.getDollars());
        assertNotNull(serviceRequest.getDate());
        assertTrue(serviceRequest.getDate().after(now));
        assertEquals(VALID_SERVICE_REQUEST.address(), serviceRequest.getAddress());

        // Trying to edit with wrong id is rejected
        this.mvc.perform(editServiceRequest(Integer.MAX_VALUE, MODIFIED_VALID_SERVICE_REQUEST_WITH_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("non_existent_post"));
        // and nothing changes
        serviceRequests = serviceRequestRepository.findAllByCustomerAccountEmail(TEST_EMAIL);
        assertEquals(1, serviceRequests.size());
        serviceRequest = serviceRequests.get(0);

        assertEquals(VALID_SERVICE_REQUEST.title(), serviceRequest.getTitle());
        assertEquals(VALID_SERVICE_REQUEST.description(), serviceRequest.getDescription());
        assertEquals(VALID_SERVICE_REQUEST.dollars(), serviceRequest.getDollars());
        assertNotNull(serviceRequest.getDate());
        assertTrue(serviceRequest.getDate().after(now));
        assertEquals(VALID_SERVICE_REQUEST.address(), serviceRequest.getAddress());

        // Can modify post with the correct id
        int id = serviceRequest.getId();
        Date lastDate = serviceRequest.getDate();

        this.mvc.perform(editServiceRequest(id, MODIFIED_VALID_SERVICE_REQUEST_WITH_ID))
                .andExpect(status().isOk());
        // with the data modified in the repository
        serviceRequests = serviceRequestRepository.findAllByCustomerAccountEmail(TEST_EMAIL);
        assertEquals(1, serviceRequests.size());
        serviceRequest = serviceRequests.get(0);

        assertEquals(id, serviceRequest.getId()); // The id stays the same
        assertEquals(MODIFIED_VALID_SERVICE_REQUEST_WITH_ID.title(), serviceRequest.getTitle());
        assertEquals(MODIFIED_VALID_SERVICE_REQUEST_WITH_ID.description(), serviceRequest.getDescription());
        assertEquals(MODIFIED_VALID_SERVICE_REQUEST_WITH_ID.dollars(), serviceRequest.getDollars());
        assertNotNull(serviceRequest.getDate());
        assertEquals(lastDate, serviceRequest.getDate()); // Posted date is not changed
        assertEquals(MODIFIED_VALID_SERVICE_REQUEST_WITH_ID.address(), serviceRequest.getAddress());

        // Trying to delete with wrong id is rejected
        this.mvc.perform(deleteServiceRequest(Integer.MAX_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("non_existent_post"));
        // and post still exists
        serviceRequests = serviceRequestRepository.findAllByCustomerAccountEmail(TEST_EMAIL);
        assertEquals(1, serviceRequests.size());

        // Delete with correct id is successful
        this.mvc.perform(deleteServiceRequest(id))
                .andExpect(status().isOk());
        // and the post no longer exists
        serviceRequests = serviceRequestRepository.findAllByCustomerAccountEmail(TEST_EMAIL);
        assertTrue(serviceRequests.isEmpty());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, authorities = {"SERVICE_PROVIDER", "VERIFIED"})
    void serviceProviderCreateTest() throws Exception {
        // Service providers can't create requests
        this.mvc.perform(createServiceRequest(VALID_SERVICE_REQUEST))
                .andExpect(status().isForbidden());
        // or list requests
        this.mvc.perform(get("/api/customer/service_request"))
                .andExpect(status().isForbidden());
        // or edit requests
        this.mvc.perform(editServiceRequest(0, VALID_SERVICE_REQUEST))
                .andExpect(status().isForbidden());
        // or delete requests
        this.mvc.perform(deleteServiceRequest(0))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, authorities = {"CUSTOMER"})
    void unverifiedCustomerCreateTest() throws Exception {
        // Unverified customers can't create requests
        this.mvc.perform(createServiceRequest(VALID_SERVICE_REQUEST))
                .andExpect(status().isForbidden());
        // or list requests
        this.mvc.perform(get("/api/customer/service_request"))
                .andExpect(status().isForbidden());
        // or edit requests
        this.mvc.perform(editServiceRequest(0, VALID_SERVICE_REQUEST))
                .andExpect(status().isForbidden());
        // or delete requests
        this.mvc.perform(deleteServiceRequest(0))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedCreateTest() throws Exception {
        // Not logged in Customers can't create requests
        this.mvc.perform(createServiceRequest(VALID_SERVICE_REQUEST))
                .andExpect(status().isForbidden());
        // or list requests
        this.mvc.perform(get("/api/customer/service_request"))
                .andExpect(status().isForbidden());
        // or edit requests
        this.mvc.perform(editServiceRequest(0, VALID_SERVICE_REQUEST))
                .andExpect(status().isForbidden());
        // or delete requests
        this.mvc.perform(deleteServiceRequest(0))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    void postValidation() throws Exception {
        this.mvc.perform(createServiceRequest(INVALID_SERVICE_REQUEST))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("validation_error"))
                .andExpect(jsonPath("fieldErrors").isArray())
                .andExpect(jsonPath("fieldErrors").isNotEmpty())
                .andExpect(jsonPath("objectErrors").isArray())
                .andExpect(jsonPath("objectErrors").isEmpty());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    void editPostValidation() throws Exception {
        this.mvc.perform(editServiceRequest(0, INVALID_SERVICE_REQUEST))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("validation_error"))
                .andExpect(jsonPath("fieldErrors").isArray())
                .andExpect(jsonPath("fieldErrors").isNotEmpty())
                .andExpect(jsonPath("objectErrors").isArray())
                .andExpect(jsonPath("objectErrors").isEmpty());
    }

    private MockHttpServletRequestBuilder createServiceRequest(ServiceRequestModel serviceRequestModel) throws JsonProcessingException {
        return post("/api/customer/service_request/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(serviceRequestModel));
    }

    private MockHttpServletRequestBuilder editServiceRequest(int id, ServiceRequestModel serviceRequestModel) throws JsonProcessingException {
        return post("/api/customer/service_request/{id}/edit", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(serviceRequestModel));
    }

    private MockHttpServletRequestBuilder deleteServiceRequest(int id) {
        return delete("/api/customer/service_request/{id}", id);
    }
}

package homerep.springy.customer;

import homerep.springy.component.DummyDataComponent;
import homerep.springy.entity.Account;
import homerep.springy.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests that validate the authorization of various customer/service_request endpoints
 */
public class AuthServiceRequestTests extends AbstractServiceRequestTests {
    @Autowired
    private DummyDataComponent dummyDataComponent;

    private static final String SERVICE_PROVIDER_EMAIL = "test@localhost";
    private static final String UNVERIFIED_EMAIL = "test2@localhost";

    @BeforeEach
    void setupExtraAccounts() {
        dummyDataComponent.createServiceProvider(SERVICE_PROVIDER_EMAIL);
        Customer unverifiedCustomer = dummyDataComponent.createCustomer(UNVERIFIED_EMAIL);
        Account account = unverifiedCustomer.getAccount();
        account.setVerified(false);
        accountRepository.save(account);
    }

    @Test
    @WithUserDetails(value = SERVICE_PROVIDER_EMAIL, setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void serviceProviderCreateTest() throws Exception {
        // Service providers can't create requests
        this.mvc.perform(createServiceRequest(VALID_SERVICE_REQUEST))
                .andExpect(status().isForbidden());
        assertTrue(serviceRequestRepository.findAll().isEmpty());
        // or list requests
        this.mvc.perform(getServiceRequests())
                .andExpect(status().isForbidden());
        // or get a specific request
        this.mvc.perform(getServiceRequest(0))
                .andExpect(status().isForbidden());
        // or edit requests
        this.mvc.perform(editServiceRequest(0, VALID_SERVICE_REQUEST))
                .andExpect(status().isForbidden());
        // or delete requests
        this.mvc.perform(deleteServiceRequest(0))
                .andExpect(status().isForbidden());
        // or attach photos
        this.mvc.perform(attachPhoto(0, "file", MediaType.IMAGE_PNG_VALUE, createImage(2, 2, "PNG")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = UNVERIFIED_EMAIL, setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void unverifiedCustomerCreateTest() throws Exception {
        // Unverified customers can't create requests
        this.mvc.perform(createServiceRequest(VALID_SERVICE_REQUEST))
                .andExpect(status().isForbidden());
        assertTrue(serviceRequestRepository.findAll().isEmpty());
        // or list requests
        this.mvc.perform(getServiceRequests())
                .andExpect(status().isForbidden());
        // or get a specific request
        this.mvc.perform(getServiceRequest(0))
                .andExpect(status().isForbidden());
        // or edit requests
        this.mvc.perform(editServiceRequest(0, VALID_SERVICE_REQUEST))
                .andExpect(status().isForbidden());
        // or delete requests
        this.mvc.perform(deleteServiceRequest(0))
                .andExpect(status().isForbidden());
        // or attach photos
        this.mvc.perform(attachPhoto(0, "file", MediaType.IMAGE_PNG_VALUE, createImage(2, 2, "PNG")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void unauthenticatedCreateTest() throws Exception {
        // Not logged-in users can't create requests
        this.mvc.perform(createServiceRequest(VALID_SERVICE_REQUEST))
                .andExpect(status().isForbidden());
        assertTrue(serviceRequestRepository.findAll().isEmpty());
        // or list requests
        this.mvc.perform(getServiceRequests())
                .andExpect(status().isForbidden());
        // or get a specific request
        this.mvc.perform(getServiceRequest(0))
                .andExpect(status().isForbidden());
        // or edit requests
        this.mvc.perform(editServiceRequest(0, VALID_SERVICE_REQUEST))
                .andExpect(status().isForbidden());
        // or delete requests
        this.mvc.perform(deleteServiceRequest(0))
                .andExpect(status().isForbidden());
        // or attach photos
        this.mvc.perform(attachPhoto(0, "file", MediaType.IMAGE_PNG_VALUE, createImage(2, 2, "PNG")))
                .andExpect(status().isForbidden());
    }
}

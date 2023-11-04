package homerep.springy.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import homerep.springy.authorities.AccountType;
import homerep.springy.config.TestDatabaseConfig;
import homerep.springy.config.TestStorageConfig;
import homerep.springy.entity.Account;
import homerep.springy.entity.Customer;
import homerep.springy.entity.ServiceProvider;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.model.ServiceRequestModel;
import homerep.springy.repository.AccountRepository;
import homerep.springy.repository.CustomerRepository;
import homerep.springy.repository.ServiceProviderRepository;
import homerep.springy.repository.ServiceRequestRepository;
import homerep.springy.type.LatLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestDatabaseConfig
@Import(TestStorageConfig.class)
public class ServiceProviderServiceRequestTests {

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
    private ObjectMapper mapper;

    private ServiceRequestModel serviceRequestA;

    private static final String CUSTOMER_EMAIL = "customer@localhost";
    private static final String SERVICE_PROVIDER_EMAIL = "serviceprovider@localhost";

    protected static final LatLong SERVICE_REQUEST_LOCATION = new LatLong(39.709824, -75.1206862);

    @BeforeEach
    @WithMockUser(username = CUSTOMER_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    void setupCustomer() throws Exception {
        Account customerAccount = new Account();
        customerAccount.setEmail(CUSTOMER_EMAIL);
        customerAccount.setPassword(null); // Not testing auth
        customerAccount.setType(AccountType.CUSTOMER);
        customerAccount.setVerified(true);
        customerAccount = accountRepository.save(customerAccount);

        Customer customer = new Customer(customerAccount);
        customer.setFirstName("Zoey");
        customer.setMiddleName(""); // TODO remove
        customer.setLastName("Proasheck");
        customer.setAddress("");
        customerRepository.save(customer);

        ServiceRequest serviceRequest = new ServiceRequest(customer);
        serviceRequest.setTitle("test");
        serviceRequest.setDescription("description");
        serviceRequest.setService("HVAC");
        serviceRequest.setStatus(ServiceRequest.Status.PENDING);
        serviceRequest.setDollars(100);
        serviceRequest.setCreationDate(new Date());
        serviceRequest.setAddress("201 Mullica Hill Rd, Glassboro, NJ 08028");
        serviceRequest.setLongitude(SERVICE_REQUEST_LOCATION.longitude());
        serviceRequest.setLatitude(SERVICE_REQUEST_LOCATION.latitude());
        serviceRequest = serviceRequestRepository.save(serviceRequest);

        serviceRequestA = ServiceRequestModel.fromEntity(serviceRequest);
    }

    @BeforeEach
    void setupProvider() {
        Account providerAccount = new Account();
        providerAccount.setEmail(SERVICE_PROVIDER_EMAIL);
        providerAccount.setPassword(null); // Not testing auth
        providerAccount.setType(AccountType.SERVICE_PROVIDER);
        providerAccount.setVerified(true);
        providerAccount = accountRepository.save(providerAccount);

        ServiceProvider serviceProvider = new ServiceProvider(providerAccount);
        serviceProvider.setName("Sakura HVAC and Plumbing");
        serviceProvider.setDescription("");
        serviceProvider.setServices(List.of("HVAC", "Plumbing"));
        serviceProvider.setPhoneNumber("");
        serviceProvider.setAddress("201 Mullica Hill Rd, Glassboro, NJ 08028");
        serviceProvider.setContactEmailAddress(SERVICE_PROVIDER_EMAIL);
        serviceProvider.setLongitude(39.709824);
        serviceProvider.setLatitude(-75.1206862);
        serviceProvider = serviceProviderRepository.save(serviceProvider);
    }

    @Test
    @WithMockUser(username = SERVICE_PROVIDER_EMAIL, authorities = {"SERVICE_PROVIDER", "VERIFIED"})
    void testQueryAll() throws Exception {
        MvcResult result = this.mvc.perform(get("/api/provider/service_requests/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();
        // response is an array of ServiceRequestModels
        ServiceRequestModel[] models = mapper.readValue(result.getResponse().getContentAsString(), ServiceRequestModel[].class);
        assertEquals(1, models.length);
        assertEquals(serviceRequestA, models[0]);
    }

    @Test
    @WithMockUser(username = SERVICE_PROVIDER_EMAIL, authorities = {"SERVICE_PROVIDER", "VERIFIED"})
    void testQueryRange() throws Exception {
        MvcResult result = this.mvc.perform(get("/api/provider/service_requests/nearby")
                        .queryParam("latitudeS", String.valueOf(SERVICE_REQUEST_LOCATION.latitude() - 1))
                        .queryParam("latitudeN", String.valueOf(SERVICE_REQUEST_LOCATION.latitude() + 1))
                        .queryParam("longitudeW", String.valueOf(SERVICE_REQUEST_LOCATION.longitude() - 1))
                        .queryParam("longitudeE", String.valueOf(SERVICE_REQUEST_LOCATION.longitude() + 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();
        // response is an array of ServiceRequestModels
        ServiceRequestModel[] models = mapper.readValue(result.getResponse().getContentAsString(), ServiceRequestModel[].class);
        assertEquals(1, models.length);
        assertEquals(serviceRequestA, models[0]);
    }

    @Test
    @WithMockUser(username = SERVICE_PROVIDER_EMAIL, authorities = {"SERVICE_PROVIDER", "VERIFIED"})
    void testQueryOutOfRange() throws Exception {
        MvcResult result = this.mvc.perform(get("/api/provider/service_requests/nearby")
                        .queryParam("latitudeS", String.valueOf(SERVICE_REQUEST_LOCATION.latitude() - 3))
                        .queryParam("longitudeW", String.valueOf(SERVICE_REQUEST_LOCATION.longitude() - 3))
                        .queryParam("latitudeN", String.valueOf(SERVICE_REQUEST_LOCATION.latitude() - 1))
                        .queryParam("longitudeE", String.valueOf(SERVICE_REQUEST_LOCATION.longitude() - 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();
        // response is an array of ServiceRequestModels
        ServiceRequestModel[] models = mapper.readValue(result.getResponse().getContentAsString(), ServiceRequestModel[].class);
        assertEquals(0, models.length);
    }
}

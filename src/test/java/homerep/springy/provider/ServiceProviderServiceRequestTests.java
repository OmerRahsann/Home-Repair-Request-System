package homerep.springy.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import homerep.springy.component.DummyDataComponent;
import homerep.springy.config.TestDatabaseConfig;
import homerep.springy.config.TestStorageConfig;
import homerep.springy.entity.Customer;
import homerep.springy.entity.ServiceProvider;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.model.ServiceRequestModel;
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

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private DummyDataComponent dummyDataComponent;

    @Autowired
    private ObjectMapper mapper;

    private ServiceRequestModel serviceRequestModel;

    private ServiceRequestModel serviceRequestModelPos179;
    private ServiceRequestModel serviceRequestModelNeg179;

    private static final String CUSTOMER_EMAIL = "customer@localhost";
    private static final String SERVICE_PROVIDER_EMAIL = "serviceprovider@localhost";

    protected static final LatLong SERVICE_REQUEST_LOCATION = new LatLong(39.709824, -75.1206862);

    private ServiceRequest createServiceRequestAt(Customer customer, double latitude, double longitude) {
        ServiceRequest serviceRequest = dummyDataComponent.createServiceRequest(customer);
        serviceRequest.setAddress(latitude + "," + longitude);
        serviceRequest.setLongitude(longitude);
        serviceRequest.setLatitude(latitude);
        return serviceRequestRepository.save(serviceRequest);
    }

    @BeforeEach
    @WithMockUser(username = CUSTOMER_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    void setupCustomer() {
        Customer customer = dummyDataComponent.createCustomer(CUSTOMER_EMAIL);

        ServiceRequest serviceRequestA = createServiceRequestAt(customer, SERVICE_REQUEST_LOCATION.latitude(), SERVICE_REQUEST_LOCATION.longitude());
        serviceRequestModel = ServiceRequestModel.fromEntity(serviceRequestA);

        ServiceRequest serviceRequestPos179 = createServiceRequestAt(customer, 0, 179.8);
        serviceRequestModelPos179 = ServiceRequestModel.fromEntity(serviceRequestPos179);
        ServiceRequest serviceRequestNeg179 = createServiceRequestAt(customer, 0, -179.8);
        serviceRequestModelNeg179 = ServiceRequestModel.fromEntity(serviceRequestNeg179);
    }

    @BeforeEach
    void setupProvider() {
        ServiceProvider serviceProvider = dummyDataComponent.createServiceProvider(SERVICE_PROVIDER_EMAIL);
        serviceProvider.setAddress("201 Mullica Hill Rd, Glassboro, NJ 08028");
        serviceProvider.setLongitude(39.709824);
        serviceProvider.setLatitude(-75.1206862);
        serviceProviderRepository.save(serviceProvider);
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
        assertEquals(3, models.length);
        assertTrue(Arrays.stream(models).anyMatch(x -> x.equals(serviceRequestModel)));
        assertTrue(Arrays.stream(models).anyMatch(x -> x.equals(serviceRequestModelPos179)));
        assertTrue(Arrays.stream(models).anyMatch(x -> x.equals(serviceRequestModelNeg179)));
    }

    @Test
    @WithMockUser(username = SERVICE_PROVIDER_EMAIL, authorities = {"SERVICE_PROVIDER", "VERIFIED"})
    void testQueryRange() throws Exception {
        MvcResult result = this.mvc.perform(get("/api/provider/service_requests/nearby")
                        .queryParam("latitudeS", String.valueOf(SERVICE_REQUEST_LOCATION.latitude() - 0.2))
                        .queryParam("latitudeN", String.valueOf(SERVICE_REQUEST_LOCATION.latitude() + 0.2))
                        .queryParam("longitudeW", String.valueOf(SERVICE_REQUEST_LOCATION.longitude() - 0.2))
                        .queryParam("longitudeE", String.valueOf(SERVICE_REQUEST_LOCATION.longitude() + 0.2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();
        // response is an array of ServiceRequestModels
        ServiceRequestModel[] models = mapper.readValue(result.getResponse().getContentAsString(), ServiceRequestModel[].class);
        assertEquals(1, models.length);
        assertEquals(serviceRequestModel, models[0]);
    }

    @Test
    @WithMockUser(username = SERVICE_PROVIDER_EMAIL, authorities = {"SERVICE_PROVIDER", "VERIFIED"})
    void testQueryLargeRange() throws Exception {
        MvcResult result = this.mvc.perform(get("/api/provider/service_requests/nearby")
                        .queryParam("latitudeS", String.valueOf(SERVICE_REQUEST_LOCATION.latitude() - 2))
                        .queryParam("latitudeN", String.valueOf(SERVICE_REQUEST_LOCATION.latitude() + 2))
                        .queryParam("longitudeW", String.valueOf(SERVICE_REQUEST_LOCATION.longitude() - 2))
                        .queryParam("longitudeE", String.valueOf(SERVICE_REQUEST_LOCATION.longitude() + 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();
        // response is empty, as the range is too large
        ServiceRequestModel[] models = mapper.readValue(result.getResponse().getContentAsString(), ServiceRequestModel[].class);
        assertEquals(0, models.length);
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

    @Test
    @WithMockUser(username = SERVICE_PROVIDER_EMAIL, authorities = {"SERVICE_PROVIDER", "VERIFIED"})
    void testQueryWrapAround() throws Exception {
        MvcResult result = this.mvc.perform(get("/api/provider/service_requests/nearby")
                        .queryParam("latitudeS", "-0.5")
                        .queryParam("longitudeW", "179.5")
                        .queryParam("latitudeN", "0.5")
                        .queryParam("longitudeE", "-179.5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();
        // response is an array of ServiceRequestModels
        ServiceRequestModel[] models = mapper.readValue(result.getResponse().getContentAsString(), ServiceRequestModel[].class);
        // Both service requests in this range are included
        assertEquals(2, models.length);
        assertTrue(Arrays.stream(models).anyMatch(x -> x.equals(serviceRequestModelPos179)));
        assertTrue(Arrays.stream(models).anyMatch(x -> x.equals(serviceRequestModelNeg179)));
    }
}

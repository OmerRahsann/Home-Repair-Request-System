package homerep.springy.customer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import homerep.springy.component.DummyDataComponent;
import homerep.springy.config.TestDatabaseConfig;
import homerep.springy.config.TestStorageConfig;
import homerep.springy.entity.Customer;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.model.ServiceRequestModel;
import homerep.springy.repository.AccountRepository;
import homerep.springy.repository.CustomerRepository;
import homerep.springy.repository.ServiceRequestRepository;
import homerep.springy.type.LatLong;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestDatabaseConfig
@Import(TestStorageConfig.class)
public abstract class AbstractServiceRequestTests {
    @Autowired
    protected AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    protected ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private DummyDataComponent dummyDataComponent;

    @Autowired
    protected ObjectMapper mapper;

    @Autowired
    protected MockMvc mvc;

    protected Customer customer;

    protected static final String TEST_EMAIL = "example@example.com";

    protected static final String CUSTOMER_ADDRESS = "201 Mullica Hill Rd, Glassboro, NJ 08028";

    protected static final LatLong VALID_REQUEST_LOCATION = new LatLong(39.709824, -75.1206862);
    protected static final ServiceRequestModel VALID_SERVICE_REQUEST = new ServiceRequestModel(
            "Test", "Description", "plumbing", 32, "201 Mullica Hill Rd, Glassboro, NJ 08028"
    );
    protected static final ServiceRequestModel VALID_SERVICE_REQUEST_WITH_ADDITIONAL = new ServiceRequestModel(
            Integer.MAX_VALUE, "Test", "Description", "plumbing", ServiceRequest.Status.COMPLETED, 32, "201 Mullica Hill Rd, Glassboro, NJ 08028", null, new Date(0), null, null
    );

    protected static final LatLong MODIFIED_VALID_REQUEST_LOCATION = new LatLong(39.70534389571724, -75.1132625334005); // Tracking individual atoms :P https://xkcd.com/2170/
    protected static final ServiceRequestModel MODIFIED_VALID_SERVICE_REQUEST_WITH_ADDITIONAl = new ServiceRequestModel(
            Integer.MAX_VALUE, "Different Title", "Different Description", "yardwork", ServiceRequest.Status.IN_PROGRESS, 42, "201 Rowan Blvd, Glassboro, NJ 08028", null, new Date(0), 10.0, 9.0
    );

    protected static final ServiceRequestModel INVALID_SERVICE_REQUEST = new ServiceRequestModel(
            "", "", "", -1, ""
    );

    @BeforeEach
    void setup() {
        customer = dummyDataComponent.createCustomer(TEST_EMAIL);
        customer.setAddress(CUSTOMER_ADDRESS);
        customer = customerRepository.save(customer);
    }

    protected MockHttpServletRequestBuilder createServiceRequest(ServiceRequestModel serviceRequestModel) throws JsonProcessingException {
        return post("/api/customer/service_request/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(serviceRequestModel));
    }

    protected MockHttpServletRequestBuilder getServiceRequest(int id) {
        return get("/api/customer/service_request/{id}", id);
    }

    protected MockHttpServletRequestBuilder getServiceRequests() {
        return get("/api/customer/service_request");
    }

    protected MockHttpServletRequestBuilder editServiceRequest(int id, ServiceRequestModel serviceRequestModel) throws JsonProcessingException {
        return post("/api/customer/service_request/{id}/edit", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(serviceRequestModel));
    }

    protected MockHttpServletRequestBuilder deleteServiceRequest(int id) {
        return delete("/api/customer/service_request/{id}", id);
    }

    protected MockHttpServletRequestBuilder attachPhoto(int id, String name, String contentType, InputStream contentStream) throws IOException {
        return multipart("/api/customer/service_request/{id}/attach", id)
                .file(new MockMultipartFile(name, null, contentType, contentStream));
    }

    protected InputStream createImage(int width, int height, String format) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, format, outputStream);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}

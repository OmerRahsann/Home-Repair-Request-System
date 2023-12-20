package homerep.springy;

import homerep.springy.config.TestDatabaseConfig;
import homerep.springy.controller.ServicesController;
import homerep.springy.entity.ServiceType;
import homerep.springy.repository.ServiceTypeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestDatabaseConfig
public class ServicesControllerTest {
    @Autowired
    private ServicesController servicesController;

    @Autowired
    private ServiceTypeRepository serviceTypeRepository;

    @Test
    void getServices() {
        // Default services are returned when no services are available from the repository
        List<String> services = servicesController.getServices();
        assertTrue(serviceTypeRepository.findAll().isEmpty());
        assertEquals(3, services.size());
        assertTrue(services.contains("Plumbing"));
        assertTrue(services.contains("Roofing"));
        assertTrue(services.contains("Yardwork"));

        // Add a few service types
        serviceTypeRepository.save(new ServiceType("Power washing"));
        serviceTypeRepository.save(new ServiceType("Inspections"));
        // These new service types are returned from the controller
        services = servicesController.getServices();
        assertFalse(serviceTypeRepository.findAll().isEmpty());
        assertEquals(2, services.size());
        // in alphabetical order
        assertEquals("Inspections", services.get(0));
        assertEquals("Power washing", services.get(1));
    }

}

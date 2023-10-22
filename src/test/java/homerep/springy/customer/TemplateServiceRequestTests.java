package homerep.springy.customer;

import homerep.springy.entity.ServiceRequestTemplate;
import homerep.springy.model.ServiceRequestModel;
import homerep.springy.repository.ServiceRequestTemplateRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TemplateServiceRequestTests extends AbstractServiceRequestTests {
    @Autowired
    private ServiceRequestTemplateRepository serviceRequestTemplateRepository;

    private static final ServiceRequestModel UNCLOG_SINK_TEMPLATE = new ServiceRequestModel(
            "Kitchen sink needs unclogging", null, "Plumbing", 400, CUSTOMER_ADDRESS
    );

    private static final ServiceRequestModel ROOF_PATCH_TEMPLATE = new ServiceRequestModel(
            "Roof is leaking and needs to be patched.", null, "Roofwork", 600, CUSTOMER_ADDRESS
    );

    @Test
    @WithMockUser(username = TEST_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    void getTemplates() throws Exception {
        // Customer can get a list of template service requests
        MvcResult result = this.mvc.perform(get("/api/customer/service_request/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty())
                .andReturn();
        // Default templates are returned when no templates are available from the repository
        ServiceRequestModel[] templates = mapper.readValue(result.getResponse().getContentAsString(), ServiceRequestModel[].class);
        assertTrue(serviceRequestTemplateRepository.findAll().isEmpty());
        assertEquals(2, templates.length);
        assertEquals(UNCLOG_SINK_TEMPLATE, templates[0]);
        assertEquals(ROOF_PATCH_TEMPLATE, templates[1]);

        // Add a template to the repository
        ServiceRequestTemplate template = new ServiceRequestTemplate();
        template.setTitle("Test yardwork");
        template.setService("Yardwork");
        template.setDollars(250);
        template = serviceRequestTemplateRepository.save(template);

        // List of templates return the templates from the repository
        result = this.mvc.perform(get("/api/customer/service_request/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty())
                .andReturn();
        templates = mapper.readValue(result.getResponse().getContentAsString(), ServiceRequestModel[].class);
        assertFalse(serviceRequestTemplateRepository.findAll().isEmpty());
        assertEquals(1, templates.length);
        ServiceRequestModel model = templates[0];

        assertNull(model.id());
        assertEquals(template.getTitle(), model.title());
        assertNull(model.description());
        assertEquals(template.getService(), model.service());
        assertNull(model.status());
        assertEquals(template.getDollars(), model.dollars());
        assertEquals(CUSTOMER_ADDRESS, model.address()); // Template starts out with the Customer's address
        assertNull(model.pictures());
        assertNull(model.creationDate());
    }
}

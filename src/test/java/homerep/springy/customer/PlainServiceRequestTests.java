package homerep.springy.customer;

import homerep.springy.entity.ServiceRequest;
import homerep.springy.model.ServiceRequestModel;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests that involve service requests without attached images
 */
public class PlainServiceRequestTests extends AbstractServiceRequestTests {

    @Test
    @WithMockUser(username = TEST_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    @Transactional
    void customerCreate() throws Exception {
        Date now = new Date();
        // Customer can create a service request
        MvcResult result = this.mvc.perform(createServiceRequest(VALID_SERVICE_REQUEST))
                .andExpect(status().isOk())
                .andReturn();
        int id = Integer.parseInt(result.getResponse().getContentAsString());
        // It is saved to the repository and is associated with the Customer
        List<ServiceRequest> serviceRequests = serviceRequestRepository.findAllByCustomerAccountEmail(TEST_EMAIL);
        assertEquals(1, serviceRequests.size());
        // with all the data
        ServiceRequest serviceRequest = serviceRequests.get(0);
        assertEquals(id, serviceRequest.getId()); // ID matches response
        assertEquals(VALID_SERVICE_REQUEST.title(), serviceRequest.getTitle());
        assertEquals(VALID_SERVICE_REQUEST.description(), serviceRequest.getDescription());
        assertEquals(VALID_SERVICE_REQUEST.service(), serviceRequest.getService());
        assertEquals(ServiceRequest.Status.PENDING, serviceRequest.getStatus()); // Created post starts out as PENDING
        assertEquals(VALID_SERVICE_REQUEST.dollars(), serviceRequest.getDollars());
        assertEquals(VALID_SERVICE_REQUEST.address(), serviceRequest.getAddress());
        assertTrue(serviceRequest.getPictures().isEmpty());
        assertNotNull(serviceRequest.getCreationDate());
        assertTrue(serviceRequest.getCreationDate().after(now));
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    void customerCreateGetList() throws Exception {
        Date now = new Date();
        // Customer can create a service request
        MvcResult result = this.mvc.perform(createServiceRequest(VALID_SERVICE_REQUEST))
                .andExpect(status().isOk())
                .andReturn();
        // Customer can get the service request with the given id
        int id = Integer.parseInt(result.getResponse().getContentAsString());
        result = this.mvc.perform(getServiceRequest(id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap()) // isObject
                .andReturn();
        // response is a ServiceRequestModel
        ServiceRequestModel model = mapper.readValue(result.getResponse().getContentAsString(), ServiceRequestModel.class);
        // with customer filled out data + server filled out data
        assertEquals(id, model.id());
        assertEquals(VALID_SERVICE_REQUEST.title(), model.title());
        assertEquals(VALID_SERVICE_REQUEST.description(), model.description());
        assertEquals(VALID_SERVICE_REQUEST.service(), model.service());
        assertEquals(ServiceRequest.Status.PENDING, model.status()); // Created post starts out as PENDING
        assertEquals(VALID_SERVICE_REQUEST.dollars(), model.dollars());
        assertEquals(VALID_SERVICE_REQUEST.address(), model.address());
        assertTrue(model.pictures() == null || model.pictures().isEmpty()); // Created post has no pictures
        assertNotNull(model.creationDate());
        assertTrue(model.creationDate().after(now));

        // It is included in the list of service requests
        result = this.mvc.perform(getServiceRequests())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();
        // response is an array of ServiceRequestModels
        ServiceRequestModel[] models = mapper.readValue(result.getResponse().getContentAsString(), ServiceRequestModel[].class);
        assertEquals(1, models.length);
        assertEquals(model, models[0]);
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    void customerGetNonExistent() throws Exception {
        // Attempting to get a non-existent post does not work
        this.mvc.perform(getServiceRequest(Integer.MAX_VALUE))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    void customerCreateAdditional() throws Exception {
        // Customer can create a service request
        MvcResult result = this.mvc.perform(createServiceRequest(VALID_SERVICE_REQUEST_WITH_ADDITIONAL))
                .andExpect(status().isOk())
                .andReturn();
        // Customer can get the service request with the given id
        int id = Integer.parseInt(result.getResponse().getContentAsString());
        assertNotEquals(VALID_SERVICE_REQUEST_WITH_ADDITIONAL.id(), id); // ID in model is ignored
        result = this.mvc.perform(getServiceRequest(id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap()) // isObject
                .andReturn();
        // response is a ServiceRequestModel
        ServiceRequestModel model = mapper.readValue(result.getResponse().getContentAsString(), ServiceRequestModel.class);
        // with customer filled out data + server filled out data
        assertEquals(id, model.id());
        assertEquals(VALID_SERVICE_REQUEST_WITH_ADDITIONAL.title(), model.title());
        assertEquals(VALID_SERVICE_REQUEST_WITH_ADDITIONAL.description(), model.description());
        assertEquals(VALID_SERVICE_REQUEST_WITH_ADDITIONAL.service(), model.service());
        assertEquals(ServiceRequest.Status.PENDING, model.status()); // Created post starts out as PENDING
        assertEquals(VALID_SERVICE_REQUEST_WITH_ADDITIONAL.dollars(), model.dollars());
        assertEquals(VALID_SERVICE_REQUEST_WITH_ADDITIONAL.address(), model.address());
        assertTrue(model.pictures() == null || model.pictures().isEmpty()); // Created post has no pictures
        assertNotEquals(VALID_SERVICE_REQUEST_WITH_ADDITIONAL.creationDate(), model.creationDate()); // Creation date in model is ignored
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    @Transactional
    void customerCreateEdit() throws Exception {
        // Customer can create a service request
        MvcResult result = this.mvc.perform(createServiceRequest(VALID_SERVICE_REQUEST))
                .andExpect(status().isOk())
                .andReturn();
        int id = Integer.parseInt(result.getResponse().getContentAsString());
        // Get the initial state of the service request
        result = this.mvc.perform(getServiceRequest(id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap()) // isObject
                .andReturn();
        ServiceRequestModel initialModel = mapper.readValue(result.getResponse().getContentAsString(), ServiceRequestModel.class);
        // Edit the service request with a valid ServiceRequestModel with some additional data that is ignored
        this.mvc.perform(editServiceRequest(id, MODIFIED_VALID_SERVICE_REQUEST_WITH_ADDITIONAl))
                .andExpect(status().isOk());
        // Get the new state
        result = this.mvc.perform(getServiceRequest(id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap()) // isObject
                .andReturn();
        ServiceRequestModel model = mapper.readValue(result.getResponse().getContentAsString(), ServiceRequestModel.class);
        // new state matches the modified model
        assertEquals(id, model.id());
        assertEquals(MODIFIED_VALID_SERVICE_REQUEST_WITH_ADDITIONAl.title(), model.title());
        assertEquals(MODIFIED_VALID_SERVICE_REQUEST_WITH_ADDITIONAl.description(), model.description());
        assertEquals(MODIFIED_VALID_SERVICE_REQUEST_WITH_ADDITIONAl.service(), model.service());
        assertEquals(MODIFIED_VALID_SERVICE_REQUEST_WITH_ADDITIONAl.status(), model.status());
        assertEquals(MODIFIED_VALID_SERVICE_REQUEST_WITH_ADDITIONAl.dollars(), model.dollars());
        assertEquals(MODIFIED_VALID_SERVICE_REQUEST_WITH_ADDITIONAl.address(), model.address());
        assertTrue(model.pictures() == null || model.pictures().isEmpty()); // Edited post still has no pictures
        assertNotNull(model.creationDate());
        assertEquals(initialModel.creationDate(), model.creationDate()); // Creation date stays the same
        // Edited service request is saved to the repository
        List<ServiceRequest> serviceRequests = serviceRequestRepository.findAllByCustomerAccountEmail(TEST_EMAIL);
        assertEquals(1, serviceRequests.size());
        // with all the data
        ServiceRequest serviceRequest = serviceRequests.get(0);
        assertEquals(id, serviceRequest.getId());
        assertEquals(MODIFIED_VALID_SERVICE_REQUEST_WITH_ADDITIONAl.title(), serviceRequest.getTitle());
        assertEquals(MODIFIED_VALID_SERVICE_REQUEST_WITH_ADDITIONAl.description(), serviceRequest.getDescription());
        assertEquals(MODIFIED_VALID_SERVICE_REQUEST_WITH_ADDITIONAl.service(), serviceRequest.getService());
        assertEquals(MODIFIED_VALID_SERVICE_REQUEST_WITH_ADDITIONAl.status(), serviceRequest.getStatus());
        assertEquals(MODIFIED_VALID_SERVICE_REQUEST_WITH_ADDITIONAl.dollars(), serviceRequest.getDollars());
        assertEquals(MODIFIED_VALID_SERVICE_REQUEST_WITH_ADDITIONAl.address(), serviceRequest.getAddress());
        assertTrue(serviceRequest.getPictures().isEmpty());
        assertEquals(initialModel.creationDate(), serviceRequest.getCreationDate());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    @Transactional
    void customerCreateEditStatus() throws Exception {
        // Customer can create a service request
        MvcResult result = this.mvc.perform(createServiceRequest(VALID_SERVICE_REQUEST))
                .andExpect(status().isOk())
                .andReturn();
        int id = Integer.parseInt(result.getResponse().getContentAsString());
        // Get the initial state of the service request
        result = this.mvc.perform(getServiceRequest(id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap()) // isObject
                .andReturn();
        ServiceRequestModel initialModel = mapper.readValue(result.getResponse().getContentAsString(), ServiceRequestModel.class);
        // Edit the service request with a null status
        ServiceRequestModel editedModel = new ServiceRequestModel(initialModel.id(), initialModel.title(),
                initialModel.description(), initialModel.service(), null, initialModel.dollars(),
                initialModel.address(), initialModel.pictures(), initialModel.creationDate());
        this.mvc.perform(editServiceRequest(id, editedModel))
                .andExpect(status().isOk());
        // Get the new state
        result = this.mvc.perform(getServiceRequest(id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap()) // isObject
                .andReturn();
        ServiceRequestModel model = mapper.readValue(result.getResponse().getContentAsString(), ServiceRequestModel.class);
        // Nothing changes
        assertEquals(initialModel, model);
        assertEquals(ServiceRequest.Status.PENDING, model.status()); // Status should still be PENDING

        // Edit the service request with a new status
        editedModel = new ServiceRequestModel(model.id(), model.title(), model.description(), model.service(),
                ServiceRequest.Status.COMPLETED, model.dollars(), model.address(), model.pictures(),
                model.creationDate());
        this.mvc.perform(editServiceRequest(id, editedModel))
                .andExpect(status().isOk());
        // Get the new state
        result = this.mvc.perform(getServiceRequest(id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap()) // isObject
                .andReturn();
        model = mapper.readValue(result.getResponse().getContentAsString(), ServiceRequestModel.class);
        // Only status changes
        assertEquals(initialModel.title(), model.title());
        assertEquals(initialModel.description(), model.description());
        assertEquals(initialModel.service(), model.service());
        assertEquals(editedModel.status(), model.status());
        assertEquals(initialModel.dollars(), model.dollars());
        assertEquals(initialModel.address(), model.address());
        assertEquals(initialModel.pictures(), model.pictures());
        assertEquals(initialModel.creationDate(), model.creationDate());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    void customerEditNonExistent() throws Exception {
        // Editing a non-existent post does not work
        this.mvc.perform(editServiceRequest(Integer.MAX_VALUE, VALID_SERVICE_REQUEST))
                .andExpect(status().isBadRequest());
        assertTrue(serviceRequestRepository.findAllByCustomerAccountEmail(TEST_EMAIL).isEmpty());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    void customerCreateEditInvalid() throws Exception {
        // Customer can create a service request
        MvcResult result = this.mvc.perform(createServiceRequest(VALID_SERVICE_REQUEST))
                .andExpect(status().isOk())
                .andReturn();
        int id = Integer.parseInt(result.getResponse().getContentAsString());
        // Get the initial state of the service request
        result = this.mvc.perform(getServiceRequest(id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap()) // isObject
                .andReturn();
        ServiceRequestModel initialModel = mapper.readValue(result.getResponse().getContentAsString(), ServiceRequestModel.class);
        // Edit the service request with an invalid ServiceRequestModel
        this.mvc.perform(editServiceRequest(id, INVALID_SERVICE_REQUEST))
                .andExpect(status().isBadRequest());
        // Get the new state
        result = this.mvc.perform(getServiceRequest(id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap()) // isObject
                .andReturn();
        ServiceRequestModel model = mapper.readValue(result.getResponse().getContentAsString(), ServiceRequestModel.class);
        // state is unchanged
        assertEquals(initialModel, model);
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    void customerCreateDelete() throws Exception {
        // Customer can create a service request
        MvcResult result = this.mvc.perform(createServiceRequest(VALID_SERVICE_REQUEST))
                .andExpect(status().isOk())
                .andReturn();
        int id = Integer.parseInt(result.getResponse().getContentAsString());
        assertEquals(1, serviceRequestRepository.findAllByCustomerAccountEmail(TEST_EMAIL).size());
        // Customer can delete a service request with the given id
        this.mvc.perform(deleteServiceRequest(id))
                .andExpect(status().isOk());
        assertTrue(serviceRequestRepository.findAllByCustomerAccountEmail(TEST_EMAIL).isEmpty());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    void customerCreateDeleteInvalid() throws Exception {
        // Customer can create a service request
        this.mvc.perform(createServiceRequest(VALID_SERVICE_REQUEST))
                .andExpect(status().isOk());
        assertEquals(1, serviceRequestRepository.findAllByCustomerAccountEmail(TEST_EMAIL).size());
        // Attempting to delete with an invalid id is rejected
        this.mvc.perform(deleteServiceRequest(Integer.MAX_VALUE))
                .andExpect(status().isBadRequest());
        // repository is not affect
        assertEquals(1, serviceRequestRepository.findAllByCustomerAccountEmail(TEST_EMAIL).size());
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
        MvcResult result = this.mvc.perform(createServiceRequest(VALID_SERVICE_REQUEST))
                .andExpect(status().isOk())
                .andReturn();
        // Customer can get the service request with the given id
        int id = Integer.parseInt(result.getResponse().getContentAsString());
        this.mvc.perform(editServiceRequest(id, INVALID_SERVICE_REQUEST))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("validation_error"))
                .andExpect(jsonPath("fieldErrors").isArray())
                .andExpect(jsonPath("fieldErrors").isNotEmpty())
                .andExpect(jsonPath("objectErrors").isArray())
                .andExpect(jsonPath("objectErrors").isEmpty());
    }
}

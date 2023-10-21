package homerep.springy.customer;

import homerep.springy.entity.Account;
import homerep.springy.entity.ImageInfo;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.model.ServiceRequestModel;
import homerep.springy.repository.ImageInfoRepository;
import homerep.springy.service.ImageStorageService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests that involve service requests with attached images
 */
public class ImageServiceRequestTests extends AbstractServiceRequestTests {
    @Autowired
    private ImageInfoRepository imageInfoRepository;

    @Autowired
    private ImageStorageService imageStorageService;

    private int postId;

    @BeforeEach
    @WithMockUser(username = TEST_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    void setupPost() throws Exception {
        MvcResult result = this.mvc.perform(createServiceRequest(VALID_SERVICE_REQUEST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber())
                .andReturn();
        postId = Integer.parseInt(result.getResponse().getContentAsString());
        ServiceRequest serviceRequest = serviceRequestRepository.findById(postId).orElse(null);
        assertNotNull(serviceRequest);
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    @Transactional
    void attachPictures() throws Exception {
        // Can attach PNG pictures
        this.mvc.perform(attachPhoto(postId, "file", MediaType.IMAGE_PNG_VALUE, createImage(2, 2, "PNG")))
                .andExpect(status().isOk());
        // Can attach JPEG pictures
        this.mvc.perform(attachPhoto(postId, "file", MediaType.IMAGE_JPEG_VALUE, createImage(2, 2, "JPEG")))
                .andExpect(status().isOk());
        // Pictures are stored with the service request in the repository
        ServiceRequest serviceRequest = serviceRequestRepository.findByIdAndCustomerAccountEmail(postId, TEST_EMAIL);
        assertNotNull(serviceRequest);
        assertEquals(2, serviceRequest.getPictures().size());
        assertEquals(2, serviceRequest.getImagesUUIDs().size());
        assertEquals(2, imageInfoRepository.findAll().size());

        // Pictures are included with the service request when requested from the API
        MvcResult result = this.mvc.perform(getServiceRequest(postId))
                .andExpect(status().isOk())
                .andReturn();
        ServiceRequestModel model = mapper.readValue(result.getResponse().getContentAsString(), ServiceRequestModel.class);
        assertNotNull(model.pictures());
        assertEquals(2, model.pictures().size());
        assertNotEquals(model.pictures().get(0), model.pictures().get(1)); // Pictures should be given different UUIDs
        // Pictures are saved to the repository
        for (String picture : model.pictures()) {
            assertTrue(imageInfoRepository.findById(UUID.fromString(picture)).isPresent());
        }
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    @Transactional
    void editAttachedPictures() throws Exception {
        // Can attach PNG pictures
        this.mvc.perform(attachPhoto(postId, "file", MediaType.IMAGE_PNG_VALUE, createImage(2, 2, "PNG")))
                .andExpect(status().isOk());
        // Can attach JPEG pictures
        this.mvc.perform(attachPhoto(postId, "file", MediaType.IMAGE_JPEG_VALUE, createImage(2, 2, "JPEG")))
                .andExpect(status().isOk());
        MvcResult result = this.mvc.perform(getServiceRequest(postId))
                .andExpect(status().isOk())
                .andReturn();
        ServiceRequestModel model = mapper.readValue(result.getResponse().getContentAsString(), ServiceRequestModel.class);
        assertNotNull(model.pictures());
        assertEquals(2, model.pictures().size());
        // Pictures can be reordered by editing the post
        List<String> newOrder = List.of(model.pictures().get(1), model.pictures().get(0));
        ServiceRequestModel editedModel = new ServiceRequestModel(null, model.title(), model.description(),
                model.service(), model.status(), model.dollars(), model.address(), newOrder, model.creationDate());
        this.mvc.perform(editServiceRequest(postId, editedModel))
                .andExpect(status().isOk());

        result = this.mvc.perform(getServiceRequest(postId))
                .andExpect(status().isOk())
                .andReturn();
        model = mapper.readValue(result.getResponse().getContentAsString(), ServiceRequestModel.class);
        assertNotNull(model.pictures());
        assertEquals(2, model.pictures().size());
        // Pictures are still in the repository
        for (String picture : model.pictures()) {
            assertTrue(imageInfoRepository.findById(UUID.fromString(picture)).isPresent());
        }
        // New picture order is applied
        assertEquals(newOrder, model.pictures());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    @Transactional
    void deleteAttachedPicturesWithEdit() throws Exception {
        // Can attach PNG pictures
        this.mvc.perform(attachPhoto(postId, "file", MediaType.IMAGE_PNG_VALUE, createImage(2, 2, "PNG")))
                .andExpect(status().isOk());
        // Can attach JPEG pictures
        this.mvc.perform(attachPhoto(postId, "file", MediaType.IMAGE_JPEG_VALUE, createImage(2, 2, "JPEG")))
                .andExpect(status().isOk());
        MvcResult result = this.mvc.perform(getServiceRequest(postId))
                .andExpect(status().isOk())
                .andReturn();
        ServiceRequestModel model = mapper.readValue(result.getResponse().getContentAsString(), ServiceRequestModel.class);
        assertNotNull(model.pictures());
        assertEquals(2, model.pictures().size());
        // Pictures can be deleted by removing them from the list
        List<String> newOrder = List.of(model.pictures().get(0));
        ServiceRequestModel editedModel = new ServiceRequestModel(null, model.title(), model.description(),
                model.service(), model.status(), model.dollars(), model.address(), newOrder, model.creationDate());
        this.mvc.perform(editServiceRequest(postId, editedModel))
                .andExpect(status().isOk());

        result = this.mvc.perform(getServiceRequest(postId))
                .andExpect(status().isOk())
                .andReturn();
        model = mapper.readValue(result.getResponse().getContentAsString(), ServiceRequestModel.class);
        assertNotNull(model.pictures());
        assertEquals(1, model.pictures().size());
        assertEquals(1, imageInfoRepository.findAll().size());
        // Last picture can be removed
        newOrder = List.of();
        editedModel = new ServiceRequestModel(null, model.title(), model.description(),
                model.service(), model.status(), model.dollars(), model.address(), newOrder, model.creationDate());
        this.mvc.perform(editServiceRequest(postId, editedModel))
                .andExpect(status().isOk());

        result = this.mvc.perform(getServiceRequest(postId))
                .andExpect(status().isOk())
                .andReturn();
        model = mapper.readValue(result.getResponse().getContentAsString(), ServiceRequestModel.class);
        assertNotNull(model.pictures());
        assertTrue(model.pictures().isEmpty());
        assertTrue(imageInfoRepository.findAll().isEmpty());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    @Transactional
    void deletePostWithAttachedPictures() throws Exception {
        // Can attach PNG pictures
        this.mvc.perform(attachPhoto(postId, "file", MediaType.IMAGE_PNG_VALUE, createImage(2, 2, "PNG")))
                .andExpect(status().isOk());
        // Can attach JPEG pictures
        this.mvc.perform(attachPhoto(postId, "file", MediaType.IMAGE_JPEG_VALUE, createImage(2, 2, "JPEG")))
                .andExpect(status().isOk());
        // Deleting a post also deletes the pictures associated with it
        this.mvc.perform(deleteServiceRequest(postId))
                .andExpect(status().isOk());
        assertTrue(serviceRequestRepository.findAll().isEmpty());
        assertTrue(imageInfoRepository.findAll().isEmpty());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    @Transactional
    void attachPhotoRequestValidation() throws Exception {
        // Must include a file
        this.mvc.perform(attachPhoto(postId, "file", MediaType.IMAGE_PNG_VALUE, null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("empty_file"));
        assertTrue(imageInfoRepository.findAll().isEmpty());
        this.mvc.perform(attachPhoto(postId, "file", MediaType.IMAGE_PNG_VALUE, new ByteArrayInputStream(new byte[0])))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("empty_file"));
        assertTrue(imageInfoRepository.findAll().isEmpty());
        this.mvc.perform(attachPhoto(postId, "random_name", MediaType.IMAGE_PNG_VALUE, createImage(2, 2, "PNG")))
                .andExpect(status().isBadRequest());
        assertTrue(imageInfoRepository.findAll().isEmpty());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    @Transactional
    void attachInvalidPhoto() throws Exception {
        // Invalid images are rejected
        this.mvc.perform(attachPhoto(postId, "file", MediaType.IMAGE_PNG_VALUE, new ByteArrayInputStream(new byte[]{0x41, 0x6d})))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("upload_failure"));
        assertTrue(imageInfoRepository.findAll().isEmpty());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    @Transactional
    void attachToNonExistentPost() throws Exception {
        // Can't attach photos to nonexistent service requests
        this.mvc.perform(attachPhoto(Integer.MAX_VALUE, "file", MediaType.IMAGE_PNG_VALUE, createImage(2, 2, "PNG")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("non_existent_post"));
        assertTrue(imageInfoRepository.findAll().isEmpty());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    @Transactional
    void attachOtherPostImage() throws Exception {
        // Can't attach other photos without going through the {id}/attach endpoint
        Account account = accountRepository.findByEmail(TEST_EMAIL);
        ImageInfo imageInfo = imageStorageService.storeImage(createImage(2, 2, "PNG"), 2, 2, account);
        ServiceRequestModel editedModel = new ServiceRequestModel(null, VALID_SERVICE_REQUEST.title(), VALID_SERVICE_REQUEST.description(),
                VALID_SERVICE_REQUEST.service(), VALID_SERVICE_REQUEST.status(), VALID_SERVICE_REQUEST.dollars(), VALID_SERVICE_REQUEST.address(), List.of(imageInfo.getUuid().toString()), null);
        this.mvc.perform(editServiceRequest(postId, editedModel))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("unknown_photo"));
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, authorities = {"CUSTOMER", "VERIFIED"})
    @Transactional
    void editDuplicatePhotos() throws Exception {
        // Can't edit a service request to have duplicate photos
        this.mvc.perform(attachPhoto(postId, "file", MediaType.IMAGE_PNG_VALUE, createImage(2, 2, "PNG")))
                .andExpect(status().isOk());
        ServiceRequest serviceRequest = serviceRequestRepository.findByIdAndCustomerAccountEmail(postId, TEST_EMAIL);
        String attachedPhotoUUID = serviceRequest.getPictures().get(0).getUuid().toString();
        ServiceRequestModel editedModel = new ServiceRequestModel(null, VALID_SERVICE_REQUEST.title(), VALID_SERVICE_REQUEST.description(),
                VALID_SERVICE_REQUEST.service(), VALID_SERVICE_REQUEST.status(), VALID_SERVICE_REQUEST.dollars(), VALID_SERVICE_REQUEST.address(), List.of(attachedPhotoUUID, attachedPhotoUUID), null);
        this.mvc.perform(editServiceRequest(postId, editedModel))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("duplicate_photos"));
    }
    
    
}

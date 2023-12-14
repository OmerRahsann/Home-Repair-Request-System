package homerep.springy.customer;

import homerep.springy.config.ServiceRequestPictureConfig;
import homerep.springy.controller.customer.CustomerServiceRequestController;
import homerep.springy.entity.Account;
import homerep.springy.entity.ImageInfo;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.model.ServiceRequestModel;
import homerep.springy.repository.ImageInfoRepository;
import homerep.springy.service.ImageStorageService;
import homerep.springy.type.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests that involve service requests with attached images
 */
public class ImageServiceRequestTests extends AbstractServiceRequestTests {
    @Autowired
    private ImageInfoRepository imageInfoRepository;

    @Autowired
    private ImageStorageService imageStorageService;

    @Autowired
    private ServiceRequestPictureConfig pictureConfig;

    private int postId;
    @Autowired
    private CustomerServiceRequestController customerServiceRequestController;

    @BeforeEach
    void setupPost() throws Exception {
        postId = customerServiceRequestController.createPost(VALID_SERVICE_REQUEST, new User(customer.getAccount()));
        ServiceRequest serviceRequest = serviceRequestRepository.findById(postId).orElse(null);
        assertNotNull(serviceRequest);
    }

    @Test
    @Transactional
    @WithUserDetails(value = TEST_EMAIL, setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void attachPictures() throws Exception {
        // Can attach PNG pictures
        MvcResult result = this.mvc.perform(attachPhoto(postId, "file", MediaType.IMAGE_PNG_VALUE, createImage(2, 2, "PNG")))
                .andExpect(status().isOk())
                .andReturn();
        // and response contains the key to the image
        String picture1Key = mapper.readValue(result.getResponse().getContentAsString(), String.class);
        assertFalse(picture1Key.isBlank());
        // Can attach JPEG pictures
        result = this.mvc.perform(attachPhoto(postId, "file", MediaType.IMAGE_JPEG_VALUE, createImage(2, 2, "JPEG")))
                .andExpect(status().isOk())
                .andReturn();
        // and response contains the key to the image
        String picture2Key = mapper.readValue(result.getResponse().getContentAsString(), String.class);
        assertFalse(picture2Key.isBlank());
        assertNotEquals(picture1Key, picture2Key); // Keys should be different for each picture
        // Pictures are stored with the service request in the repository
        ServiceRequest serviceRequest = serviceRequestRepository.findByIdAndCustomerAccountEmail(postId, TEST_EMAIL);
        assertNotNull(serviceRequest);
        assertEquals(2, serviceRequest.getPictures().size());
        assertEquals(List.of(picture1Key, picture2Key), serviceRequest.getImagesUUIDs()); // Pictures are attached in the order sent
        assertEquals(2, imageInfoRepository.findAll().size());

        // Pictures are included with the service request when requested from the API
        result = this.mvc.perform(getServiceRequest(postId))
                .andExpect(status().isOk())
                .andReturn();
        ServiceRequestModel model = mapper.readValue(result.getResponse().getContentAsString(), ServiceRequestModel.class);
        assertNotNull(model.pictures());
        assertEquals(List.of(picture1Key, picture2Key), model.pictures()); // Pictures are attached in the order sent
        // Pictures are saved to the repository
        for (String picture : model.pictures()) {
            assertTrue(imageInfoRepository.findById(UUID.fromString(picture)).isPresent());
        }
    }

    @Test
    @Transactional
    @WithUserDetails(value = TEST_EMAIL, setupBefore = TestExecutionEvent.TEST_EXECUTION)
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
        ServiceRequestModel editedModel = model.withPictures(newOrder);
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
    @Transactional
    @WithUserDetails(value = TEST_EMAIL, setupBefore = TestExecutionEvent.TEST_EXECUTION)
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
        ServiceRequestModel editedModel = model.withPictures(newOrder);
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
        editedModel = model.withPictures(newOrder);
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
    @Transactional
    @WithUserDetails(value = TEST_EMAIL, setupBefore = TestExecutionEvent.TEST_EXECUTION)
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
    @Transactional
    @WithUserDetails(value = TEST_EMAIL, setupBefore = TestExecutionEvent.TEST_EXECUTION)
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
    @Transactional
    @WithUserDetails(value = TEST_EMAIL, setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void attachInvalidPhoto() throws Exception {
        // Invalid images are rejected
        this.mvc.perform(attachPhoto(postId, "file", MediaType.IMAGE_PNG_VALUE, new ByteArrayInputStream(new byte[]{0x41, 0x6d})))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("upload_failure"));
        assertTrue(imageInfoRepository.findAll().isEmpty());
    }

    @Test
    @Transactional
    @WithUserDetails(value = TEST_EMAIL, setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void attachToNonExistentPost() throws Exception {
        // Can't attach photos to nonexistent service requests
        this.mvc.perform(attachPhoto(Integer.MAX_VALUE, "file", MediaType.IMAGE_PNG_VALUE, createImage(2, 2, "PNG")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("non_existent_post"));
        assertTrue(imageInfoRepository.findAll().isEmpty());
    }

    @Test
    @Transactional
    @WithUserDetails(value = TEST_EMAIL, setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void attachOtherPostImage() throws Exception {
        // Can't attach other photos without going through the {id}/attach endpoint
        Account account = accountRepository.findByEmail(TEST_EMAIL);
        ImageInfo imageInfo = imageStorageService.storeImage(createImage(2, 2, "PNG"), 2, 2, account);
        ServiceRequestModel editedModel = VALID_SERVICE_REQUEST.withPictures(List.of(imageInfo.getUuid().toString()));
        this.mvc.perform(editServiceRequest(postId, editedModel))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("unknown_photo"));
    }

    @Test
    @Transactional
    @WithUserDetails(value = TEST_EMAIL, setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void editDuplicatePhotos() throws Exception {
        // Can't edit a service request to have duplicate photos
        this.mvc.perform(attachPhoto(postId, "file", MediaType.IMAGE_PNG_VALUE, createImage(2, 2, "PNG")))
                .andExpect(status().isOk());
        ServiceRequest serviceRequest = serviceRequestRepository.findByIdAndCustomerAccountEmail(postId, TEST_EMAIL);
        String attachedPhotoUUID = serviceRequest.getPictures().get(0).getUuid().toString();
        ServiceRequestModel editedModel = VALID_SERVICE_REQUEST.withPictures(List.of(attachedPhotoUUID, attachedPhotoUUID));
        this.mvc.perform(editServiceRequest(postId, editedModel))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("duplicate_photos"));
    }
    
    @Test
    @WithUserDetails(value = TEST_EMAIL, setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void resizeAttachedPicture() throws Exception {
        // Decrease max pixels for testing
        pictureConfig.setMaxSizePixels(8);
        // Pictures above the max size are resized to fit while keeping the aspect ratio
        int size = pictureConfig.getMaxSizePixels() * 2;
        float ratio = 2.0f;
        InputStream imageIS = createImage((int) (ratio * size), size, "PNG");
        MvcResult result = this.mvc.perform(attachPhoto(postId, "file", MediaType.IMAGE_PNG_VALUE, imageIS))
                .andExpect(status().isOk())
                .andReturn();
        UUID uuid = mapper.readValue(result.getResponse().getContentAsString(), UUID.class);

        result = this.mvc.perform(get("/image/{uuid}", uuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE))
                .andReturn();

        byte[] responseContent = result.getResponse().getContentAsByteArray();
        assertNotEquals(0, responseContent.length);

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(responseContent));
        assertNotNull(image);
        // Max size of image matches max size in config
        assertEquals(pictureConfig.getMaxSizePixels(), Math.max(image.getWidth(), image.getHeight()));
        // The aspect ratio of the image is kept
        float newRatio = (float) image.getWidth() / image.getHeight();
        assertEquals(ratio, newRatio, 0.01f);
    }

    @Test
    @WithUserDetails(value = TEST_EMAIL, setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void maxAttachedPictures() throws Exception {
        // Attaching pictures up to the max configured limit is fine
        for (int i = 0; i < pictureConfig.getMaxNumPictures(); i++) {
            InputStream imageIS = createImage(2, 2, "PNG");
            this.mvc.perform(attachPhoto(postId, "file", MediaType.IMAGE_PNG_VALUE, imageIS))
                    .andExpect(status().isOk());
        }
        // Attaching any more results in an error
        InputStream imageIS = createImage(2, 2, "PNG");
        this.mvc.perform(attachPhoto(postId, "file", MediaType.IMAGE_PNG_VALUE, imageIS))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("timestamp").isNumber())
                .andExpect(jsonPath("type").value("max_pictures"));
        // and is not saved into the ImageInfoRepository
        assertEquals(pictureConfig.getMaxNumPictures(), imageInfoRepository.findAll().size());
    }
}

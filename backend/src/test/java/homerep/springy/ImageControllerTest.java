package homerep.springy;

import homerep.springy.authorities.AccountType;
import homerep.springy.config.TestDatabaseConfig;
import homerep.springy.config.TestStorageConfig;
import homerep.springy.entity.Account;
import homerep.springy.entity.ImageInfo;
import homerep.springy.exception.ImageStoreException;
import homerep.springy.repository.AccountRepository;
import homerep.springy.service.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestDatabaseConfig
@Import(TestStorageConfig.class)
public class ImageControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ImageStorageService imageStorageService;

    @Autowired
    private ResourceLoader resourceLoader;

    private static final String TEST_EMAIL = "example@example.com";

    private static final String TEST_PNG_LOCATION = "classpath:image-storage-test/logo.png";

    private UUID storedImageUUID;

    @BeforeEach
    void storeImage() throws IOException, ImageStoreException {
        Account uploader = new Account();
        uploader.setEmail(TEST_EMAIL);
        uploader.setPassword(null);
        uploader.setVerified(true);
        uploader.setType(AccountType.CUSTOMER);

        uploader = accountRepository.save(uploader);
        Resource testImage = resourceLoader.getResource(TEST_PNG_LOCATION);
        // Store an image
        ImageInfo imageInfo = imageStorageService.storeImage(testImage.getInputStream(), 320, 320, uploader);
        storedImageUUID = imageInfo.getUuid();
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void nonExistentImage() throws Exception {
        this.mvc.perform(get("/image/{uuid}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void invalidUUID() throws Exception {
        this.mvc.perform(get("/image/1232132135124"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL)
    void validImage() throws Exception {
        MvcResult result = this.mvc.perform(get("/image/{uuid}", storedImageUUID))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"))
                .andReturn();

        byte[] responseContent = result.getResponse().getContentAsByteArray();
        assertNotEquals(0, responseContent.length);

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(responseContent));
        assertNotNull(image);
        assertEquals(320, image.getWidth());
        assertEquals(320, image.getHeight());
    }

    @Test
    @WithAnonymousUser
    void authRequired() throws Exception {
        this.mvc.perform(get("/image/{uuid}", storedImageUUID))
                .andExpect(status().isForbidden());
    }
}

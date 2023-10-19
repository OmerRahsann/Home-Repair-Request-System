package homerep.springy;

import homerep.springy.authorities.AccountType;
import homerep.springy.config.TestDatabaseConfig;
import homerep.springy.config.TestMailConfig;
import homerep.springy.config.TestStorageConfig;
import homerep.springy.entity.Account;
import homerep.springy.entity.ImageInfo;
import homerep.springy.repository.AccountRepository;
import homerep.springy.repository.ImageInfoRepository;
import homerep.springy.service.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({TestMailConfig.class, TestStorageConfig.class})
@TestDatabaseConfig
public class ImageStorageServiceTests {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ImageStorageService imageStorageService;

    @Autowired
    private ImageInfoRepository imageInfoRepository;

    @Autowired
    private ResourceLoader resourceLoader;

    private static final String TEST_EMAIL = "example@example.com";

    private static final String TEST_PNG_LOCATION = "classpath:image-storage-test/logo.png";

    @BeforeEach
    void setupAccount() {
        Account account = new Account();
        account.setEmail(TEST_EMAIL);
        account.setPassword(null);
        account.setVerified(true);
        account.setType(AccountType.CUSTOMER);

        accountRepository.save(account);
    }

    @Test
    void storeImage() throws IOException {
        Instant start = Instant.now();

        Account uploader = accountRepository.findByEmail(TEST_EMAIL);
        Resource testImage = resourceLoader.getResource(TEST_PNG_LOCATION);
        ImageInfo imageInfo = imageStorageService.storeImage(testImage.getInputStream(), 320, 320, uploader);
        // It's stored into the repository
        assertEquals(1, imageInfoRepository.findAll().size());
        // ImageInfo is filled out correctly
        assertNotNull(imageInfo.getUuid());
        assertEquals(imageInfo.getUploader(), uploader);
        assertTrue(imageInfo.getUploadDate().isAfter(start));

        String uuidStr = imageInfo.getUuid().toString();
        String firstByte = uuidStr.substring(0, 2);
        String secondByte = uuidStr.substring(2, 4);
        String fileName = uuidStr.substring(4) + ".jpg";

        Path storedImagePath = TestStorageConfig.TEST_STORAGE_ROOT.resolve(
                Path.of(firstByte, secondByte, fileName)
        );
        Path storedTmpImagePath = TestStorageConfig.TEST_STORAGE_ROOT.resolve(
                Path.of(firstByte, secondByte, fileName + ".tmp")
        );
        Path storedImageDirPath = storedImagePath.getParent();
        // Image is stored to the right location
        assertTrue(Files.exists(storedImagePath));
        assertTrue(Files.isRegularFile(storedImagePath));
        // Temp file is deleted
        assertFalse(Files.exists(storedTmpImagePath));
        assertEquals(1, Files.list(storedImageDirPath).count());

        // Saved image is readable
        BufferedImage image = ImageIO.read(storedImagePath.toFile());
        // maxWidth & maxHeight is respected
        assertEquals(320, image.getWidth());
        assertEquals(320, image.getHeight());

        // Trying to delete a random UUID does nothing
        imageStorageService.deleteImage(UUID.randomUUID());
        // repository is not afffect
        assertEquals(1, imageInfoRepository.findAll().size());
        // image is still there
        assertTrue(Files.exists(storedImagePath));
        assertTrue(Files.isRegularFile(storedImagePath));

        // Deleting with the correct UUID works
        imageStorageService.deleteImage(imageInfo.getUuid());
        // removed from the repository
        assertTrue(imageInfoRepository.findAll().isEmpty());
        // file is deleted
        assertFalse(Files.exists(storedImagePath));
    }

}

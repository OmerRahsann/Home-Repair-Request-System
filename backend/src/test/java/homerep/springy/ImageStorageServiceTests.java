package homerep.springy;

import homerep.springy.authorities.AccountType;
import homerep.springy.component.DummyDataComponent;
import homerep.springy.config.TestDatabaseConfig;
import homerep.springy.config.TestStorageConfig;
import homerep.springy.entity.Account;
import homerep.springy.entity.ImageInfo;
import homerep.springy.exception.ImageStoreException;
import homerep.springy.repository.ImageInfoRepository;
import homerep.springy.service.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestStorageConfig.class)
@TestDatabaseConfig
public class ImageStorageServiceTests {
    @Autowired
    private DummyDataComponent dummyDataComponent;

    @Autowired
    private ImageStorageService imageStorageService;

    @Autowired
    private ImageInfoRepository imageInfoRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ResourceLoader resourceLoader;

    private Account uploader;

    private static final String TEST_EMAIL = "example@example.com";

    private static final String TEST_PNG_LOCATION = "classpath:image-storage-test/logo.png";

    @BeforeEach
    void setupAccount() {
        uploader = dummyDataComponent.createAccount(TEST_EMAIL, AccountType.CUSTOMER);
    }

    @Test
    void storeImage() throws IOException, ImageStoreException {
        Instant start = Instant.now();

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
        try (Stream<Path> pathStream = Files.list(storedImageDirPath)) {
            assertEquals(1, pathStream.count());
        }

        // Saved image is readable
        BufferedImage image = ImageIO.read(storedImagePath.toFile());
        // maxWidth & maxHeight is respected
        assertEquals(320, image.getWidth());
        assertEquals(320, image.getHeight());

        // Trying to delete a random UUID does nothing
        imageStorageService.deleteImage(UUID.randomUUID());
        // repository is not affected
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

    @Test
    void testStoreRollback() throws IOException {
        InputStream testImageStream = resourceLoader.getResource(TEST_PNG_LOCATION).getInputStream();
        // Try to store an image but roll it back
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        try {
            txTemplate.execute(status -> {
                storeRollback(testImageStream, uploader);
                return null;
            });
        } catch (RuntimeException ex) {
            assertEquals("On purpose exception", ex.getMessage());
        }
        // Image is not stored in the repository
        assertTrue(imageInfoRepository.findAll().isEmpty());
        // Image is not stored on the filesystem
        try (Stream<Path> pathStream = Files.walk(TestStorageConfig.TEST_STORAGE_ROOT)) {
            List<Path> paths = pathStream
                    .filter(Files::isRegularFile)
                    .toList();
            assertEquals(List.of(), paths);
        }
    }

    @Test
    void testDeleteRollback() throws IOException, ImageStoreException {
        Resource testImage = resourceLoader.getResource(TEST_PNG_LOCATION);
        // Store an image
        ImageInfo imageInfo = imageStorageService.storeImage(testImage.getInputStream(), 320, 320, uploader);

        // Try to delete the image but roll it back
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        try {
            txTemplate.execute(status -> {
                deleteRollback(imageInfo.getUuid());
                return null;
            });
        } catch (RuntimeException ex) {
            assertEquals("On purpose exception", ex.getMessage());
        }

        // Image is still in repository
        assertEquals(1, imageInfoRepository.findAll().size());
        // Image is still in the file system
        try (Stream<Path> pathStream = Files.walk(TestStorageConfig.TEST_STORAGE_ROOT)) {
            List<Path> paths = pathStream
                    .filter(Files::isRegularFile)
                    .toList();
            assertEquals(1, paths.size());
        }
    }

    @Test
    void testInvalidImage() throws IOException {
        // Try to store an obviously invalid image
        InputStream inputStream = new ByteArrayInputStream(new byte[]{0x41, 0x6d, 0x62, 0x65, 0x72});
        assertThrows(ImageStoreException.class, () -> imageStorageService.storeImage(inputStream, 320, 320, uploader));
        // Image is not stored in the repository
        assertTrue(imageInfoRepository.findAll().isEmpty());
        // Image is not stored in the file system
        try (Stream<Path> pathStream = Files.walk(TestStorageConfig.TEST_STORAGE_ROOT)) {
            List<Path> paths = pathStream
                    .filter(Files::isRegularFile)
                    .toList();
            assertEquals(List.of(), paths);
        }
    }

    public void storeRollback(InputStream inputStream, Account uploader) {
        assertDoesNotThrow(() -> imageStorageService.storeImage(inputStream, 320, 320, uploader));
        throw new RuntimeException("On purpose exception");
    }

    public void deleteRollback(UUID uuid) {
        imageStorageService.deleteImage(uuid);
        throw new RuntimeException("On purpose exception");
    }

}

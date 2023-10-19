package homerep.springy.service.impl;

import homerep.springy.config.ImageStorageConfiguration;
import homerep.springy.entity.Account;
import homerep.springy.entity.ImageInfo;
import homerep.springy.exception.ImageStoreException;
import homerep.springy.repository.ImageInfoRepository;
import homerep.springy.service.ImageStorageService;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.process.Pipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class ImageStorageServiceImpl implements ImageStorageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageStorageServiceImpl.class);

    @Autowired
    private ImageInfoRepository imageInfoRepository;

    @Autowired
    private ImageStorageConfiguration.ImageStorageConfig config;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public ImageInfo storeImage(InputStream inputStream, int maxWidth, int maxHeight, Account uploader) throws ImageStoreException {
        UUID uuid = UUID.randomUUID();
        Path finalFilePath = constructPath(uuid, "jpg");
        Path tempFilePath = constructPath(uuid, "jpg.tmp");
        try {
            Files.createDirectories(tempFilePath.getParent());
            Files.createFile(tempFilePath);
            try (OutputStream os = Files.newOutputStream(tempFilePath, StandardOpenOption.CREATE)) {
                processImage(inputStream, os, maxWidth, maxHeight);
                os.close();
                applicationEventPublisher.publishEvent(new UploadImageEvent(tempFilePath, finalFilePath));
                ImageInfo imageInfo = new ImageInfo(uuid, uploader, Instant.now());
                return imageInfoRepository.save(imageInfo);
            }
        } catch (Exception e) {
            // Make sure files are deleted no matter what happens
            try {
                Files.deleteIfExists(tempFilePath);
            } catch (Exception ignored) {
            }
            try {
                Files.deleteIfExists(finalFilePath);
            } catch (Exception ignored) {
            }
            throw new ImageStoreException(e);
        }
    }

    @Override
    @Transactional
    public void deleteImage(UUID uuid) {
        Optional<ImageInfo> imageInfo = imageInfoRepository.findById(uuid);
        if (imageInfo.isEmpty()) {
            return;
        }
        Path filePath = constructPath(uuid, "jpg");
        applicationEventPublisher.publishEvent(new DeleteImageEvent(filePath));
        imageInfoRepository.delete(imageInfo.get());
    }

    private Path constructPath(UUID uuid, String extension) {
        String uuidStr = uuid.toString();
        String firstByte = uuidStr.substring(0, 2);
        String secondByte = uuidStr.substring(2, 4);
        String fileName = uuidStr.substring(4) + "." + extension;

        return Path.of(config.getStorageRoot(), firstByte, secondByte, fileName);
    }

    private void processImage(InputStream inputStream, OutputStream outputStream, int maxWidth, int maxHeight) throws IOException, InterruptedException, IM4JavaException {
        IMOperation op = new IMOperation();
        // From standard input
        op.addImage("-");
        // resize the image to fit within maxWidth x maxHeight
        op.resize(maxWidth, maxHeight, '>');
        // orient the image for portrait/landscape phone pictures
        op.autoOrient();
        // strip all unnecessary metadata
        op.strip();
        // convert to JPEG with a given quality
        op.quality(config.getQuality());
        // output to standard output
        op.addImage("JPEG:-");

        ConvertCmd convert = new ConvertCmd();
        convert.setInputProvider(new Pipe(inputStream, null));
        convert.setOutputConsumer(new Pipe(null, outputStream));
        convert.run(op); // TODO should this be async??
    }

    protected record UploadImageEvent(Path tmpFilePath, Path finalFilePath) {
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    protected void handleUploadRollback(UploadImageEvent event) {
        try {
            Files.deleteIfExists(event.tmpFilePath());
        } catch (IOException e) {
            LOGGER.warn("Failed to delete temporary image file for rolled back image upload. Path: " +
                    event.tmpFilePath(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    protected void handleUploadCommit(UploadImageEvent event) {
        try {
            Files.move(event.tmpFilePath(), event.finalFilePath(), StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            LOGGER.warn("Failed to move temporary image file to final image file. Src: " +
                    event.tmpFilePath() + " Dst: " + event.finalFilePath(), e);
        }
    }

    protected record DeleteImageEvent(Path filePath) {}

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    protected void handleDeleteCommit(DeleteImageEvent event) {
        try {
            Files.deleteIfExists(event.filePath());
        } catch (IOException e) {
            LOGGER.warn("Failed to delete image file. Path: " + event.filePath(), e);
        }
    }
}

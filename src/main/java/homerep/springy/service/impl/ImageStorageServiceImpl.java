package homerep.springy.service.impl;

import homerep.springy.config.ImageStorageConfig;
import homerep.springy.entity.Account;
import homerep.springy.entity.ImageInfo;
import homerep.springy.exception.ImageStoreException;
import homerep.springy.repository.ImageInfoRepository;
import homerep.springy.service.ImageStorageService;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.ImageCommand;
import org.im4java.process.ArrayListOutputConsumer;
import org.im4java.process.Pipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class ImageStorageServiceImpl implements ImageStorageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageStorageServiceImpl.class);

    private final ImageInfoRepository imageInfoRepository;

    private final ImageStorageConfig config;

    private final ApplicationEventPublisher applicationEventPublisher;

    private boolean hasMagickCmd = false;
    private boolean hasConvertCmd = false;

    public ImageStorageServiceImpl(ImageInfoRepository imageInfoRepository,
                                   ImageStorageConfig config,
                                   ApplicationEventPublisher applicationEventPublisher) {
        this.imageInfoRepository = imageInfoRepository;
        this.config = config;
        this.applicationEventPublisher = applicationEventPublisher;

        checkIMVersion();
        checkIMPolicy();
    }

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

    @Override
    public Path findImage(UUID uuid) {
        Path path = constructPath(uuid, "jpg");
        if (Files.exists(path)) {
            return path;
        }
        return null;
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

        ImageCommand convert = createConvertCmd();
        convert.setInputProvider(new Pipe(inputStream, null));
        convert.setOutputConsumer(new Pipe(null, outputStream));
        convert.run(op);
    }

    private void checkIMVersion() {
        IMOperation op = new IMOperation();
        op.version();
        try {
            // 'magick' is available in ImageMagick version 7 and should be preferred
            // over 'convert' as 'convert' conflicts with a built-in Windows command
            ArrayListOutputConsumer consumer = new ArrayListOutputConsumer();
            ImageCommand cmd = new ImageCommand("magick");
            cmd.setOutputConsumer(consumer);
            cmd.run(op);
            hasMagickCmd = true;
            LOGGER.info("Found 'magick' command");
        } catch (IOException | InterruptedException | IM4JavaException e) {
            LOGGER.debug("Testing 'magick' command failed", e);
        }
        try {
            // 'convert' is available in ImageMagick version 6 and 7. We fall back to
            // this command just in case an older version of ImageMagick was installed.
            ArrayListOutputConsumer consumer = new ArrayListOutputConsumer();
            ConvertCmd cmd = new ConvertCmd();
            cmd.setOutputConsumer(consumer);
            cmd.run(op);
            hasConvertCmd = true;
            LOGGER.info("Found 'convert' command");
        } catch (IOException | InterruptedException | IM4JavaException e) {
            LOGGER.debug("Testing 'convert' command failed", e);
        }
        if (hasMagickCmd) {
            LOGGER.info("Utilizing 'magick' command for image conversion.");
        } else if (hasConvertCmd) {
            LOGGER.info("Utilizing 'convert' command for image conversion.");
        } else {
            LOGGER.error("No suitable ImageMagick command was found! Make sure ImageMagick is installed and the 'magick' or 'convert' commands work.");
        }
    }

    private ImageCommand createConvertCmd() {
        if (hasMagickCmd) {
            return new ImageCommand("magick");
        }
        return new ConvertCmd();
    }

    private void checkIMPolicy() {
        try {
            // List the policies
            IMOperation op = new IMOperation();
            op.list("policy");

            ArrayListOutputConsumer consumer = new ArrayListOutputConsumer();
            ImageCommand cmd = createConvertCmd();
            cmd.setOutputConsumer(consumer);
            cmd.run(op);
            // Scan through the list for
            // Policy: Module
            //   rights: None
            //   pattern: *
            boolean hasDenyAll = false;

            Iterator<String> it = consumer.getOutput().iterator();
            Supplier<String> lineSupplier = () -> {
                if (it.hasNext()) {
                    return it.next().stripLeading();
                } else {
                    return "";
                }
            };
            while (it.hasNext()) {
                String line = lineSupplier.get();
                if (!line.startsWith("Policy: Module")) {
                    continue;
                }
                String rights = "";
                String pattern = "";
                for (int i = 0; i < 2; i++) {
                    line = lineSupplier.get();
                    String value = line.substring(line.indexOf(": ") + 2).stripTrailing();
                    if (line.startsWith("rights: ")) {
                        rights = value;
                    } else if (line.startsWith("pattern: ")) {
                        pattern = value;
                    }
                }
                if (rights.equals("None") && pattern.equals("*")) {
                    hasDenyAll = true;
                    break;
                }
            }

            if (!hasDenyAll) {
                LOGGER.error("Insecure ImageMagick policy detected! Please install other/policy.xml to the correct location.");
            } else {
                LOGGER.info("Secure ImageMagick policy detected.");
            }
        } catch (Exception ex) {
            LOGGER.warn("Failed to check ImageMagick policy.", ex);
        }
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

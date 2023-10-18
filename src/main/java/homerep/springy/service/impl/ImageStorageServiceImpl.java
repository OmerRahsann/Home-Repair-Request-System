package homerep.springy.service.impl;

import homerep.springy.entity.Account;
import homerep.springy.entity.ImageInfo;
import homerep.springy.repository.ImageInfoRepository;
import homerep.springy.service.ImageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.UUID;

/**
 * Using the database to store the images.
 * Probably a very bad idea.
 * TODO store images on the file system
 */
@Service
public class ImageStorageServiceImpl implements ImageStorageService {
    @Autowired
    private ImageInfoRepository imageInfoRepository;

    @Override
    public ImageInfo storeImage(InputStream inputStream, Account uploader) {
        try {
            // TODO validate & sanitize images
            // TODO resize to a sensible size
            // TODO encode as JPGs
            ImageInfo imageInfo = new ImageInfo(uploader, Instant.now(), inputStream.readAllBytes());
            return imageInfoRepository.save(imageInfo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteImage(UUID uuid) {
        imageInfoRepository.deleteById(uuid);
    }

    @Override
    public void deleteAll() {
        imageInfoRepository.deleteAll();
        imageInfoRepository.flush();
    }
}

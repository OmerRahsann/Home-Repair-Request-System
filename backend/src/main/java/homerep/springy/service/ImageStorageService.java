package homerep.springy.service;

import homerep.springy.entity.Account;
import homerep.springy.entity.ImageInfo;
import homerep.springy.exception.ImageStoreException;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;

public interface ImageStorageService {
    ImageInfo storeImage(InputStream inputStream, int maxWidth, int maxHeight, Account uploader) throws ImageStoreException;

    void deleteImage(UUID uuid);

    Path findImage(UUID uuid);
}

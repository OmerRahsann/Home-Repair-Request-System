package homerep.springy.service;

import homerep.springy.entity.Account;
import homerep.springy.entity.ImageInfo;

import java.io.InputStream;
import java.util.UUID;

public interface ImageStorageService {
    ImageInfo storeImage(InputStream inputStream, int maxWidth, int maxHeight, Account uploader);

    void deleteImage(UUID uuid);
}

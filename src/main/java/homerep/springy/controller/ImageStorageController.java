package homerep.springy.controller;

import homerep.springy.entity.ImageInfo;
import homerep.springy.repository.ImageInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

/**
 * TEMPORARY endpoint that provides images from the database
 * TODO replace with something more sensible
 */
@RestController
public class ImageStorageController {
    @Autowired
    private ImageInfoRepository imageInfoRepository;

    @GetMapping(value = "/api/image/{uuid}", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getImage(@PathVariable("uuid") UUID uuid) {
        Optional<ImageInfo> imageInfo = imageInfoRepository.findById(uuid);
        if (imageInfo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found");
        }
        return imageInfo.get().getData();
    }
}

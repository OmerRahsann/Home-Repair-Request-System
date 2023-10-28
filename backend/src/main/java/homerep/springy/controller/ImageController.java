package homerep.springy.controller;

import homerep.springy.service.ImageStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.PathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import java.util.UUID;

@RestController
public class ImageController {

    @Autowired
    private ImageStorageService imageStorageService;

    @GetMapping(value = "/image/{uuid}", produces = MediaType.IMAGE_JPEG_VALUE)
    public PathResource getImage(@PathVariable("uuid") String uuidStr) {
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException ignored) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Path path = imageStorageService.findImage(uuid);
        if (path == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return new PathResource(path);
    }
}

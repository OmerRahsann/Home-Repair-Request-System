package homerep.springy.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
public class ImageInfo {
    @Id
    private UUID uuid;

    @ManyToOne
    private Account uploader;

    private Instant uploadDate;
    protected ImageInfo() {}

    public ImageInfo(UUID uuid, Account uploader, Instant uploadDate) {
        this.uuid = uuid;
        this.uploader = uploader;
        this.uploadDate = uploadDate;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Account getUploader() {
        return uploader;
    }

    public void setUploader(Account uploader) {
        this.uploader = uploader;
    }

    public Instant getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Instant uploadDate) {
        this.uploadDate = uploadDate;
    }
}

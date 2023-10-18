package homerep.springy.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
public class ImageInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @ManyToOne
    private Account uploader;

    private Instant uploadDate;

    @Column(length = 4 * 1024 * 1024) // Yes, let's store 4 MiB of data/record in the database TODO not do this
    private byte[] data;

    protected ImageInfo() {}

    public ImageInfo(Account uploader, Instant uploadDate, byte[] data) {
        this.uploader = uploader;
        this.uploadDate = uploadDate;
        this.data = data;
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

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}

package homerep.springy.entity;

import homerep.springy.model.notification.NotificationType;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class Notification {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Account account;

    private String title;

    private String message;

    private Instant timestamp;

    @Column(name = "notification_read")
    private boolean read;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    public Notification() {
    }

    public Notification(Account account, String title, String message, NotificationType type) {
        this.account = account;
        this.title = title;
        this.message = message;
        this.timestamp = Instant.now();
        this.read = false;
        this.type = type;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }
}

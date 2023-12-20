package homerep.springy.model.notification;

import homerep.springy.entity.Notification;

import java.time.Instant;

public record NotificationModel(
        String title,
        String message,
        Instant timestamp,
        boolean read,
        NotificationType type
) {
    public static NotificationModel fromEntity(Notification notification) {
        return new NotificationModel(
                notification.getTitle(),
                notification.getMessage(),
                notification.getTimestamp(),
                notification.isRead(),
                notification.getType()
        );
    }
}

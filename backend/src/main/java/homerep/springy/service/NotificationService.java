package homerep.springy.service;

import homerep.springy.entity.Account;
import homerep.springy.model.notification.NotificationModel;
import homerep.springy.model.notification.NotificationType;

import java.util.List;

public interface NotificationService {
    void sendNotification(Account account, String title, String message, NotificationType type);

    void clearNotifications(Account account);

    void markNotificationsAsRead(Account account);

    List<NotificationModel> getNotifications(Account account);
}

package homerep.springy.service.impl;

import homerep.springy.entity.Account;
import homerep.springy.entity.Notification;
import homerep.springy.model.notification.NotificationModel;
import homerep.springy.model.notification.NotificationType;
import homerep.springy.repository.NotificationRepository;
import homerep.springy.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public void sendNotification(Account account, String title, String message, NotificationType type) {
        Notification notification = new Notification(
                account,
                title,
                message,
                type
        );
        notificationRepository.save(notification);
    }

    @Override
    public void clearNotifications(Account account) {
        notificationRepository.deleteAllByAccount(account);
    }

    @Override
    public void markNotificationsAsRead(Account account) {
        notificationRepository.markAllByAccountAsRead(account);
    }

    @Override
    public List<NotificationModel> getNotifications(Account account) {
        List<Notification> notifications = notificationRepository.findAllByAccount(
                account,
                Sort.by(Sort.Direction.DESC, "timestamp")
        );
        return toModels(notifications);
    }

    private List<NotificationModel> toModels(List<Notification> notifications) {
        List<NotificationModel> models = new ArrayList<>(notifications.size());
        for (Notification notification : notifications) {
            models.add(NotificationModel.fromEntity(notification));
        }
        return models;
    }
}

package homerep.springy;

import homerep.springy.authorities.AccountType;
import homerep.springy.component.DummyDataComponent;
import homerep.springy.config.TestDatabaseConfig;
import homerep.springy.entity.Account;
import homerep.springy.model.notification.NotificationModel;
import homerep.springy.model.notification.NotificationType;
import homerep.springy.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestDatabaseConfig
public class NotificationServiceTest {
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private DummyDataComponent dummyDataComponent;

    private Account account1;
    private Account account2;

    private static final String EMAIL_1 = "test@localhost";
    private static final String EMAIL_2 = "test2@localhost";

    @BeforeEach
    void setup() {
        account1 = dummyDataComponent.createAccount(EMAIL_1, AccountType.CUSTOMER);
        account2 = dummyDataComponent.createAccount(EMAIL_2, AccountType.SERVICE_PROVIDER);
    }

    private void sendNotifications(int num, Account account) {
        for (int i = 0; i < num; i++) {
            String title = dummyDataComponent.generateDummySentence();
            String message = dummyDataComponent.generateDummySentence();
            NotificationType type = dummyDataComponent.randomFrom(List.of(NotificationType.values()));
            notificationService.sendNotification(account, title, message, type);
        }
    }

    @Test
    void sendGetNotification() {
        Instant start = Instant.now();
        String title = dummyDataComponent.generateDummySentence();
        String message = dummyDataComponent.generateDummySentence();
        NotificationType type = dummyDataComponent.randomFrom(List.of(NotificationType.values()));
        // Send a notification to account1
        notificationService.sendNotification(account1, title, message, type);
        // It is in the list of notifications only for account 1
        List<NotificationModel> models = notificationService.getNotifications(account2);
        assertTrue(models.isEmpty());
        models = notificationService.getNotifications(account1);
        assertEquals(1, models.size());
        // Model matches data passed into sendNotification
        NotificationModel model = models.get(0);
        assertEquals(title, model.title());
        assertEquals(message, model.message());
        assertEquals(type, model.type());
        // Timestamp is sensible
        assertTrue(model.timestamp().isAfter(start));
        assertTrue(model.timestamp().isBefore(Instant.now()));
        // Notification has not been read yet
        assertFalse(model.read());
    }

    @Test
    void markAsReadNotifications() {
        // Send 10 notifications to account1
        sendNotifications(10, account1);
        // Mark them as read
        notificationService.markNotificationsAsRead(account1);
        // All 10 notifications are marked as read
        List<NotificationModel> models = notificationService.getNotifications(account1);
        assertEquals(10, models.size());
        assertEquals(10, models.stream()
                .filter(NotificationModel::read)
                .count());
    }

    @Test
    @Transactional
    void clearNotifications() {
        // Send 10 notifications to account1
        sendNotifications(10, account1);
        // All 10 notifications are listed
        List<NotificationModel> models = notificationService.getNotifications(account1);
        assertEquals(10, models.size());
        // Clear the notifications
        notificationService.clearNotifications(account1);
        // There's no more notifications
        models = notificationService.getNotifications(account1);
        assertTrue(models.isEmpty());
    }

    @Test
    void notificationsAreSorted() {
        // Send 10 notifications to account1
        sendNotifications(10, account1);
        // All 10 notifications are listed
        List<NotificationModel> models = notificationService.getNotifications(account1);
        assertEquals(10, models.size());
        // Notifications are sorted by timestamp descending
        for (int i = 0; i < models.size() - 1; i++) {
            NotificationModel left = models.get(0);
            NotificationModel right = models.get(1);
            assertTrue(left.timestamp().isAfter(right.timestamp()));
        }
    }
}

package homerep.springy.controller;

import homerep.springy.entity.Account;
import homerep.springy.model.notification.NotificationModel;
import homerep.springy.repository.AccountRepository;
import homerep.springy.service.NotificationService;
import homerep.springy.type.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/clear")
    @Transactional
    public void clearNotifications(@AuthenticationPrincipal User user) {
        Account account = accountRepository.getReferenceById(user.getAccountId());
        notificationService.clearNotifications(account);
    }

    @PostMapping("/mark_read")
    public void markNotificationsAsRead(@AuthenticationPrincipal User user) {
        Account account = accountRepository.getReferenceById(user.getAccountId());
        notificationService.markNotificationsAsRead(account);
    }

    @GetMapping
    public List<NotificationModel> getNotifications(@AuthenticationPrincipal User user) {
        Account account = accountRepository.getReferenceById(user.getAccountId());
        return notificationService.getNotifications(account);
    }
}

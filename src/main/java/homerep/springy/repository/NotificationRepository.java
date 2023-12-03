package homerep.springy.repository;

import homerep.springy.entity.Account;
import homerep.springy.entity.Notification;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    void deleteAllByAccount(Account account);

    List<Notification> findAllByAccountAndRead(Account account, boolean read);

    List<Notification> findAllByAccount(Account account, Sort sort);
}

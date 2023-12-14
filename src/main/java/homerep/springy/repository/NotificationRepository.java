package homerep.springy.repository;

import homerep.springy.entity.Account;
import homerep.springy.entity.Notification;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    void deleteAllByAccount(Account account);

    @Modifying
    @Transactional
    @Query(
            value = "update Notification n SET n.read = true where n.account = ?1"
    )
    void markAllByAccountAsRead(Account account);

    List<Notification> findAllByAccount(Account account, Sort sort);
}

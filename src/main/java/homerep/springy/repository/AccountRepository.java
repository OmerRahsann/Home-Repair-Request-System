package homerep.springy.repository;

import homerep.springy.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByEmail(String email);

    Account findByVerificationToken(String token);
}

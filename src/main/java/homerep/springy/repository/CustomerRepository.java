package homerep.springy.repository;

import homerep.springy.entity.Account;
import homerep.springy.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Customer findByAccount(Account account);

    Customer findByAccountEmail(String email);
}

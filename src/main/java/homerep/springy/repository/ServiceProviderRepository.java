package homerep.springy.repository;

import homerep.springy.entity.Account;
import homerep.springy.entity.ServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Integer> {
    ServiceProvider findByAccountEmail(String email);
}

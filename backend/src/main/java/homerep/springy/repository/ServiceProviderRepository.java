package homerep.springy.repository;

import homerep.springy.entity.ServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Integer> {
    ServiceProvider findByAccountId(Long accountId);
}

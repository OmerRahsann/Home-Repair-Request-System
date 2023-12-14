package homerep.springy.repository;

import homerep.springy.entity.ServiceType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceTypeRepository extends JpaRepository<ServiceType, Integer> {
    default List<String> findAllServices() {
        List<String> services = findAll(Sort.by("serviceType").ascending())
                .stream()
                .map(ServiceType::getServiceType)
                .toList();
        if (services.isEmpty()) {
            // We're in a dev environment and services is empty.
            // Return some random services.
            return List.of("Plumbing", "Roofing", "Yardwork");
        }
        return services;
    }
}

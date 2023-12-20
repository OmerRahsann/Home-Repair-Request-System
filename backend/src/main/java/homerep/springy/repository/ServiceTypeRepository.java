package homerep.springy.repository;

import homerep.springy.entity.ServiceType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ServiceTypeRepository extends JpaRepository<ServiceType, Integer> {

    @Query("select s.serviceType from ServiceType s")
    List<String> _findAllServices(Sort sort);

    default List<String> findAllServices() {
        List<String> services = _findAllServices(Sort.by("serviceType").ascending());
        if (services.isEmpty()) {
            // We're in a dev environment and services is empty.
            // Return some random services.
            return List.of("Plumbing", "Roofing", "Yardwork");
        }
        return services;
    }
}

package homerep.springy.repository;

import homerep.springy.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Integer> {
    List<ServiceRequest> findAllByCustomerAccountId(long accountId);

    ServiceRequest findByIdAndCustomerAccountId(int id, long accountId);

    List<ServiceRequest> findAllByLatitudeBetweenAndLongitudeBetween(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude);
}

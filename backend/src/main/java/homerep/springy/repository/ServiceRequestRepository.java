package homerep.springy.repository;

import homerep.springy.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Integer> {
    List<ServiceRequest> findAllByCustomerAccountEmail(String email);

    ServiceRequest findByIdAndCustomerAccountEmail(int id, String email);
}

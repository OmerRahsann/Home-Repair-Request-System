package homerep.springy.repository;

import homerep.springy.entity.EmailRequest;
import homerep.springy.entity.ServiceProvider;
import homerep.springy.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailRequestRepository extends JpaRepository<EmailRequest, Long> {
    EmailRequest findByServiceProviderAndServiceRequest(ServiceProvider serviceProvider, ServiceRequest serviceRequest);

    EmailRequest findByIdAndServiceRequest(long id, ServiceRequest serviceRequest);
}

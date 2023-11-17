package homerep.springy.repository;

import homerep.springy.entity.Customer;
import homerep.springy.entity.EmailRequest;
import homerep.springy.entity.ServiceProvider;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.model.emailrequest.EmailRequestStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailRequestRepository extends JpaRepository<EmailRequest, Long> {
    List<EmailRequest> findAllByServiceRequestCustomerAndStatus(Customer customer, EmailRequestStatus status, Sort sort);

    List<EmailRequest> findAllByServiceProviderAndStatus(ServiceProvider serviceProvider, EmailRequestStatus status, Sort sort);

    EmailRequest findByServiceProviderAndServiceRequest(ServiceProvider serviceProvider, ServiceRequest serviceRequest);

    EmailRequest findByIdAndServiceRequestCustomerAccountEmail(long id, String email);
}

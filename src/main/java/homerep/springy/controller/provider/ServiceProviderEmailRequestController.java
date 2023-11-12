package homerep.springy.controller.provider;

import homerep.springy.entity.EmailRequest;
import homerep.springy.entity.ServiceProvider;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.exception.ApiException;
import homerep.springy.exception.NonExistentPostException;
import homerep.springy.model.emailrequest.EmailRequestModel;
import homerep.springy.model.emailrequest.EmailRequestStatus;
import homerep.springy.repository.EmailRequestRepository;
import homerep.springy.repository.ServiceProviderRepository;
import homerep.springy.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/provider/service_requests/")
public class ServiceProviderEmailRequestController {

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private EmailRequestRepository emailRequestRepository;

    @GetMapping("/{id}/email")
    public EmailRequestModel getEmail(@PathVariable("id") int id, @AuthenticationPrincipal User user) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(id).orElse(null);
        if (serviceRequest == null) {
            throw new NonExistentPostException();
        }
        ServiceProvider serviceProvider = serviceProviderRepository.findByAccountEmail(user.getUsername());
        EmailRequest emailRequest = emailRequestRepository.findByServiceProviderAndServiceRequest(serviceProvider, serviceRequest);
        if (emailRequest == null) {
            return new EmailRequestModel(EmailRequestStatus.NOT_REQUESTED);
        }
        if (emailRequest.getStatus() != EmailRequestStatus.ACCEPTED) {
            return new EmailRequestModel(emailRequest.getStatus());
        }
        String email = serviceRequest.getCustomer().getAccount().getEmail();
        return new EmailRequestModel(email, EmailRequestStatus.ACCEPTED);
    }

    @PostMapping("/{id}/email/request")
    public void requestEmail(@PathVariable("id") int id, @AuthenticationPrincipal User user) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(id).orElse(null);
        if (serviceRequest == null) {
            throw new NonExistentPostException();
        }
        ServiceProvider serviceProvider = serviceProviderRepository.findByAccountEmail(user.getUsername());
        EmailRequest emailRequest = emailRequestRepository.findByServiceProviderAndServiceRequest(serviceProvider, serviceRequest);
        if (emailRequest != null) {
            throw new ApiException("already_requested", "Email request was already sent.");
        }
        emailRequest = new EmailRequest(serviceProvider, serviceRequest);
        emailRequestRepository.save(emailRequest);
        // TODO notification of some sort?
    }
}

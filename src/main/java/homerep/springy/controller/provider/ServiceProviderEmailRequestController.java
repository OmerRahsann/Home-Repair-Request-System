package homerep.springy.controller.provider;

import homerep.springy.entity.ServiceProvider;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.exception.ApiException;
import homerep.springy.exception.NonExistentPostException;
import homerep.springy.model.emailrequest.EmailRequestModel;
import homerep.springy.repository.ServiceProviderRepository;
import homerep.springy.repository.ServiceRequestRepository;
import homerep.springy.service.EmailRequestService;
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
    private EmailRequestService emailRequestService;

    @GetMapping("/{id}/email")
    public EmailRequestModel getEmail(@PathVariable("id") int id, @AuthenticationPrincipal User user) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(id).orElse(null);
        if (serviceRequest == null) {
            throw new NonExistentPostException();
        }
        ServiceProvider serviceProvider = serviceProviderRepository.findByAccountEmail(user.getUsername());
        return emailRequestService.getEmail(serviceRequest, serviceProvider);
    }

    @PostMapping("/{id}/email/request")
    public void requestEmail(@PathVariable("id") int id, @AuthenticationPrincipal User user) {
        ServiceRequest serviceRequest = serviceRequestRepository.findById(id).orElse(null);
        if (serviceRequest == null) {
            throw new NonExistentPostException();
        }
        ServiceProvider serviceProvider = serviceProviderRepository.findByAccountEmail(user.getUsername());
        if (!emailRequestService.sendEmailRequest(serviceRequest, serviceProvider)) {
            throw new ApiException("already_requested", "Email request was already sent.");
        }
    }
}

package homerep.springy.controller.customer;

import homerep.springy.entity.EmailRequest;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.exception.ApiException;
import homerep.springy.exception.NonExistentPostException;
import homerep.springy.model.emailrequest.EmailRequestInfoModel;
import homerep.springy.repository.EmailRequestRepository;
import homerep.springy.repository.ServiceRequestRepository;
import homerep.springy.service.EmailRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/service_request")
public class CustomerEmailRequestController {

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private EmailRequestRepository emailRequestRepository;

    @Autowired
    private EmailRequestService emailRequestService;

    @GetMapping("/{id}/email_requests")
    public List<EmailRequestInfoModel> getEmailRequests(@PathVariable("id") int id, @AuthenticationPrincipal User user) {
        ServiceRequest serviceRequest = serviceRequestRepository.findByIdAndCustomerAccountEmail(id, user.getUsername());
        if (serviceRequest == null) {
            throw new NonExistentPostException();
        }
        return emailRequestService.listEmailRequests(serviceRequest);
    }

    @PostMapping("/{service_request_id}/email_requests/{email_request_id}/accepted")
    public void updateEmailRequestStatus(
            @PathVariable("service_request_id") int serviceRequestId,
            @PathVariable("email_request_id") int emailRequestId,
            @RequestBody boolean accepted,
            @AuthenticationPrincipal User user) {
        ServiceRequest serviceRequest = serviceRequestRepository.findByIdAndCustomerAccountEmail(serviceRequestId, user.getUsername());
        if (serviceRequest == null) {
            throw new NonExistentPostException();
        }
        EmailRequest emailRequest = emailRequestRepository.findByIdAndServiceRequest(emailRequestId, serviceRequest);
        if (emailRequest == null) {
            throw new ApiException("non_existent_email_request", "Email request not found.");
        }
        emailRequestService.updateEmailRequestStatus(emailRequest, accepted);
    }
}

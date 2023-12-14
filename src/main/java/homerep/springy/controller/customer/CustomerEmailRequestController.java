package homerep.springy.controller.customer;

import homerep.springy.entity.Customer;
import homerep.springy.entity.EmailRequest;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.exception.ApiException;
import homerep.springy.exception.NonExistentPostException;
import homerep.springy.model.emailrequest.EmailRequestInfoModel;
import homerep.springy.repository.CustomerRepository;
import homerep.springy.repository.EmailRequestRepository;
import homerep.springy.repository.ServiceRequestRepository;
import homerep.springy.service.EmailRequestService;
import homerep.springy.type.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class CustomerEmailRequestController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private EmailRequestRepository emailRequestRepository;

    @Autowired
    private EmailRequestService emailRequestService;

    @GetMapping("/email_requests")
    public List<EmailRequestInfoModel> getPendingEmailRequests(@AuthenticationPrincipal User user) {
        Customer customer = customerRepository.findByAccountEmail(user.getEmail());
        return emailRequestService.getPendingEmailRequests(customer);
    }

    @GetMapping("/service_request/{id}/email_requests")
    public List<EmailRequestInfoModel> getEmailRequests(@PathVariable("id") int id, @AuthenticationPrincipal User user) {
        ServiceRequest serviceRequest = serviceRequestRepository.findByIdAndCustomerAccountEmail(id, user.getEmail());
        if (serviceRequest == null) {
            throw new NonExistentPostException();
        }
        return emailRequestService.getEmailRequests(serviceRequest);
    }

    @PostMapping("/email_requests/{email_request_id}/accept")
    public void acceptEmailRequest(@PathVariable("email_request_id") long emailRequestId, @AuthenticationPrincipal User user) {
        EmailRequest emailRequest = emailRequestRepository.findByIdAndServiceRequestCustomerAccountEmail(emailRequestId, user.getEmail());
        if (emailRequest == null) {
            throw new ApiException("non_existent_email_request", "Email request not found.");
        }
        emailRequestService.acceptEmailRequest(emailRequest);
    }

    @PostMapping("/email_requests/{email_request_id}/reject")
    public void rejectEmailRequest(@PathVariable("email_request_id") long emailRequestId, @AuthenticationPrincipal User user) {
        EmailRequest emailRequest = emailRequestRepository.findByIdAndServiceRequestCustomerAccountEmail(emailRequestId, user.getEmail());
        if (emailRequest == null) {
            throw new ApiException("non_existent_email_request", "Email request not found.");
        }
        emailRequestService.rejectEmailRequest(emailRequest);
    }
}

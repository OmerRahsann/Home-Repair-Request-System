package homerep.springy.controller.customer;

import homerep.springy.entity.Customer;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.model.ServiceRequestModel;
import homerep.springy.repository.CustomerRepository;
import homerep.springy.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/customer/service_request")
public class ServiceRequestController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @PostMapping("/create")
    public void createPost(@RequestBody @Validated ServiceRequestModel serviceRequestModel, @AuthenticationPrincipal User user) {
        Customer customer = customerRepository.findByAccountEmail(user.getUsername());
        ServiceRequest serviceRequest = new ServiceRequest(customer);
        serviceRequest.setTitle(serviceRequestModel.title());
        serviceRequest.setDescription(serviceRequestModel.description());
        serviceRequest.setDollars(serviceRequestModel.dollars());
        serviceRequest.setDate(new Date());
        serviceRequestRepository.save(serviceRequest);
    }

    @GetMapping
    public List<ServiceRequestModel> getPosts(@AuthenticationPrincipal User user) {
        List<ServiceRequest> serviceRequests = serviceRequestRepository.findAllByCustomerAccountEmail(user.getUsername());
        List<ServiceRequestModel> models = new ArrayList<>(serviceRequests.size());
        for (ServiceRequest serviceRequest : serviceRequests) {
            models.add(new ServiceRequestModel(serviceRequest.getTitle(), serviceRequest.getDescription(), serviceRequest.getDollars()));
        }
        return models;
    }
}

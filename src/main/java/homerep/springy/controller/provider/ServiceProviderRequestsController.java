package homerep.springy.controller.provider;

import homerep.springy.entity.ServiceRequest;
import homerep.springy.repository.ServiceRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/provider/service_request")
public class ServiceProviderRequestsController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceProviderRequestsController.class);

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @GetMapping
    public List<ServiceRequest> getAllServiceRequests() {
        return serviceRequestRepository.findAll();
    }

    @GetMapping("/sorted/distance")
    public List<ServiceRequest> getServiceRequestsSortedByDistance() {
        // TODO: Implement a mechanism to sort ServiceRequests by distance
        return serviceRequestRepository.findAll(); // Placeholder
    }

    @GetMapping("/sorted/price")
    public List<ServiceRequest> getServiceRequestsSortedByPriceAndDistance() {
        // TODO: Filter the ServiceRequests by a distance mechanism no further than 10 miles
        return serviceRequestRepository.findAll().stream()
                .sorted((s1, s2) -> Integer.compare(s2.getDollars(), s1.getDollars())) // Sort by price in descending order
                .collect(Collectors.toList());
    }

    @GetMapping("/sorted/date")
    public List<ServiceRequest> getServiceRequestsSortedByDateAndDistance() {
        // TODO: Filter the ServiceRequests by a distance mechanism no further than 10 miles
        return serviceRequestRepository.findAll().stream()
                .sorted((s1, s2) -> s2.getCreationDate().compareTo(s1.getCreationDate())) // Sort by date, newest first
                .collect(Collectors.toList());
    }
}


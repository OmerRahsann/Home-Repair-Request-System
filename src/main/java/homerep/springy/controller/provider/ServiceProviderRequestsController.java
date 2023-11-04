package homerep.springy.controller.provider;

import homerep.springy.entity.ServiceRequest;
import homerep.springy.model.ServiceRequestModel;
import homerep.springy.repository.ServiceProviderRepository;
import homerep.springy.repository.ServiceRequestRepository;
import homerep.springy.repository.ServiceTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/provider/service_requests")
public class ServiceProviderRequestsController {

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private ServiceTypeRepository serviceTypeRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @GetMapping("/all")
    public List<ServiceRequestModel> getAllServiceRequests() {
        List<ServiceRequest> serviceRequests = serviceRequestRepository.findAll();
        List<ServiceRequestModel> models = new ArrayList<>(serviceRequests.size());
        for (ServiceRequest serviceRequest : serviceRequests) {
            models.add(ServiceRequestModel.fromEntity(serviceRequest));
        }
        return models;
    }

    @GetMapping("/nearby")
    public List<ServiceRequestModel> getServiceRequestsNearby(
            @RequestParam double latitudeS,
            @RequestParam double longitudeW,
            @RequestParam double latitudeN,
            @RequestParam double longitudeE,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) Integer lowerDollarRange,
            @RequestParam(required = false) Integer higherDollarRange,
            @AuthenticationPrincipal User user) {
        List<ServiceRequest> serviceRequests = serviceRequestRepository.findAllByLatitudeBetweenAndLongitudeBetween(latitudeS, latitudeN, longitudeW, longitudeE);

        return serviceRequests.stream()
                .filter(request -> {
                    boolean matchesServiceType = serviceType == null || request.getService().equalsIgnoreCase(serviceType);
                    double desiredPrice = request.getDollars();
                    boolean isWithinDollarsRange = lowerDollarRange == null || higherDollarRange==null || (lowerDollarRange <= desiredPrice && desiredPrice <= higherDollarRange);
                    boolean isPending = request.getStatus().equals(ServiceRequest.Status.PENDING);

                    return matchesServiceType && isWithinDollarsRange && isPending;
                })
                .map(ServiceRequestModel::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/services")
    public List<String> getServices() {
        return serviceTypeRepository.findAllServices();
    }

}



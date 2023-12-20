package homerep.springy.controller.provider;

import homerep.springy.entity.ServiceRequest;
import homerep.springy.model.ServiceRequestModel;
import homerep.springy.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
            @RequestParam(required = false) Integer higherDollarRange) {
        List<ServiceRequest> serviceRequests;
        double deltaLatitude = Math.abs(latitudeN - latitudeS);
        if (deltaLatitude > 2.0) {
            // Too zoomed out, don't return anything
            return List.of();
        }

        double deltaLongitude = Math.abs(longitudeE - longitudeW);
        if (deltaLongitude > 180) {
            // Longitudes wrap around at -180/180, so we have to split the query at that point
            deltaLongitude = Math.abs(-180 - longitudeE) + Math.abs(180 - longitudeW);
            if (deltaLongitude > 1.0) {
                return List.of(); // Too zoomed out, don't return anything
            }

            serviceRequests = serviceRequestRepository.findAllByLatitudeBetweenAndLongitudeBetween(latitudeS, latitudeN, -180, longitudeE);
            serviceRequests.addAll(serviceRequestRepository.findAllByLatitudeBetweenAndLongitudeBetween(latitudeS, latitudeN, longitudeW, 180));
        } else {
            if (deltaLongitude > 1.0) {
                return List.of(); // Too zoomed out, don't return anything
            }
            serviceRequests = serviceRequestRepository.findAllByLatitudeBetweenAndLongitudeBetween(latitudeS, latitudeN, longitudeW, longitudeE);
        }

        return serviceRequests.stream()
                .filter(request -> {
                    boolean matchesServiceType = serviceType == null || request.getService().equalsIgnoreCase(serviceType);
                    // TODO not do math, make customer specify range?
                    double start = Math.ceil(request.getDollars() / 10.0) * 10.0;
                    double lowerBoundPrice = 0.8 * start;
                    double upperBoundPrice = Math.ceil(1.3 * start);

                    boolean isWithinDollarsRange = lowerDollarRange == null || higherDollarRange == null || (lowerDollarRange <= upperBoundPrice && lowerBoundPrice <= higherDollarRange);
                    boolean isPending = request.getStatus().equals(ServiceRequest.Status.PENDING);

                    return matchesServiceType && isWithinDollarsRange && isPending;
                })
                .map(ServiceRequestModel::fromEntity)
                .collect(Collectors.toList());
    }

}



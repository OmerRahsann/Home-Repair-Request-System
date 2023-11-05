package homerep.springy.controller.provider;
import homerep.springy.entity.ServiceProvider;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.entity.ServiceType;
import homerep.springy.repository.ServiceProviderRepository;
import homerep.springy.repository.ServiceRequestRepository;
import homerep.springy.model.ServiceRequestModelMapper;
import homerep.springy.model.ServiceRequestModel;

import homerep.springy.repository.ServiceTypeRepository;
import org.apache.tomcat.util.json.JSONParser;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.google.maps.GeocodingApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.GeocodingResult;
import java.io.IOException;
import java.security.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/provider/service_requests")
public class ServiceProviderRequestsController {

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private ServiceRequestModelMapper serviceRequestModelMapper;

    @Autowired
    private ServiceTypeRepository serviceTypeRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    private static final String GOOGLE_MAPS_API_KEY = "AIzaSyB-Hir-BFLaHrDngWHU5dXi3wA4VfIshs4";

    @GetMapping("/all")
    public List<ServiceRequestModel> getAllServiceRequests() {
        List<ServiceRequest> serviceRequests = serviceRequestRepository.findAll();
        List<ServiceRequestModel> models = new ArrayList<>(serviceRequests.size());
        for (ServiceRequest serviceRequest : serviceRequests) {
            models.add(toModel(serviceRequest));
        }
        return models;
    }

    @GetMapping("/nearby")
    public List<ServiceRequestModel> getServiceRequestsNearby(
            @RequestParam double latitudeSW,
            @RequestParam double longitudeSW,
            @RequestParam double latitudeNE,
            @RequestParam double longitudeNE,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) Integer lowerDollarRange,
            @RequestParam(required = false) Integer higherDollarRange,
            @AuthenticationPrincipal User user) {
        List<ServiceRequest> serviceRequests = serviceRequestRepository.findAll();

        return serviceRequests.stream()
                .filter(request -> {
                    try {
                        boolean isWithinBounds = isWithinBounds(request.getLatitude(), request.getLongitude(), latitudeSW, longitudeSW, latitudeNE, longitudeNE);
                        boolean matchesServiceType = (serviceType == null) || request.getService().equalsIgnoreCase(serviceType);
                        double desiredPrice = request.getDollars();
                        boolean isWithinDollarsRange = ((lowerDollarRange == null) && (higherDollarRange==null)) || (lowerDollarRange <= desiredPrice) && (desiredPrice <= higherDollarRange);
                        boolean isPending = request.getStatus().equals(ServiceRequest.Status.PENDING);

                        return isWithinBounds && matchesServiceType && isWithinDollarsRange && isPending;

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @GetMapping("/services")
    public List<String> getServices() {
        return serviceTypeRepository.findAllServices();
    }


    private boolean isWithinBounds(double lat, double lng, double latitudeSW, double longitudeSW, double latitudeNE, double longitudeNE) throws IOException {
            return lat >= latitudeSW && lat <= latitudeNE && lng >= longitudeSW && lng <= longitudeNE;
    }

    private ServiceRequestModel toModel(ServiceRequest serviceRequest) {
        return new ServiceRequestModel(
                serviceRequest.getId(),
                serviceRequest.getTitle(),
                serviceRequest.getDescription(),
                serviceRequest.getService(),
                serviceRequest.getStatus(),
                serviceRequest.getDollars(),
                serviceRequest.getAddress(),
                serviceRequest.getImagesUUIDs(),
                serviceRequest.getCreationDate(),
                serviceRequest.getLatitude(),
                serviceRequest.getLongitude()
        );
    }
}



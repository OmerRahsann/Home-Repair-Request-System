package homerep.springy.controller.provider;

import com.google.gson.JsonObject;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.repository.ServiceRequestRepository;
import homerep.springy.model.ServiceRequestModelMapper;
import homerep.springy.model.ServiceRequestModel;

import org.apache.tomcat.util.json.JSONParser;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.google.maps.GeocodingApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.GeocodingResult;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.net.URLEncoder;
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
            @RequestParam double longitudeNE) {
        List<ServiceRequest> serviceRequests = serviceRequestRepository.findAll();

        return serviceRequests.stream()
                .filter(request -> {
                    try {
                        return isWithinBounds(request.getLatitude(), request.getLongitude(), latitudeSW, longitudeSW, latitudeNE, longitudeNE);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(this::toModel) // Convert ServiceRequest to ServiceRequestModel
                .collect(Collectors.toList());
    }


    private boolean isWithinBounds(double lat, double lng, double latitudeSW, double longitudeSW, double latitudeNE, double longitudeNE) throws IOException {
            return lat >= latitudeSW && lat <= latitudeNE && lng >= longitudeSW && lng <= longitudeNE;
    }



//    @GetMapping("/nearby")
//    public List<ServiceRequest> getServiceRequestsNearby(
//        @RequestParam double latitudeSW,
//        @RequestParam double longitudeSW,
//        @RequestParam double latitudeNE,
//        @RequestParam double longitudeNE) {
//
//        double midpointLat = (latitudeSW + latitudeNE) / 2;
//        double midpointLon = (longitudeSW + longitudeNE) / 2;
//
//        double radius = haversineDistance(latitudeSW, longitudeSW, latitudeNE, longitudeNE) / 2;
//
//        List<ServiceRequest> allRequests = serviceRequestRepository.findAll();
//
//        return allRequests.stream()
//                .filter(request -> {
//                    double[] coords = convertAddressToCoords(request.getAddress()); // Assuming address field in ServiceRequest
//                    double distance = haversineDistance(midpointLat, midpointLon, coords[0], coords[1]);
//                    return distance <= radius;
//                })
//                .collect(Collectors.toList());
//    }

    public double[] convertAddressToCoords(String address) {
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(GOOGLE_MAPS_API_KEY)
                .build();

        try {
            GeocodingResult[] results = GeocodingApi.geocode(context, address).await();
            if (results.length > 0) {
                double lat = results[0].geometry.location.lat;
                double lng = results[0].geometry.location.lng;
                return new double[]{lat, lng};
            } else {
                throw new RuntimeException("No results found for address: " + address);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error converting address to coordinates: " + e.getMessage(), e);
        }
    }

    public double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        int R = 3959; // Radius of the Earth in miles
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in miles
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



package homerep.springy.controller.customer;

import homerep.springy.config.ServiceRequestPictureConfig;
import homerep.springy.entity.*;
import homerep.springy.exception.ApiException;
import homerep.springy.exception.GeocodingException;
import homerep.springy.exception.ImageStoreException;
import homerep.springy.exception.NonExistentPostException;
import homerep.springy.model.ServiceRequestModel;
import homerep.springy.repository.CustomerRepository;
import homerep.springy.repository.ServiceRequestRepository;
import homerep.springy.repository.ServiceRequestTemplateRepository;
import homerep.springy.service.GeocodingService;
import homerep.springy.service.ImageStorageService;
import homerep.springy.type.LatLong;
import homerep.springy.type.User;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer/service_request")
public class CustomerServiceRequestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerServiceRequestController.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private ServiceRequestTemplateRepository serviceRequestTemplateRepository;

    @Autowired
    private ImageStorageService imageStorage;

    @Autowired
    private ServiceRequestPictureConfig pictureConfig;

    @Autowired
    private GeocodingService geocodingService;

    @PostMapping("/create")
    public int createPost(@RequestBody @Validated ServiceRequestModel serviceRequestModel, @AuthenticationPrincipal User user) {
        Customer customer = customerRepository.findByAccountId(user.getAccountId());
        ServiceRequest serviceRequest = new ServiceRequest(customer);
        updatePost(serviceRequestModel, serviceRequest);
        serviceRequest.setCreationDate(new Date());
        serviceRequest.setStatus(ServiceRequest.Status.PENDING);
        serviceRequest = serviceRequestRepository.save(serviceRequest);
        return serviceRequest.getId();
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void deletePost(@PathVariable("id") int id, @AuthenticationPrincipal User user) {
        ServiceRequest serviceRequest = serviceRequestRepository.findByIdAndCustomerAccountId(id, user.getAccountId());
        if (serviceRequest == null) {
            throw new NonExistentPostException();
        }
        for (ImageInfo image : serviceRequest.getPictures()) {
            imageStorage.deleteImage(image.getUuid());
        }
        serviceRequestRepository.delete(serviceRequest);
    }

    @PostMapping("/{id}/edit")
    @Transactional
    public void editPost(@PathVariable("id") int id, @RequestBody @Validated ServiceRequestModel serviceRequestModel,
                         @AuthenticationPrincipal User user) {
        ServiceRequest serviceRequest = serviceRequestRepository.findByIdAndCustomerAccountId(id, user.getAccountId());
        if (serviceRequest == null) {
            throw new NonExistentPostException();
        }
        updatePost(serviceRequestModel, serviceRequest);
        serviceRequestRepository.save(serviceRequest);
    }

    @PostMapping("/{id}/attach")
    @Transactional
    public UUID attachPicture(@RequestParam("file") MultipartFile file, @PathVariable("id") int id, @AuthenticationPrincipal User user) {
        try {
            if (file.isEmpty() || file.getContentType() == null) {
                throw new ApiException("empty_file", "No image file was sent.");
            }
            ServiceRequest serviceRequest = serviceRequestRepository.findByIdAndCustomerAccountId(id, user.getAccountId());
            if (serviceRequest == null) {
                throw new NonExistentPostException();
            }
            if (serviceRequest.getPictures().size() >= pictureConfig.getMaxNumPictures()) {
                throw new ApiException("max_pictures", "Can't attach any more pictures. A max of " + pictureConfig.getMaxNumPictures() + " is allowed.");
            }
            Account account = serviceRequest.getCustomer().getAccount();
            ImageInfo imageInfo = imageStorage.storeImage(file.getInputStream(), pictureConfig.getMaxSizePixels(), pictureConfig.getMaxSizePixels(), account);
            serviceRequest.getPictures().add(imageInfo);
            serviceRequestRepository.save(serviceRequest);
            return imageInfo.getUuid();
        } catch (IOException | ImageStoreException e) {
            LOGGER.warn("Image attachment failed due to:", e);
            throw new ApiException("upload_failure", "Failed to upload file.");
        }
    }

    @GetMapping
    public List<ServiceRequestModel> getPosts(@AuthenticationPrincipal User user) {
        List<ServiceRequest> serviceRequests = serviceRequestRepository.findAllByCustomerAccountId(user.getAccountId());
        List<ServiceRequestModel> models = new ArrayList<>(serviceRequests.size());
        for (ServiceRequest serviceRequest : serviceRequests) {
            models.add(ServiceRequestModel.fromEntity(serviceRequest));
        }
        return models;
    }

    @GetMapping("/{id}")
    public ServiceRequestModel getPost(@PathVariable("id") int id, @AuthenticationPrincipal User user) {
        ServiceRequest serviceRequest = serviceRequestRepository.findByIdAndCustomerAccountId(id, user.getAccountId());
        if (serviceRequest == null) {
            throw new NonExistentPostException();
        }
        return ServiceRequestModel.fromEntity(serviceRequest);
    }

    @GetMapping("/templates")
    public List<ServiceRequestModel> getTemplates(@AuthenticationPrincipal User user) {
        Customer customer = customerRepository.findByAccountId(user.getAccountId());

        List<ServiceRequestTemplate> templates = serviceRequestTemplateRepository.findAllTemplates();
        List<ServiceRequestModel> models = new ArrayList<>(templates.size());
        for (ServiceRequestTemplate template : templates) {
            models.add(new ServiceRequestModel(
                    template.getTitle(),
                    null,
                    template.getService(),
                    template.getDollars(),
                    customer.getAddress()
            ));
        }
        return models;
    }

    private void updatePost(ServiceRequestModel serviceRequestModel, ServiceRequest serviceRequest) {
        serviceRequest.setTitle(serviceRequestModel.title());
        serviceRequest.setDescription(serviceRequestModel.description());
        serviceRequest.setService(serviceRequestModel.service());
        if (serviceRequestModel.status() != null) {
            serviceRequest.setStatus(serviceRequestModel.status());
        }
        serviceRequest.setDollars(serviceRequestModel.dollars());
        serviceRequest.setAddress(serviceRequestModel.address());

        try {
            LatLong location = geocodingService.geocode(serviceRequestModel.address());
            if (location == null) {
                throw new ApiException("geocoding_error", "Unable to geocode address.");
            }
            serviceRequest.setLatitude(location.latitude());
            serviceRequest.setLongitude(location.longitude());
            serviceRequest.setLocationRetrievalTime(Instant.now());
        } catch (GeocodingException e) {
            throw new ApiException("geocoding_error", "Unable to geocode address.", e);
        }

        if (serviceRequestModel.pictures() != null) {
            Map<String, ImageInfo> alreadyAttached = serviceRequest.getPictures().stream()
                    .collect(Collectors.toMap(imageInfo -> imageInfo.getUuid().toString(), Function.identity()));
            List<ImageInfo> newOrder = new ArrayList<>(serviceRequestModel.pictures().size());
            // Validate that all pictures were attached to this service request
            for (int i = 0; i < serviceRequestModel.pictures().size(); i++) {
                String photo = serviceRequestModel.pictures().get(i);
                ImageInfo imageInfo = alreadyAttached.get(photo);
                if (imageInfo == null) {
                    throw new ApiException("unknown_photo", "Unknown photo: " + photo);
                }
                newOrder.add(imageInfo);
            }
            if (serviceRequestModel.pictures().stream().distinct().count() != newOrder.size()) {
                throw new ApiException("duplicate_photos", "Can't have duplicate photos in a single post.");
            }
            // Mark unused photos for deletion
            for (String photo : alreadyAttached.keySet()) {
                if (!serviceRequestModel.pictures().contains(photo)) {
                    imageStorage.deleteImage(UUID.fromString(photo));
                }
            }
            // Apply the new order
            serviceRequest.setPictures(newOrder);
        }
    }

}

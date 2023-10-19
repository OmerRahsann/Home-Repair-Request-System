package homerep.springy.controller.customer;

import homerep.springy.entity.Account;
import homerep.springy.entity.Customer;
import homerep.springy.entity.ImageInfo;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.exception.ApiException;
import homerep.springy.exception.ImageStoreException;
import homerep.springy.model.ServiceRequestModel;
import homerep.springy.repository.CustomerRepository;
import homerep.springy.repository.ServiceRequestRepository;
import homerep.springy.service.ImageStorageService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer/service_request")
public class ServiceRequestController {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private ImageStorageService imageStorage;

    @PostMapping("/create")
    public int createPost(@RequestBody @Validated ServiceRequestModel serviceRequestModel, @AuthenticationPrincipal User user) {
        Customer customer = customerRepository.findByAccountEmail(user.getUsername());
        ServiceRequest serviceRequest = new ServiceRequest(customer);
        updatePost(serviceRequestModel, serviceRequest);
        serviceRequest.setDate(new Date());
        serviceRequest = serviceRequestRepository.save(serviceRequest);
        return serviceRequest.getId();
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void deletePost(@PathVariable("id") int id, @AuthenticationPrincipal User user) {
        ServiceRequest serviceRequest = serviceRequestRepository.findByIdAndCustomerAccountEmail(id, user.getUsername());
        if (serviceRequest == null) {
            throw new ApiException("non_existent_post", "Post not found.");
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
        ServiceRequest serviceRequest = serviceRequestRepository.findByIdAndCustomerAccountEmail(id, user.getUsername());
        if (serviceRequest == null) {
            throw new ApiException("non_existent_post", "Post not found.");
        }
        updatePost(serviceRequestModel, serviceRequest);
        serviceRequestRepository.save(serviceRequest);
    }

    @PostMapping("/{id}/attach")
    @Transactional
    public void attachPicture(@RequestParam("file") MultipartFile file, @PathVariable("id") int id, @AuthenticationPrincipal User user) {
        try {
            if (file.isEmpty() || file.getContentType() == null) {
                throw new ApiException("empty_file", "No image file was sent.");
            }
            ServiceRequest serviceRequest = serviceRequestRepository.findByIdAndCustomerAccountEmail(id, user.getUsername());
            if (serviceRequest == null) {
                throw new ApiException("non_existent_post", "Post not found.");
            }
            Account account = serviceRequest.getCustomer().getAccount();
            ImageInfo imageInfo = imageStorage.storeImage(file.getInputStream(), 1920, 1920, account); // TODO make maxWidth/Height configurable
            serviceRequest.getPictures().add(imageInfo);
            serviceRequestRepository.save(serviceRequest);
        } catch (IOException | ImageStoreException e) {
            throw new ApiException("upload_failure", "Failed to upload file.");
        }
    }

    @GetMapping
    public List<ServiceRequestModel> getPosts(@AuthenticationPrincipal User user) {
        List<ServiceRequest> serviceRequests = serviceRequestRepository.findAllByCustomerAccountEmail(user.getUsername());
        List<ServiceRequestModel> models = new ArrayList<>(serviceRequests.size());
        for (ServiceRequest serviceRequest : serviceRequests) {
            models.add(new ServiceRequestModel(
                    serviceRequest.getId(),
                    serviceRequest.getTitle(),
                    serviceRequest.getDescription(),
                    serviceRequest.getDollars(),
                    serviceRequest.getAddress(),
                    serviceRequest.getImagesUUIDs()
            ));
        }
        return models;
    }

    private void updatePost(ServiceRequestModel serviceRequestModel, ServiceRequest serviceRequest) {
        serviceRequest.setTitle(serviceRequestModel.title());
        serviceRequest.setDescription(serviceRequestModel.description());
        serviceRequest.setDollars(serviceRequestModel.dollars());
        serviceRequest.setAddress(serviceRequestModel.address());
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
            // Must occur after all validation is complete as images may be deleted from the file system
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

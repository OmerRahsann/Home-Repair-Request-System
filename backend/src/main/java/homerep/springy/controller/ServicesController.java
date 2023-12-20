package homerep.springy.controller;

import homerep.springy.repository.ServiceTypeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ServicesController {
    private final ServiceTypeRepository serviceTypeRepository;

    public ServicesController(ServiceTypeRepository serviceTypeRepository) {
        this.serviceTypeRepository = serviceTypeRepository;
    }

    @GetMapping("/api/services")
    public List<String> getServices() {
        return serviceTypeRepository.findAllServices();
    }

}

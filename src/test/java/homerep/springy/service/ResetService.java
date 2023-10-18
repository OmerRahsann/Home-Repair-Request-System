package homerep.springy.service;

import homerep.springy.repository.AccountRepository;
import homerep.springy.repository.CustomerRepository;
import homerep.springy.repository.ServiceProviderRepository;
import homerep.springy.repository.ServiceRequestRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResetService {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private ImageStorageService imageStorageService;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void resetAll() {
        serviceRequestRepository.deleteAll();
        serviceRequestRepository.flush(); // Resolves weird data integrity violation for transactional tests
        imageStorageService.deleteAll();

        customerRepository.deleteAll();
        customerRepository.flush();
        serviceProviderRepository.deleteAll();
        serviceProviderRepository.flush();

        accountRepository.deleteAll();
        accountRepository.flush();
    }
}

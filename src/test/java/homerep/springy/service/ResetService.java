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

    @Transactional
    public void resetAll() {
        serviceRequestRepository.deleteAll();

        customerRepository.deleteAll();
        serviceProviderRepository.deleteAll();

        accountRepository.deleteAll();
    }
}

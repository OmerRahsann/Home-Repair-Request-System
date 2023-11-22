package homerep.springy.service.impl;

import homerep.springy.entity.Customer;
import homerep.springy.entity.EmailRequest;
import homerep.springy.entity.ServiceProvider;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.model.accountinfo.CustomerInfoModel;
import homerep.springy.model.accountinfo.ServiceProviderInfoModel;
import homerep.springy.model.emailrequest.EmailRequestInfoModel;
import homerep.springy.model.emailrequest.EmailRequestModel;
import homerep.springy.model.emailrequest.EmailRequestStatus;
import homerep.springy.repository.EmailRequestRepository;
import homerep.springy.repository.ServiceRequestRepository;
import homerep.springy.service.EmailRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class EmailRequestServiceImpl implements EmailRequestService {

    @Autowired
    private EmailRequestRepository emailRequestRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Override
    public EmailRequestModel getEmail(ServiceRequest serviceRequest, ServiceProvider serviceProvider) {
        EmailRequest emailRequest = emailRequestRepository.findByServiceProviderAndServiceRequest(serviceProvider, serviceRequest);
        return toModel(serviceRequest, emailRequest);
    }

    @Override
    public boolean canAccessEmail(ServiceProvider serviceProvider, ServiceRequest serviceRequest) {
        EmailRequest emailRequest = emailRequestRepository.findByServiceProviderAndServiceRequest(serviceProvider, serviceRequest);
        if (emailRequest == null) {
            return false;
        }
        return emailRequest.getStatus() == EmailRequestStatus.ACCEPTED;
    }

    @Override
    public boolean sendEmailRequest(ServiceRequest serviceRequest, ServiceProvider serviceProvider) {
        EmailRequest emailRequest = emailRequestRepository.findByServiceProviderAndServiceRequest(serviceProvider, serviceRequest);
        if (emailRequest != null) {
            return false;
        }
        emailRequest = new EmailRequest(serviceProvider, serviceRequest);
        emailRequest = emailRequestRepository.save(emailRequest);
        serviceRequest.getEmailRequests().add(emailRequest);
        serviceRequest = serviceRequestRepository.save(serviceRequest);
        // TODO notification of some sort?
        return true;
    }

    @Override
    public List<EmailRequestInfoModel> getPendingEmailRequests(Customer customer) {
        List<EmailRequest> emailRequests = emailRequestRepository.findAllByServiceRequestCustomerAndStatus(
                customer,
                EmailRequestStatus.PENDING,
                Sort.by(Sort.Direction.DESC, "requestTimestamp")
        );
        return toInfoModels(emailRequests);
    }

    @Override
    public List<EmailRequestModel> getAcceptedEmailRequests(ServiceProvider serviceProvider) {
        List<EmailRequest> emailRequests = emailRequestRepository.findAllByServiceProviderAndStatus(
                serviceProvider,
                EmailRequestStatus.ACCEPTED,
                Sort.by(Sort.Direction.DESC, "updateTimestamp")
        );
        // TODO should expire after some time and after service request is complete
        return toModels(emailRequests);
    }

    @Override
    public List<EmailRequestInfoModel> getEmailRequests(ServiceRequest serviceRequest) {
        return toInfoModels(serviceRequest.getEmailRequests());
    }

    @Override
    public boolean acceptEmailRequest(EmailRequest emailRequest) {
        if (emailRequest.getStatus() != EmailRequestStatus.PENDING) {
            return false;
        }
        emailRequest.setStatus(EmailRequestStatus.ACCEPTED);
        emailRequest.setUpdateTimestamp(Instant.now());
        emailRequestRepository.save(emailRequest);
        // TODO notification of some sort?
        return true;
    }

    @Override
    public void rejectEmailRequest(EmailRequest emailRequest) {
        emailRequest.setStatus(EmailRequestStatus.REJECTED);
        emailRequest.setUpdateTimestamp(Instant.now());
        emailRequestRepository.save(emailRequest);
    }

    private EmailRequestModel toModel(ServiceRequest serviceRequest, EmailRequest emailRequest) {
        if (emailRequest == null) {
            return new EmailRequestModel(serviceRequest, EmailRequestStatus.NOT_REQUESTED);
        }
        if (emailRequest.getStatus() != EmailRequestStatus.ACCEPTED) {
            return new EmailRequestModel(serviceRequest, emailRequest.getStatus());
        }
        String email = serviceRequest.getCustomer().getAccount().getEmail();
        return new EmailRequestModel(
                serviceRequest,
                new CustomerInfoModel(
                        serviceRequest.getCustomer().getFirstName(),
                        serviceRequest.getCustomer().getMiddleName(),
                        serviceRequest.getCustomer().getLastName(),
                        null,
                        serviceRequest.getCustomer().getPhoneNumber()
                ),
                email,
                EmailRequestStatus.ACCEPTED,
                emailRequest.getUpdateTimestamp()
        );
    }

    private List<EmailRequestModel> toModels(List<EmailRequest> emailRequests) {
        List<EmailRequestModel> models = new ArrayList<>(emailRequests.size());
        for (EmailRequest emailRequest : emailRequests) {
            models.add(toModel(emailRequest.getServiceRequest(), emailRequest));
        }
        return models;
    }

    private List<EmailRequestInfoModel> toInfoModels(Collection<EmailRequest> emailRequests) {
        List<EmailRequestInfoModel> models = new ArrayList<>(emailRequests.size());
        for (EmailRequest emailRequest : emailRequests) {
            models.add(new EmailRequestInfoModel(
                    emailRequest.getId(),
                    emailRequest.getServiceRequest().getId(),
                    ServiceProviderInfoModel.fromEntity(emailRequest.getServiceProvider()),
                    emailRequest.getStatus(),
                    emailRequest.getRequestTimestamp()
            ));
        }
        return models;
    }
}

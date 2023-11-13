package homerep.springy.service.impl;

import homerep.springy.entity.EmailRequest;
import homerep.springy.entity.ServiceProvider;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.model.accountinfo.ServiceProviderInfoModel;
import homerep.springy.model.emailrequest.EmailRequestInfoModel;
import homerep.springy.model.emailrequest.EmailRequestModel;
import homerep.springy.model.emailrequest.EmailRequestStatus;
import homerep.springy.repository.EmailRequestRepository;
import homerep.springy.repository.ServiceRequestRepository;
import homerep.springy.service.EmailRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        if (emailRequest == null) {
            return new EmailRequestModel(EmailRequestStatus.NOT_REQUESTED);
        }
        if (emailRequest.getStatus() != EmailRequestStatus.ACCEPTED) {
            return new EmailRequestModel(emailRequest.getStatus());
        }
        String email = serviceRequest.getCustomer().getAccount().getEmail();
        return new EmailRequestModel(email, EmailRequestStatus.ACCEPTED);
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
    public List<EmailRequestInfoModel> listEmailRequests(ServiceRequest serviceRequest) {
        List<EmailRequestInfoModel> models = new ArrayList<>(serviceRequest.getEmailRequests().size());
        for (EmailRequest emailRequest : serviceRequest.getEmailRequests()) {
            models.add(new EmailRequestInfoModel(
                    emailRequest.getId(),
                    ServiceProviderInfoModel.fromEntity(emailRequest.getServiceProvider()),
                    emailRequest.getStatus()
            ));
        }
        return models;
    }

    @Override
    public void updateEmailRequestStatus(EmailRequest emailRequest, boolean accepted) {
        emailRequest.setStatus(accepted ? EmailRequestStatus.ACCEPTED : EmailRequestStatus.REJECTED);
        emailRequestRepository.save(emailRequest);
        // TODO notification of some sort?
    }
}

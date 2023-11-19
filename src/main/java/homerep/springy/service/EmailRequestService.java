package homerep.springy.service;

import homerep.springy.entity.Customer;
import homerep.springy.entity.EmailRequest;
import homerep.springy.entity.ServiceProvider;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.model.emailrequest.EmailRequestInfoModel;
import homerep.springy.model.emailrequest.EmailRequestModel;

import java.util.List;

public interface EmailRequestService {
    EmailRequestModel getEmail(ServiceRequest serviceRequest, ServiceProvider serviceProvider);

    boolean canAccessEmail(ServiceProvider serviceProvider, ServiceRequest serviceRequest);

    boolean sendEmailRequest(ServiceRequest serviceRequest, ServiceProvider serviceProvider);

    List<EmailRequestInfoModel> getPendingEmailRequests(Customer customer);

    List<EmailRequestModel> getAcceptedEmailRequests(ServiceProvider serviceProvider);

    List<EmailRequestInfoModel> getEmailRequests(ServiceRequest serviceRequest);

    boolean acceptEmailRequest(EmailRequest emailRequest);

    void rejectEmailRequest(EmailRequest emailRequest);
}

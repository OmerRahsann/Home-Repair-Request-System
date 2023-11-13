package homerep.springy.service;

import homerep.springy.entity.EmailRequest;
import homerep.springy.entity.ServiceProvider;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.model.emailrequest.EmailRequestInfoModel;
import homerep.springy.model.emailrequest.EmailRequestModel;

import java.util.List;

public interface EmailRequestService {
    EmailRequestModel getEmail(ServiceRequest serviceRequest, ServiceProvider serviceProvider);

    boolean sendEmailRequest(ServiceRequest serviceRequest, ServiceProvider serviceProvider);

    List<EmailRequestInfoModel> listEmailRequests(ServiceRequest serviceRequest);

    void updateEmailRequestStatus(EmailRequest emailRequest, boolean accepted);
}

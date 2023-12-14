package homerep.springy;

import com.icegreen.greenmail.spring.GreenMailBean;
import homerep.springy.component.DummyDataComponent;
import homerep.springy.config.TestDatabaseConfig;
import homerep.springy.config.TestMailConfig;
import homerep.springy.config.TestStorageConfig;
import homerep.springy.controller.customer.CustomerEmailRequestController;
import homerep.springy.controller.provider.ServiceProviderEmailRequestController;
import homerep.springy.entity.Customer;
import homerep.springy.entity.EmailRequest;
import homerep.springy.entity.ServiceProvider;
import homerep.springy.entity.ServiceRequest;
import homerep.springy.exception.ApiException;
import homerep.springy.exception.NonExistentPostException;
import homerep.springy.model.ServiceRequestModel;
import homerep.springy.model.accountinfo.ServiceProviderInfoModel;
import homerep.springy.model.emailrequest.EmailRequestInfoModel;
import homerep.springy.model.emailrequest.EmailRequestModel;
import homerep.springy.model.emailrequest.EmailRequestStatus;
import homerep.springy.model.notification.NotificationModel;
import homerep.springy.model.notification.NotificationType;
import homerep.springy.repository.EmailRequestRepository;
import homerep.springy.service.EmailRequestService;
import homerep.springy.service.impl.NotificationServiceImpl;
import homerep.springy.type.User;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestDatabaseConfig
@Import({TestMailConfig.class, TestStorageConfig.class})
public class EmailRequestTest {

    @Autowired
    private EmailRequestRepository emailRequestRepository;

    @Autowired
    private EmailRequestService emailRequestService;

    @Autowired
    private CustomerEmailRequestController customerEmailRequestController;

    @Autowired
    private ServiceProviderEmailRequestController serviceProviderEmailRequestController;

    @Autowired
    private DummyDataComponent dummyDataComponent;

    @Autowired
    private NotificationServiceImpl notificationService;

    @Autowired
    private GreenMailBean greenMailBean;

    private Customer customer;
    private User customerUser;
    private ServiceProvider serviceProvider1;
    private User serviceProvider1User;
    private ServiceProvider serviceProvider2;
    private User serviceProvider2User;
    private ServiceRequest serviceRequest1;
    private ServiceRequest serviceRequest2;

    private static final String CUSTOMER_EMAIL = "test@localhost";

    private static final String SERVICE_PROVIDER_1_EMAIL = "example@example.com";

    private static final String SERVICE_PROVIDER_2_EMAIL = "example2@example.com";

    @BeforeEach
    void setup() {
        customer = dummyDataComponent.createCustomer(CUSTOMER_EMAIL);
        customerUser = new User(customer.getAccount());
        serviceRequest1 = dummyDataComponent.createServiceRequest(customer);
        serviceRequest2 = dummyDataComponent.createServiceRequest(customer);

        serviceProvider1 = dummyDataComponent.createServiceProvider(SERVICE_PROVIDER_1_EMAIL);
        serviceProvider1User = new User(serviceProvider1.getAccount());
        serviceProvider2 = dummyDataComponent.createServiceProvider(SERVICE_PROVIDER_2_EMAIL);
        serviceProvider2User = new User(serviceProvider2.getAccount());
    }

    @Test
    void getEmailNoRequest() {
        // No email requests
        assertTrue(emailRequestRepository.findAll().isEmpty());
        // Try to get the email without sending a request
        EmailRequestModel emailRequestModel = emailRequestService.getEmail(serviceRequest1, serviceProvider1);
        assertNotNull(emailRequestModel);
        assertEquals(ServiceRequestModel.fromEntity(serviceRequest1), emailRequestModel.serviceRequest());
        // customer info is not supplied
        assertNull(emailRequestModel.customer());
        // email is not supplied
        assertNull(emailRequestModel.email());
        assertEquals(EmailRequestStatus.NOT_REQUESTED, emailRequestModel.status());
    }

    @Test
    void getEmailAfterRequested() {
        // No email requests
        assertTrue(emailRequestRepository.findAll().isEmpty());
        Instant start = Instant.now();
        // Send an email request
        emailRequestService.sendEmailRequest(serviceRequest1, serviceProvider1);
        // Email request is created
        assertEquals(1, emailRequestRepository.findAll().size());
        EmailRequest emailRequest = emailRequestRepository.findAll().get(0);
        // status is PENDING
        assertEquals(EmailRequestStatus.PENDING, emailRequest.getStatus());
        // timestamp is sensible
        assertTrue(emailRequest.getRequestTimestamp().isAfter(start));
        // Try to get the email with a request that has not been accepted or denied
        EmailRequestModel emailRequestModel = emailRequestService.getEmail(serviceRequest1, serviceProvider1);
        assertNotNull(emailRequestModel);
        assertEquals(ServiceRequestModel.fromEntity(serviceRequest1), emailRequestModel.serviceRequest());
        // customer info is not supplied
        assertNull(emailRequestModel.customer());
        // email is not supplied
        assertNull(emailRequestModel.email());
        assertEquals(EmailRequestStatus.PENDING, emailRequestModel.status());
    }

    @Test
    @Transactional
    void getEmailAfterAccepted() {
        // No email requests
        assertTrue(emailRequestRepository.findAll().isEmpty());
        // Send an email request
        emailRequestService.sendEmailRequest(serviceRequest1, serviceProvider1);
        // Email request is created
        assertEquals(1, emailRequestRepository.findAll().size());
        EmailRequest emailRequest = emailRequestRepository.findAll().get(0);
        // status is PENDING
        assertEquals(EmailRequestStatus.PENDING, emailRequest.getStatus());
        // Accept the email request
        emailRequestService.acceptEmailRequest(emailRequest);
        // Email and customer info is provided after the request is accepted
        EmailRequestModel emailRequestModel = emailRequestService.getEmail(serviceRequest1, serviceProvider1);
        assertNotNull(emailRequestModel);
        assertEquals(ServiceRequestModel.fromEntity(serviceRequest1), emailRequestModel.serviceRequest());
        assertNotNull(emailRequestModel.customer());
        assertEquals(customer.getFirstName(), emailRequestModel.customer().firstName());
        assertEquals(customer.getMiddleName(), emailRequestModel.customer().middleName());
        assertEquals(customer.getLastName(), emailRequestModel.customer().lastName());
        assertNull(emailRequestModel.customer().address()); // Address is not provided
        assertEquals(customer.getPhoneNumber(), emailRequestModel.customer().phoneNumber());
        assertEquals(CUSTOMER_EMAIL, emailRequestModel.email());
        assertEquals(EmailRequestStatus.ACCEPTED, emailRequestModel.status());
    }

    @Test
    void getEmailAfterRejected() {
        // No email requests
        assertTrue(emailRequestRepository.findAll().isEmpty());
        // Send an email request
        emailRequestService.sendEmailRequest(serviceRequest1, serviceProvider1);
        // Email request is created
        assertEquals(1, emailRequestRepository.findAll().size());
        EmailRequest emailRequest = emailRequestRepository.findAll().get(0);
        // status is PENDING
        assertEquals(EmailRequestStatus.PENDING, emailRequest.getStatus());
        // Reject the email request
        emailRequestService.rejectEmailRequest(emailRequest);
        // Email is not provided after the request is rejected
        EmailRequestModel emailRequestModel = emailRequestService.getEmail(serviceRequest1, serviceProvider1);
        assertNotNull(emailRequestModel);
        assertEquals(ServiceRequestModel.fromEntity(serviceRequest1), emailRequestModel.serviceRequest());
        assertNull(emailRequestModel.customer());
        assertNull(emailRequestModel.email());
        assertEquals(EmailRequestStatus.REJECTED, emailRequestModel.status());
    }

    @Test
    void acceptAfterRejectRequest() {
        // Send an email request
        emailRequestService.sendEmailRequest(serviceRequest1, serviceProvider1);
        // Email request is created
        assertEquals(1, emailRequestRepository.findAll().size());
        EmailRequest emailRequest = emailRequestRepository.findAll().get(0);
        // status is PENDING
        assertEquals(EmailRequestStatus.PENDING, emailRequest.getStatus());
        // Reject the email request
        emailRequestService.rejectEmailRequest(emailRequest);
        assertEquals(EmailRequestStatus.REJECTED, emailRequest.getStatus());
        // Trying to accept the rejected email request does not work
        assertFalse(emailRequestService.acceptEmailRequest(emailRequest));
        assertEquals(EmailRequestStatus.REJECTED, emailRequest.getStatus());
    }

    @Test
    @Transactional
    void getEmailRequests() {
        // No email requests
        assertTrue(emailRequestRepository.findAll().isEmpty());
        // The email request list is also empty
        List<EmailRequestInfoModel> models = emailRequestService.getEmailRequests(serviceRequest1);
        assertTrue(models.isEmpty());
        // Send an email request
        emailRequestService.sendEmailRequest(serviceRequest1, serviceProvider1);
        // Email request is created
        assertEquals(1, emailRequestRepository.findAll().size());
        EmailRequest emailRequest = emailRequestRepository.findAll().get(0);
        // List includes the created email request
        models = emailRequestService.getEmailRequests(serviceRequest1);
        assertEquals(1, models.size());
        EmailRequestInfoModel model = models.get(0);
        // Model matches repository information
        assertEquals(emailRequest.getId(), model.id());
        assertEquals(emailRequest.getServiceRequest().getId(), model.serviceRequestId());
        assertEquals(ServiceProviderInfoModel.fromEntity(serviceProvider1), model.serviceProvider());
        assertEquals(EmailRequestStatus.PENDING, model.status()); // Status starts as PENDING
        assertEquals(emailRequest.getStatus(), model.status());
    }

    @Test
    void nonExistentPost() {
        // Trying to get the email for a non-existent post results in an ApiException
        assertThrows(NonExistentPostException.class,
                () -> serviceProviderEmailRequestController.getEmail(Integer.MAX_VALUE, serviceProvider1User));

        // Trying to send an email request for a non-existent post results in an ApiException
        assertThrows(NonExistentPostException.class,
                () -> serviceProviderEmailRequestController.requestEmail(Integer.MAX_VALUE, serviceProvider1User));

        // Trying to list email requests for a non-existent post results in an ApiException
        assertThrows(NonExistentPostException.class,
                () -> customerEmailRequestController.getEmailRequests(Integer.MAX_VALUE, customerUser));
    }

    @Test
    void nonExistentEmailRequest() {
        // Trying to accept a non-existent email request results in an ApiException
        ApiException exception = assertThrows(ApiException.class,
                () -> customerEmailRequestController.acceptEmailRequest(Integer.MAX_VALUE, customerUser));
        assertEquals(exception.getType(), "non_existent_email_request");
        // Trying to reject a non-existent email request results in an ApiException
        exception = assertThrows(ApiException.class,
                () -> customerEmailRequestController.rejectEmailRequest(Integer.MAX_VALUE, customerUser));
        assertEquals(exception.getType(), "non_existent_email_request");
    }

    @Test
    @Transactional
    void doubleEmailRequest() {
        // No email requests
        assertTrue(emailRequestRepository.findAll().isEmpty());
        // Sending a single email request is successful
        assertDoesNotThrow(() -> serviceProviderEmailRequestController.requestEmail(serviceRequest1.getId(), serviceProvider1User));
        assertEquals(1, emailRequestRepository.findAll().size());
        // Sending another email request results in an error
        ApiException exception = assertThrows(ApiException.class,
                () -> serviceProviderEmailRequestController.requestEmail(serviceRequest1.getId(), serviceProvider1User));
        assertEquals(exception.getType(), "already_requested");
        // no additional email requests are created
        assertEquals(1, emailRequestRepository.findAll().size());
    }

    @Test
    @Transactional
    void customerGetPendingEmailRequests() {
        // Multiple service providers can send an email request
        serviceProviderEmailRequestController.requestEmail(serviceRequest1.getId(), serviceProvider1User);
        serviceProviderEmailRequestController.requestEmail(serviceRequest1.getId(), serviceProvider2User);
        // Customer can get a list of all pending email requests
        List<EmailRequestInfoModel> requests = customerEmailRequestController.getPendingEmailRequests(customerUser);
        assertEquals(2, requests.size());
        // Both requests in the list are pending
        assertEquals(EmailRequestStatus.PENDING, requests.get(0).status());
        assertEquals(EmailRequestStatus.PENDING, requests.get(1).status());
        // request 0 has serviceProvider2's info
        assertEquals(ServiceProviderInfoModel.fromEntity(serviceProvider2), requests.get(0).serviceProvider());
        // request 1 has serviceProvider1's info
        assertEquals(ServiceProviderInfoModel.fromEntity(serviceProvider1), requests.get(1).serviceProvider());
        // List is sorted by request timestamp in descending order
        Instant request0Timestamp = requests.get(0).requestTimestamp();
        Instant request1Timestamp = requests.get(1).requestTimestamp();
        assertTrue(request0Timestamp.isAfter(request1Timestamp));

        // Accept and reject some requests
        customerEmailRequestController.acceptEmailRequest(requests.get(0).id(), customerUser);
        customerEmailRequestController.rejectEmailRequest(requests.get(1).id(), customerUser);
        // There's no more pending requests
        requests = customerEmailRequestController.getPendingEmailRequests(customerUser);
        assertTrue(requests.isEmpty());
    }

    @Test
    @Transactional
    void serviceProviderGetAcceptedEmailRequests() {
        // Send multiple email requests
        serviceProviderEmailRequestController.requestEmail(serviceRequest1.getId(), serviceProvider1User);
        serviceProviderEmailRequestController.requestEmail(serviceRequest2.getId(), serviceProvider1User);
        // There's no pending email requests were accepted
        List<EmailRequestModel> models = serviceProviderEmailRequestController.getAcceptedEmailRequests(serviceProvider1User);
        assertTrue(models.isEmpty());
        // Accept both requests
        for (EmailRequest emailRequest : emailRequestRepository.findAll()) {
            customerEmailRequestController.acceptEmailRequest(emailRequest.getId(), customerUser);
        }
        // Service provider get a list of accepted email requests
        models = serviceProviderEmailRequestController.getAcceptedEmailRequests(serviceProvider1User);
        assertEquals(2, models.size());
        // List is sorted by status update timestamp in descending order
        Instant request0Timestamp = models.get(0).updateTimestamp();
        Instant request1Timestamp = models.get(1).updateTimestamp();
        assertTrue(request0Timestamp.isAfter(request1Timestamp));
    }

    @Test
    void emailNotificationForRequest() throws Exception {
        greenMailBean.getGreenMail().purgeEmailFromAllMailboxes();
        assertEquals(0, greenMailBean.getReceivedMessages().length);

        // After sending an email request
        assertTrue(emailRequestService.sendEmailRequest(serviceRequest1, serviceProvider1));

        // A notification email is sent to the customer
        assertEquals(1, greenMailBean.getReceivedMessages().length);
        MimeMessage message = greenMailBean.getReceivedMessages()[0];
        // only to their registered email
        assertEquals(1, message.getAllRecipients().length);
        assertEquals(CUSTOMER_EMAIL, message.getAllRecipients()[0].toString());
        // with the service provider's information
        assertTrue(message.getContent() instanceof String);
        String content = (String) message.getContent();
        assertTrue(content.contains(serviceProvider1.getName()));
        assertTrue(content.contains(serviceProvider1.getDescription()));
        assertTrue(content.contains(serviceProvider1.getAddress()));
        assertTrue(content.contains(serviceProvider1.getContactEmailAddress()));
        assertTrue(content.contains(serviceProvider1.getPhoneNumber()));
        // and the title of the service request
        assertTrue(content.contains(serviceRequest1.getTitle()));
    }

    @Test
    void webNotificationForRequest() {
        // After sending an email request
        assertTrue(emailRequestService.sendEmailRequest(serviceRequest1, serviceProvider1));
        // A web notification is sent to the customer
        List<NotificationModel> notifications = notificationService.getNotifications(customer.getAccount());
        assertEquals(1, notifications.size());
        NotificationModel notification = notifications.get(0);
        // the title has the service provider's name
        assertTrue(notification.title().contains(serviceProvider1.getName()));
        // title specifies that it is a request
        assertTrue(notification.title().contains("requested"));
        // and the message has the title of the service request
        assertTrue(notification.message().contains(serviceRequest1.getTitle()));
        // type is NEW_EMAIL_REQUEST
        assertEquals(NotificationType.NEW_EMAIL_REQUEST, notification.type());
    }

    @Test
    @Transactional
    void emailNotificationForAccepting() throws Exception {
        // Send an email request
        assertTrue(emailRequestService.sendEmailRequest(serviceRequest1, serviceProvider1));
        assertEquals(1, emailRequestRepository.findAll().size());
        EmailRequest emailRequest = emailRequestRepository.findAll().get(0);
        // Ignore irrelevant emails
        greenMailBean.getGreenMail().purgeEmailFromAllMailboxes();
        assertEquals(0, greenMailBean.getReceivedMessages().length);
        // Accept the email request
        emailRequestService.acceptEmailRequest(emailRequest);
        // A notification email is sent to the service provider
        assertEquals(1, greenMailBean.getReceivedMessages().length);
        MimeMessage message = greenMailBean.getReceivedMessages()[0];
        // only to their registered email
        assertEquals(1, message.getAllRecipients().length);
        assertEquals(SERVICE_PROVIDER_1_EMAIL, message.getAllRecipients()[0].toString());
        // with the customer's information
        assertTrue(message.getContent() instanceof String);
        String content = (String) message.getContent();
        assertTrue(content.contains(customer.getFirstName()));
        assertTrue(content.contains(customer.getLastName()));
        assertTrue(content.contains(CUSTOMER_EMAIL));
        assertTrue(content.contains(customer.getPhoneNumber()));
        // and the title and description of the service request
        assertTrue(content.contains(serviceRequest1.getTitle()));
        assertTrue(content.contains(serviceRequest1.getDescription()));
    }

    @Test
    @Transactional
    void webNotificationForAccepting() {
        // Send an email request
        assertTrue(emailRequestService.sendEmailRequest(serviceRequest1, serviceProvider1));
        assertEquals(1, emailRequestRepository.findAll().size());
        EmailRequest emailRequest = emailRequestRepository.findAll().get(0);
        // Ignore other notifications
        notificationService.clearNotifications(serviceProvider1.getAccount());
        // Accept the email request
        emailRequestService.acceptEmailRequest(emailRequest);
        // A web notification is sent to the service provider
        List<NotificationModel> notifications = notificationService.getNotifications(serviceProvider1.getAccount());
        assertEquals(1, notifications.size());
        NotificationModel notification = notifications.get(0);
        // the title has the customer's name
        assertTrue(notification.title().contains(customer.getFirstName()));
        assertTrue(notification.title().contains(customer.getLastName()));
        // title specifies that the request was accepted
        assertTrue(notification.title().contains("accepted"));
        // the message has the title of the service request
        assertTrue(notification.message().contains(serviceRequest1.getTitle()));
        // type is ACCEPTED_EMAIL_REQUEST
        assertEquals(NotificationType.ACCEPTED_EMAIL_REQUEST, notification.type());
    }
}

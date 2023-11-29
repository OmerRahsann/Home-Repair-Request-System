package homerep.springy.entity;

import homerep.springy.model.emailrequest.EmailRequestStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;

@Entity
public class EmailRequest {
    @Id
    @GeneratedValue
    private long id;

    @ManyToOne(optional = false)
    private ServiceProvider serviceProvider;

    @ManyToOne(optional = false)
    @JoinColumn
    private ServiceRequest serviceRequest;

    @CreationTimestamp
    private Instant requestTimestamp;

    private Instant updateTimestamp;

    @Enumerated(EnumType.STRING)
    private EmailRequestStatus status;

    public EmailRequest() {}

    public EmailRequest(ServiceProvider serviceProvider, ServiceRequest serviceRequest) {
        this.serviceProvider = serviceProvider;
        this.serviceRequest = serviceRequest;
        this.status = EmailRequestStatus.PENDING;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    public void setServiceProvider(ServiceProvider requester) {
        this.serviceProvider = requester;
    }

    public ServiceRequest getServiceRequest() {
        return serviceRequest;
    }

    public void setServiceRequest(ServiceRequest serviceRequest) {
        this.serviceRequest = serviceRequest;
    }

    public Instant getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(Instant requestTime) {
        this.requestTimestamp = requestTime;
    }

    public Instant getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(Instant statusChangeTimestamp) {
        this.updateTimestamp = statusChangeTimestamp;
    }

    public EmailRequestStatus getStatus() {
        return status;
    }

    public void setStatus(EmailRequestStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailRequest that = (EmailRequest) o;
        return Objects.equals(serviceProvider, that.serviceProvider) && Objects.equals(serviceRequest, that.serviceRequest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceProvider, serviceRequest);
    }

    @Override
    public String toString() {
        return "EmailRequest{" +
                "id=" + id +
                ", requester=" + serviceProvider +
                ", serviceRequest=" + serviceRequest +
                ", requestTimestamp=" + requestTimestamp +
                '}';
    }
}

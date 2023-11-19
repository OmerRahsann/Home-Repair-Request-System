package homerep.springy.entity;

import homerep.springy.model.appointment.AppointmentStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
public class Appointment {
    @Id
    @GeneratedValue
    private long id;

    @ManyToOne(optional = false)
    private ServiceProvider serviceProvider;

    @ManyToOne(optional = false)
    private ServiceRequest serviceRequest;

    @ManyToOne(optional = false)
    private Customer customer;

    private Instant creationTimestamp;

    private Instant updateTimestamp;

    @Temporal(value = TemporalType.DATE)
    private LocalDate date; // TODO time periods instead of full days

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    private String message;

    public Appointment() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    public void setServiceProvider(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public ServiceRequest getServiceRequest() {
        return serviceRequest;
    }

    public void setServiceRequest(ServiceRequest serviceRequest) {
        this.serviceRequest = serviceRequest;
    }

    public Instant getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(Instant creationTime) {
        this.creationTimestamp = creationTime;
    }

    /**
     * @return a timestamp of when the service request was confirmed or cancelled
     */
    public Instant getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(Instant updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public AppointmentStatus getStatus() {
        updateStatus();
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String description) {
        this.message = description;
    }

    @PostLoad
    private void updateStatus() {
        LocalDate now = LocalDate.now();
        if (now.isAfter(getDate())) {
            // Make sure the status is updated in the database on the next save
            status = status.toExpiredStatus();
        }
    }
}

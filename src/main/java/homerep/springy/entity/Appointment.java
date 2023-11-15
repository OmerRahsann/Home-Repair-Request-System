package homerep.springy.entity;

import homerep.springy.model.appointment.AppointmentStatus;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
// TODO change this to a CHECK/EXCLUSION constraint when implementing time periods
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"customer_id", "date"}),
                @UniqueConstraint(columnNames = {"service_provider_id", "date"})
        }
)
public class Appointment {
    @Id
    @GeneratedValue
    private long id;

    @ManyToOne
    private ServiceProvider serviceProvider;

    @ManyToOne
    private ServiceRequest serviceRequest;

    @ManyToOne
    private Customer customer;

    @Temporal(value = TemporalType.DATE)
    private LocalDate date; // TODO time periods instead of full days

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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public AppointmentStatus getStatus() {
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
}

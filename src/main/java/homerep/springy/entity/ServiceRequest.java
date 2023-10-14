package homerep.springy.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "service_request")
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private int requestId;

    @Column(name = "customer_id")
    private int customerId;

    @Column(name = "request_description", length = 256)
    private String requestDescription;

    @Column(name = "request_status", length = 16)
    private String requestStatus;

    @Column(name = "request_date")
    private Timestamp requestDate;

    @Column(name = "geo_location", length = 128)
    private String geoLocation;

    public ServiceRequest() {
    }

    public ServiceRequest(int customerId, String requestDescription, String requestStatus, Timestamp requestDate, String geoLocation) {
        this.customerId = customerId;
        this.requestDescription = requestDescription;
        this.requestStatus = requestStatus;
        this.requestDate = requestDate;
        this.geoLocation = geoLocation;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getRequestDescription() {
        return requestDescription;
    }

    public void setRequestDescription(String requestDescription) {
        this.requestDescription = requestDescription;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public Timestamp getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Timestamp requestDate) {
        this.requestDate = requestDate;
    }

    public String getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(String geoLocation) {
        this.geoLocation = geoLocation;
    }
}


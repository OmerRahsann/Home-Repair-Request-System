package homerep.springy.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.*;

@Entity
public class ServiceRequest {

    @Id
    @GeneratedValue
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer;

    private String title;

    private String description;

    private String service;

    private Status status;

    private int dollars;

    private Date creationDate;

    private String address;

    private double latitude;

    private double longitude;

    private Instant locationRetrievalTime;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @OrderColumn
    private List<ImageInfo> pictures = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "serviceRequest", cascade = CascadeType.REMOVE)
    @OrderBy("requestTimestamp desc")
    private Set<EmailRequest> emailRequests = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "serviceRequest", cascade = CascadeType.REMOVE)
    private List<Appointment> appointments = new ArrayList<>();

    protected ServiceRequest() {}

    public ServiceRequest(Customer customer) {
        this.customer = customer;
    }

    public int getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getDollars() {
        return dollars;
    }

    public void setDollars(int dollars) {
        this.dollars = dollars;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date date) {
        this.creationDate = date;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPictures(List<ImageInfo> pictures) {
        this.pictures = pictures;
    }

    public List<ImageInfo> getPictures() {
        return pictures;
    }

    public List<String> getImagesUUIDs() {
        return pictures.stream()
                .map(ImageInfo::getUuid)
                .map(UUID::toString)
                .toList();
    }

    public void setLatitude(double latitude){
        this.latitude = latitude;
    }

    public double getLatitude(){
        return this.latitude;
    }

    public void setLongitude(double longitude){
        this.longitude = longitude;
    }

    public double getLongitude(){
        return this.longitude;
    }

    public Instant getLocationRetrievalTime() {
        return locationRetrievalTime;
    }

    public void setLocationRetrievalTime(Instant locationRetrievalTime) {
        this.locationRetrievalTime = locationRetrievalTime;
    }

    public Set<EmailRequest> getEmailRequests() {
        return emailRequests;
    }

    public void setEmailRequests(Set<EmailRequest> emailRequests) {
        this.emailRequests = emailRequests;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    public enum Status {
        PENDING,
        CONFIRMED,
        IN_PROGRESS,
        COMPLETED
    }
}


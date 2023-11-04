package homerep.springy.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class ServiceProvider {
    @Id
    @GeneratedValue
    private int providerId;

    @OneToOne
    private Account account;

    private String name;

    private String description;

    @ElementCollection
    private List<String> services;

    private String phoneNumber;

    private String address;

    private String contactEmailAddress;

    private double longitude;

    private double latitude;

    protected ServiceProvider() {}

    public ServiceProvider(Account account) {
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactEmailAddress() {
        return contactEmailAddress;
    }

    public void setContactEmailAddress(String contactEmailAddress) {
        this.contactEmailAddress = contactEmailAddress;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }    

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}

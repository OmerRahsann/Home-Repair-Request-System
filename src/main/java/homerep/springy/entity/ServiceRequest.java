package homerep.springy.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
public class ServiceRequest {

    @Id
    @GeneratedValue
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer;

    private String title;

    private String description;

    private int dollars;

    private Date date;

    private String address;

    @OneToMany
    @OrderColumn
    private List<ImageInfo> pictures = new ArrayList<>();

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

    public int getDollars() {
        return dollars;
    }

    public void setDollars(int dollars) {
        this.dollars = dollars;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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
}


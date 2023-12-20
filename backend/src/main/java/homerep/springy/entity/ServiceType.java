package homerep.springy.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "services")
public class ServiceType {
    @Id
    @GeneratedValue
    private int id;

    @Column(unique = true)
    private String serviceType;

    protected ServiceType() {}

    public ServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public int getId() {
        return id;
    }

    public String getServiceType() {
        return serviceType;
    }
}

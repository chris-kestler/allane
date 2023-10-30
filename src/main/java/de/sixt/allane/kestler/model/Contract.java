package de.sixt.allane.kestler.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;

/**
 * Boilerplate Hibernate Pojo for Contract
 */
@Entity
public class Contract extends AbstractIdentifiableClass<Long> {
    private float rate;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "customer")
    @JsonBackReference("customer")
    @Nullable
    private Customer customerData;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "vehicle")
    @JsonBackReference("vehicle")
    @Nullable
    private Vehicle vehicleData;

    public Contract(){}

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    public void setCustomer(@Nullable Customer customer) {
        this.customerData = customer;
    }

    public void setVehicle(@Nullable Vehicle vehicleData) {
        this.vehicleData = vehicleData;
    }

    public Vehicle getVehicle() {
        return vehicleData;
    }

    public Customer getCustomer() {
        return customerData;
    }
}

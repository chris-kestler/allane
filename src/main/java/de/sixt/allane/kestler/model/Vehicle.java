package de.sixt.allane.kestler.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

/**
 * Boilerplate Hibernate Pojo for Vehicle
 */
@Entity
public class Vehicle extends AbstractIdentifiableClass<Long> {
    private String vin;

    private String make, model;

    private short year;

    private float price;

    @Transient
    private boolean used = false; // this is used by Rental service to provide the client a cheap way to check which vehicles are already used

    public Vehicle(){}

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public short getYear() {
        return year;
    }

    public void setYear(short year) {
        this.year = year;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public boolean isUsed() {
        return used;
    }

    public void markUsed() {
        this.used = true;
    }
}

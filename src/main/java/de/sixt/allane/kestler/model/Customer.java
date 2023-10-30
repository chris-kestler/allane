package de.sixt.allane.kestler.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Date;

/**
 * Boilerplate Hibernate Pojo for Customer
 */
@Entity
@Table
public class Customer extends AbstractIdentifiableClass<Long> {
    private String vorname, nachname;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
    private Date dob;

    public Customer(){
        super();
    }

    public String getVorname() {
        return vorname;
    }

    public String getNachname() {
        return nachname;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.entities.user;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 * @author stephan
 */
@Entity @Table(name="ADDRESSES")
public class AddressEntity implements Serializable {    
    @GeneratedValue
    @Id 
    private Long id; 
    private String street; 
    private String city; 
    private String addressState; 
    private String country;
    private String zip; 
    private String phone;

    public AddressEntity() {
    }
    
    public AddressEntity(AddressEntity ae){
        this.street = ae.street;
        this.city= ae.city;
        this.addressState=ae.addressState;
        this.country = ae.country;
        this.zip = ae.zip; 
        this.phone = ae.phone; 
                
    }
    
    
    public String getAddressState() {
        return addressState;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public Long getId() {
        return id;
    }

    public String getPhone() {
        return phone;
    }

    public String getStreet() {
        return street;
    }

    public String getZip() {
        return zip;
    }

    public void setAddressState(String addressState) {
        this.addressState = addressState;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

  
    
    }

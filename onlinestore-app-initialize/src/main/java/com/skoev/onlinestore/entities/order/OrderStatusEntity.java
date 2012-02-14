/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.entities.order;

import com.skoev.onlinestore.entities.user.UserEntity;


import java.math.BigDecimal;
import java.util.*;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.ElementCollection;
import java.io.Serializable;
import javax.persistence.FetchType;
import javax.persistence.OrderColumn;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany; 
import javax.persistence.FetchType; 
import javax.persistence.CascadeType;
import javax.persistence.Temporal; 
import javax.persistence.TemporalType; 
import javax.persistence.Enumerated; 
import javax.persistence.EnumType;
import java.text.*;
/**
 *
 * @author stephan
 */

@Entity @Table(name="ORDER_STATUSES")
public class OrderStatusEntity implements Serializable {
   @GeneratedValue
   @Id
   private Long id; 
   @Enumerated(EnumType.STRING)
   private OrderStatusEnum status; 
   @Temporal(TemporalType.TIMESTAMP)
   private Date statusBegan; 
   private String actor; 
   //@ManyToOne
   //private UserEntity actor;

    public String getActor() {
        return actor;
    }

    public Long getId() {
        return id;
    }

    public OrderStatusEnum getStatus() {
        return status;
    }

    public Date getStatusBegan() {
        return statusBegan;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStatus(OrderStatusEnum status) {
        this.status = status;
    }

    public void setStatusBegan(Date statusBegan) {
        this.statusBegan = statusBegan;
    }
   
    public String getStatusBeganFormatted(){
    DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        return dateFormat.format(statusBegan); 
        
    }
   
   
   
}

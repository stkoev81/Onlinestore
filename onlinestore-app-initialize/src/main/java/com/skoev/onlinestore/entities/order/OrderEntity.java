/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.entities.order;


import com.skoev.onlinestore.entities.user.*; 

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
import java.math.BigDecimal; 
import java.util.*; 
import javax.persistence.*; 
import java.text.*; 
/**
 *
 * @author stephan
 */

@Entity @Table(name="ORDERS")
public class OrderEntity implements Serializable {
   @Transient
   private DateFormat dateFormat;
    
   @GeneratedValue
   @Id
   private Long id;  
   private String shippingMethod;
   private BigDecimal shippingCost;   
   private BigDecimal totalCost;
   @Temporal(TemporalType.TIMESTAMP)
   private Date orderDate;  
   @Temporal(TemporalType.TIMESTAMP)
   private Date lastModified;  
   private String lastActor; 
   private String currentStatus; 
   @Version
   private Integer version;

    public OrderEntity() {
        dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
    }
   
   
   //bidirectional
   @ManyToOne(fetch=FetchType.EAGER) @JoinColumn   
   private UserEntity customer;    
   
   //unidirectional
   @ManyToOne(fetch=FetchType.EAGER) @JoinColumn
   private UserInfoEntity ui;
   
   //unidirectional
   @OneToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL) @JoinColumn
   private List<OrderLineEntity> orderLines;
   
   //unidirectional
   @OneToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL) @JoinColumn
   @OrderColumn(name = "ordering") 
   private List<OrderStatusEntity> statusHistory; 
   
  
      

    public UserInfoEntity getUi() {
        return ui;
    }

    public void setUi(UserInfoEntity ui) {
        this.ui = ui;
    }

          

    public UserEntity getCustomer() {
        return customer;
    }

    public Long getId() {
        return id;
    }

 
    public List<OrderLineEntity> getOrderLines() {
        return orderLines;
    }

    public BigDecimal getShippingCost() {
        return shippingCost;
    }

    public String getShippingMethod() {
        return shippingMethod;
    }

    public List<OrderStatusEntity> getStatusHistory() {
        return statusHistory;
    }
 

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setCustomer(UserEntity customer) {
        this.customer = customer;
    }

    public void setId(Long id) {
        this.id = id;
    }

 

    public void setOrderLines(List<OrderLineEntity> orderLines) {
        this.orderLines = orderLines;
    }

    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
    }

    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }

    public void setStatusHistory(List<OrderStatusEntity> statusHistory) {
        this.statusHistory = statusHistory;
    }



    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }
    

    
    public String getLastActor(){
        return lastActor;
    }
   
    public Date getLastModified(){
        return lastModified;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }



    public void setLastActor(String lastActor) {
        this.lastActor = lastActor;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
    
    
    public String getNextActorTypeRequired(){
        return OrderAction.nextAction(this).getActorType();
    }
    
    public String getNextActionRequired(){
        return OrderAction.nextAction(this).getActionRequired();
    }
    
    public String getOrderDateFormatted(){        
        return dateFormat.format(orderDate); 
    }
    public String getLastModifiedFormatted(){
        return dateFormat.format(lastModified);
    }
   
    
}

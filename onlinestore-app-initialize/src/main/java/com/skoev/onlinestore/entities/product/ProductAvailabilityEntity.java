/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.entities.product;

import java.math.BigDecimal;
import java.util.*;
import java.io.Serializable;
import javax.persistence.*; 
import java.text.DateFormat; 
/**
 *
 * @author stephan
 */

@Entity @Table(name="PRODUCT_AVAILABILITY")
public class ProductAvailabilityEntity implements Serializable {
   @GeneratedValue
   @Id
   private Long id;
   private Integer numberAvailable; 
   private Integer numberInWarehouse; 
   private Integer numberInCarts; 
   private Integer numberInUnprocessedOrders;

    
   public void calculateNumberAvailable(){
       numberAvailable = numberInWarehouse - numberInCarts - numberInUnprocessedOrders; 
    
   }
           
   public Integer getNumberAvailable() {
        return numberAvailable;
    }

    public Integer getNumberInCarts() {
        return numberInCarts;
    }

    public Integer getNumberInUnprocessedOrders() {
        return numberInUnprocessedOrders;
    }

    public Integer getNumberInWarehouse() {
        return numberInWarehouse;
    }


    public void setNumberAvailable(Integer numberAvailable) {
        this.numberAvailable = numberAvailable;
    }

    public void setNumberInCarts(Integer numberInCarts) {
        this.numberInCarts = numberInCarts;
    }

    public void setNumberInUnprocessedOrders(Integer numberInUnprocessedOrders) {
        this.numberInUnprocessedOrders = numberInUnprocessedOrders;
    }

    public void setNumberInWarehouse(Integer numberInWarehouse) {
        this.numberInWarehouse = numberInWarehouse;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

   
  
   
}

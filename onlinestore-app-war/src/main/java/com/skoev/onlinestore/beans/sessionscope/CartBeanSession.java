/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.skoev.onlinestore.beans.sessionscope;



import java.io.Serializable;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.event.ActionEvent; 
import javax.ejb.EJB;
import com.skoev.onlinestore.ejb.*;
import java.util.*; 
import javax.annotation.PreDestroy;

/**
 *
 * @author stephan
 */
@Named
@SessionScoped
public class CartBeanSession implements Serializable {
   // action listener that checks for cookies when cart button is pressed
   @EJB CartStateful cartStateful;
    
     //action listener 
   @PreDestroy
   public void preDestroy(){
        
        cartStateful.emptyCart();
   }
   
    public CartStateful getCartStateful() {
        return cartStateful;
    }

    public void setCartStateful(CartStateful cartStateful) {
        this.cartStateful = cartStateful;
    }
    
    public String action(){
    
        return null; 
    }
}

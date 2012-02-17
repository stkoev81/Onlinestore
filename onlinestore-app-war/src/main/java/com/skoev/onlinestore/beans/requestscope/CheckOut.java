/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.beans.requestscope;

import com.skoev.onlinestore.beans.sessionscope.*; 

import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.event.ActionEvent; 
import javax.inject.Inject;

import javax.faces.context.FacesContext;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.annotation.PostConstruct;
import javax.faces.event.*;
import java.util.*; 

/**
 *
 * @author stephan
 */
@Named
@RequestScoped
public class CheckOut {
    private String cardMonth; 
    private String cardYear; 
     
    
    
    
    
   // @Inject private CartBeanConversation cartBeanConversation; 
   @Inject private RequestInfo requestInfo; 
//    @Inject private BrowseStore browseStore; 
    @Inject private CartBeanSession cartBeanSession;
    
      /**
     * PostAddToView listener that gets called when the customer opens the 
     * CheckOut page
     * @param event 
     */  
       public void checkOutPageListener(ComponentSystemEvent event){      
            //if logged in, set the ui information for the account accordingly
           // if not logged in, user will have to enter information.
           
           cartBeanSession.getCartStateful().initOrder(requestInfo.getRequest().getUserPrincipal());
          Date date = cartBeanSession.getCartStateful().getUi().getCardExpirationDate();
           Calendar cal = Calendar.getInstance();
         if (date!=null){
            cal.setTime(date); 
            cardMonth = Integer.toString(cal.get(Calendar.MONTH)+ 1);
            cardYear = Integer.toString(cal.get(Calendar.YEAR));                        
         }
         
       }
    
       
    public String checkOutAction(){
        Calendar cardExp = Calendar.getInstance();
           cardExp.clear();
           cardExp.set(Calendar.YEAR, Integer.parseInt(cardYear));
           cardExp.set(Calendar.MONTH, Integer.parseInt(cardMonth)-1);
           cardExp.set(Calendar.DATE, cardExp.getActualMaximum(Calendar.DATE)); 
           cartBeanSession.getCartStateful().getUi().setCardExpirationDate(cardExp.getTime());
           cartBeanSession.getCartStateful().placeOrder();
        return "/BrowseStore/OrderConfirmation.xhtml?faces-redirect=true";
    }

    public String getCardMonth() {
        return cardMonth;
    }

    public String getCardYear() {
        return cardYear;
    }

 

    public void setCardMonth(String cardMonth) {
        this.cardMonth = cardMonth;
    }

    public void setCardYear(String cardYear) {
        this.cardYear = cardYear;
    }

    
    
   
    
}

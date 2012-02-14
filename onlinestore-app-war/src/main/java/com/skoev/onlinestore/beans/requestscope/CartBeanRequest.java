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
import javax.annotation.PostConstruct; import javax.faces.event.*;
import com.skoev.onlinestore.ejb.*; 
import javax.faces.application.FacesMessage; 
import javax.faces.context.FacesContext;

 
/**
 *
 * @author stephan
 */
@Named
@RequestScoped
public class CartBeanRequest {
    private String outcome; 
    private String shoppingPageURL; 
    private Long productID; 
    
    
   // @Inject private CartBeanConversation cartBeanConversation; 
    @Inject private RequestInfo requestInfo; 
    @Inject private BrowseStore browseStore; 
    @Inject private CartBeanSession cartBeanSession;
    

    @PostConstruct
    public void initialize(){
        shoppingPageURL = requestInfo.getRequest().getServletPath() +"?"+ requestInfo.getRequest().getQueryString();
        productID = browseStore.getSingleProductID(); 
    }
    
    /** Creates a new instance of CartManipulatorRequest */
    public CartBeanRequest() {
    }
    
   
    public String addToCartAction(){
         
       try {
       cartBeanSession.getCartStateful().addToCart(productID, 1);        
       }
       catch(ProductNotAvailableException e){
             FacesMessage message = new FacesMessage("Error! This product is no"
                     + " longer available. "); 
            message.setSeverity(FacesMessage.SEVERITY_ERROR); 
            FacesContext.getCurrentInstance().addMessage(null, message);           
           
           return null; 
       }
       
       return "/BrowseStore/ItemsAddedCart.xhtml"; 
   }
    
    

    
    public String updateCartAction(){
        cartBeanSession.getCartStateful().updateCart();        
        return null; 
    }
    
    public String emptyCartAction(){
        cartBeanSession.getCartStateful().emptyCart();
        return "/BrowseStore/ViewCart.xhtml?faces-redirect=true"; 
    }
    
    public boolean isCartEmpty(){
        return cartBeanSession.getCartStateful().getCartContents().isEmpty();
    }
    

    public String getShoppingPageURL() {
        return shoppingPageURL;
    }

    public void setShoppingPageURL(String shoppingPageURL) {
        this.shoppingPageURL = shoppingPageURL;
    }

    public Long getProductID() {
        return productID;
    }

    public void setProductID(Long productID) {
        this.productID = productID;
    }
   
    
}

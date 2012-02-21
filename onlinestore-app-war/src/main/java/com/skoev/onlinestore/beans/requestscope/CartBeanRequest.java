package com.skoev.onlinestore.beans.requestscope;

import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.annotation.PostConstruct;import com.skoev.onlinestore.ejb.*; 
import javax.faces.application.FacesMessage; 
import javax.faces.context.FacesContext;
import com.skoev.onlinestore.beans.sessionscope.*; 
 
/**
 * This class is used to add and remove products to and from the shopping cart. 
 * It is a request scoped CDI managed bean. Internally, it obtains a 
 * {@link com.skoev.onlinestore.ejb.CartStateful} instance from {@link
 * com.skoev.onlinestore.beans.sessionscope.CartBeanSession} and calls the 
 * methods on that instance to perform shopping cart operations. 
 * 
 * @see com.skoev.onlinestore.beans.sessionscope.CartBeanSession
 * @see com.skoev.onlinestore.ejb.CartStateful
 */
@Named
@RequestScoped
public class CartBeanRequest {
    /**
     * The URL of the page to which the customer will be returned 
     * after adding products to cart
     */
    private String shoppingPageURL; 
    /**
     * The ID of the product that will be added to the shopping cart
     */
    private Long productID; 
    
    @Inject private RequestInfo requestInfo; 
    @Inject private BrowseStore browseStore; 
    @Inject private CartBeanSession cartBeanSession;
    
    /**
     * Sets {@link #shoppingPageURL} and {@link #productID} by reading values
     * from the HTTP request query string. This method is called by the
     * container automatically right after the bean is constructed.
     */
    @PostConstruct
    public void initialize(){
        shoppingPageURL = requestInfo.getRequest().getServletPath() +"?"+ requestInfo.getRequest().getQueryString();
        productID = browseStore.getSingleProductID(); 
    }
    
    public CartBeanRequest() {
    }
    
    /**
     * Add the product to the shopping cart. If a 
     * {@link com.skoev.onlinestore.ejb.ProductNotAvailableException} is caught, 
     * returns null and sets a FacesMessage for the user. 
     * @return The name of the "Items added to cart" page if there is no 
     * ProductNotAvailableException; null otherwise. 
     */
    public String addToCartAction(){
       try {
            cartBeanSession.getCartStateful().addToCart(productID);        
       }
       catch(ProductNotAvailableException e){
            FacesMessage message = new FacesMessage(e.userMessage()); 
            message.setSeverity(FacesMessage.SEVERITY_ERROR); 
            FacesContext.getCurrentInstance().addMessage(null, message);           
           return null; 
       }
       return "/BrowseStore/ItemsAddedCart.xhtml"; 
   }
    
    /**
     * Updates the cart with the latest product quantities. 
     * It should be called after 
     * modifying the contents of CartStateful. It calls 
     * {@link com.skoev.onlinestore.ejb.CartStateful#updateCart()} internally.
     
     * @return null (so JSF navigation stays on the same page).
     */
    public String updateCartAction(){
        cartBeanSession.getCartStateful().updateCart();        
        return null; 
    }
    
    /**
     * Empties the contents of CartStateuf. Internally, this method calls 
     * {@link com.skoev.onlinestore.ejb.CartStateful#emptyCart()}
     * @return 
     */
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

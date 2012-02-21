package com.skoev.onlinestore.beans.sessionscope;

import java.io.Serializable;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.ejb.EJB;
import com.skoev.onlinestore.ejb.*;
import javax.annotation.PreDestroy;

/**
 * This session scoped CDI managed bean is simply used to keep a 
 * {@link com.skoev.onlinestore.ejb.CartStateful} object. Request scoped beans 
 * obtain the CartStateful object from here; this way, multiple requests by the 
 * same user will have access to the same shopping cart. 
 * 
 * @see com.skoev.onlinestore.ejb.CartStateful
 * @see com.skoev.onlinestore.beans.requestscope.CartBeanRequest
 */
@Named
@SessionScoped
public class CartBeanSession implements Serializable {
    /**
     * Injected EJB that represents shopping cart. 
     */
    @EJB
    CartStateful cartStateful;

    /**
     * Empties the shopping cart by calling 
     * {@link com.skoev.onlinestore.ejb.CartStateful#emptyCart()}
     * . This method is called automatically by the 
     * container right before an HTTP session ends (either due to timeout or due
     * to signing out by the user).
     */
    @PreDestroy
    public void preDestroy() {
        cartStateful.emptyCart();
    }

    public CartStateful getCartStateful() {
        return cartStateful;
    }

    public void setCartStateful(CartStateful cartStateful) {
        this.cartStateful = cartStateful;
    }
}

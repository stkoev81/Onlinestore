package com.skoev.onlinestore.beans.sessionscope;

import java.util.*;
import javax.inject.Named;
import javax.inject.Inject;
import javax.enterprise.context.SessionScoped;
import javax.ejb.*;
import javax.faces.event.ComponentSystemEvent; 
import java.io.Serializable;
import com.skoev.onlinestore.entities.order.*;
import com.skoev.onlinestore.ejb.*; 
import com.skoev.onlinestore.beans.requestscope.RequestInfo;

/**
 * This class is used for showing a user the orders he has placed. It is a 
 * session scoped CDI managed bean. 
 * 
 */
@Named(value = "orderHistory")
@SessionScoped
public class OrderHistory implements Serializable{
    @Inject
    private RequestInfo requestInfo;   
    @EJB
    private EntityAccessorStateless entityAccessor;
    /**
     * List of all the orders placed by that user
     */
    private List<OrderEntity> allOrders;
    /**
     * A single order placed by that user to be looked up
     */
    private OrderEntity singleOrder; 
    /**
     * ID which will be used for looking up a single order 
     */
    private Long singleOrderID; 

    public OrderHistory() {
    }
       
    /**
     * preRenderView listener for the "Order history" page. 
     * It finds in the database all orders placed 
     * by the currently logged in user. The results are available through
     * {@link #getAllOrders()}. 
     * 
     * @param event 
     */
    public void queryOrders(ComponentSystemEvent event){
        String username = requestInfo.getRequest().getUserPrincipal().getName();
        String query = "SELECT p FROM OrderEntity p WHERE p.customer.username="
                + "'"+username+"' ORDER BY p.orderDate DESC";
        allOrders = entityAccessor.doQuery(OrderEntity.class, query);
    }
  
    /**
     * This method looks up a single order that the current user has placed. 
     * Before the call, the order ID must be set with {@link #setSingleOrderID}. 
     * After the call, the looked up order is available through 
     * {@link #getSingleOrder} so details about it can be presented to the user.
     * 
     * @return The name of the "Order details" page
     */
    public String viewDetailsAction(){
           singleOrder = entityAccessor.findEntity(OrderEntity.class
                   , singleOrderID);
        return "/InsideAccount/OrderDetails.xhtml";
    }
    
    public List<OrderEntity> getAllOrders() {
        return allOrders;
    }

    public void setAllOrders(List<OrderEntity> allOrders) {
        this.allOrders = allOrders;
    }

    public OrderEntity getSingleOrder() {
        return singleOrder;
    }

    public Long getSingleOrderID() {
        return singleOrderID;
    }

    public void setSingleOrder(OrderEntity singleOrder) {
        this.singleOrder = singleOrder;
    }

    public void setSingleOrderID(Long singleOrderID) {
        this.singleOrderID = singleOrderID;
    }
   
    public boolean showOrderHistory(){
        return allOrders!=null && !allOrders.isEmpty(); 
    }
  
}

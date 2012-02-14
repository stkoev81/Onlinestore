/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.beans.sessionscope;

import com.skoev.onlinestore.beans.requestscope.RequestInfo;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import java.util.*; 
import com.skoev.onlinestore.entities.order.*;
import com.skoev.onlinestore.ejb.*; 

import javax.ejb.*;
import javax.faces.event.ComponentSystemEvent; 
import java.io.Serializable;

/**
 *
 * @author stephan
 */
@Named(value = "orderHistory")
@SessionScoped
public class OrderHistory implements Serializable{
    @Inject
    private RequestInfo requestInfo;   
    @EJB
    private EntityAccessorStateless entityAccessor;
    private List<OrderEntity> allOrders;
    private OrderEntity singleOrder; 
    private Long singleOrderID; 

    /** Creates a new instance of OrderHistory */
    public OrderHistory() {
    }

       //preRenderView listener
    public synchronized void queryOrders(ComponentSystemEvent event){
        String username = requestInfo.getRequest().getUserPrincipal().getName();
        String query = "SELECT p FROM OrderEntity p WHERE p.customer.username="
                + "'"+username+"' ORDER BY p.orderDate DESC";
        allOrders = entityAccessor.doQuery(OrderEntity.class, query);
    }
    
  
    
    public synchronized String viewDetailsAction(){
           singleOrder = entityAccessor.getEntity(OrderEntity.class, singleOrderID);
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.entities.order;
import java.util.*;

/**
 *
 * @author stephan
 */
public class OrderAction {
    public static final String ACCOUNTANT = "ACCOUNTANT"; 
    public static final String MANAGER = "MANAGER"; 
    public static final String WAREHOUSE = "WAREHOUSE_WORKER";
    
    private String actionRequired; 
    private String actorType; 
    private List<OrderStatusEnum> possibleStatuses;

    public OrderAction(String actionRequired, String actorType, OrderStatusEnum...ps) {
        this.actionRequired = actionRequired;
        this.actorType = actorType;
        this.possibleStatuses = new LinkedList<OrderStatusEnum>();
        for(OrderStatusEnum os:ps){
            this.possibleStatuses.add(os);
        }
    }
    
    public static  OrderAction nextAction(OrderEntity order){    
        
    switch (getCurrentStatus(order)){
        
        case ORDER_RECEIVED: return new OrderAction("Start processing "
                + "payment", ACCOUNTANT,OrderStatusEnum.PAYMENT_PROCESSING); 
            
        case PAYMENT_PROCESSING: return new OrderAction("Confirm payment "
                + "success", ACCOUNTANT,OrderStatusEnum.PAYMENT_RECEIVED
                , OrderStatusEnum.PAYMENT_FAILED);             
            
        case PAYMENT_FAILED: return new OrderAction("None","None");
            // add a call to advance automatically to CLOSED
            
            
        case PAYMENT_RECEIVED: return new OrderAction("Start shipment "
                + "processing", WAREHOUSE,OrderStatusEnum.SHIPMENT_PROCESSING); 
        
        case SHIPMENT_PROCESSING: return new OrderAction("Confirm shipment "
                + "sent", WAREHOUSE,OrderStatusEnum.SHIPPED
                , OrderStatusEnum.OUT_OF_STOCK);
            
        case OUT_OF_STOCK: return new OrderAction("Start processing refund", 
                ACCOUNTANT, OrderStatusEnum.REFUND_PROCESSING); 
            // add a call to send an e-mail automatically
        
        case REFUND_PROCESSING: return new OrderAction("Confirm refund "
                + "completed", ACCOUNTANT, OrderStatusEnum.REFUND_COMPLETED);
            
        case REFUND_COMPLETED: return new OrderAction("None", "None"); 
            // add a call to advance automatically to CLOSED
            
        case SHIPPED: return new OrderAction("If customer requests a return, decide to approve or reject it", MANAGER
                , OrderStatusEnum.RETURN_APPROVED, OrderStatusEnum.ORDER_CLOSED); 
            // add a call to advance automatically to CLOSED after a certain time
            
 
        case RETURN_APPROVED: return new OrderAction("Confirm that return was"
                + " received", WAREHOUSE, OrderStatusEnum.RETURN_RECEIVED); 
        
            
        
        case RETURN_RECEIVED: return new OrderAction("Start processing refund",
                ACCOUNTANT, OrderStatusEnum.REFUND_PROCESSING); 
            
        case ORDER_CLOSED: return new OrderAction("None", "None");
            //from this state, nothing can be done to an order anymore except by
            //Administrator
        
        default: return null;
    }
    }
    
    private static OrderStatusEnum getCurrentStatus(OrderEntity order){
        List<OrderStatusEntity> history = order.getStatusHistory();
        //size must be at least 1;
        return history.get(history.size()-1).getStatus();    
    }
    
    
    

    public String getActionRequired() {
        return actionRequired;
    }

    public String getActorType() {
        return actorType;
    }

    public List<OrderStatusEnum> getPossibleStatuses() {
        return possibleStatuses;
    }

    public void setActionRequired(String actionRequired) {
        this.actionRequired = actionRequired;
    }

    public void setActorType(String actorType) {
        this.actorType = actorType;
    }

    public void setPossibleStatuses(List<OrderStatusEnum> possibleStatuses) {
        this.possibleStatuses = possibleStatuses;
    }
        
    
    
    
}

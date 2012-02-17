package com.skoev.onlinestore.entities.order;

import java.util.*;

/**
 * This class represents an action taken on an order and has a static method
 * {@link #nextAction} for determining the proper action to be taken on an order
 * based on its current status. This method contains the logic for the order
 * processing sequence. 
 * 
 * @see OrderStatusEnum
 * @see OrderStatusEntity
 */
public class OrderAction {

    
    private static final String ACCOUNTANT = "ACCOUNTANT";
    private static final String MANAGER = "MANAGER";
    private static final String WAREHOUSE = "WAREHOUSE_WORKER";
    
    /**
     * A description of the action that must be taken next (for example "Start 
     * processing payment"). This description will be seen by the employees. 
     * After the employee completes the action, they will update the order
     * status accordingly. 
     */
    private String actionRequired;
    /**
     * The type of employee that must take the next action: ACCOUNTANT, MANAGER, 
     * or WAREHOUSE_WORKER
     *
     */
    private String actorType;
    /**
     * The possible order statuses that can result from taking the next action. 
     */
    private List<OrderStatusEnum> possibleStatuses;

    /**
     * Constructs a new OrderAction object 
     * @param actionRequired
     * @param actorType
     * @param ps Possible status
     */
    public OrderAction(String actionRequired, String actorType, OrderStatusEnum... ps) {
        this.actionRequired = actionRequired;
        this.actorType = actorType;
        this.possibleStatuses = new LinkedList<OrderStatusEnum>();
        for (OrderStatusEnum os : ps) {
            this.possibleStatuses.add(os);
        }
    }

    /**
     * Determines the next required action for an order based on the current 
     * status of the order. This method contains the logic for the order
     * processing sequence. 
     * @param order The OrderEntity for which the next action is being determined.
     * @return The OrderAction representing the action that must be taken next.
     */
    public static OrderAction nextAction(OrderEntity order) {

        switch (getCurrentStatus(order)) {

            case ORDER_RECEIVED:
                return new OrderAction("Start processing "
                        + "payment", ACCOUNTANT, OrderStatusEnum.PAYMENT_PROCESSING);

            case PAYMENT_PROCESSING:
                return new OrderAction("Confirm payment "
                        + "success", ACCOUNTANT, OrderStatusEnum.PAYMENT_RECEIVED, OrderStatusEnum.PAYMENT_FAILED);

            case PAYMENT_FAILED:
                return new OrderAction("None", "None");
            
            case PAYMENT_RECEIVED:
                return new OrderAction("Start shipment "
                        + "processing", WAREHOUSE, OrderStatusEnum.SHIPMENT_PROCESSING);

            case SHIPMENT_PROCESSING:
                return new OrderAction("Confirm shipment "
                        + "sent", WAREHOUSE, OrderStatusEnum.SHIPPED, OrderStatusEnum.OUT_OF_STOCK);

            case OUT_OF_STOCK:
                return new OrderAction("Start processing refund",
                        ACCOUNTANT, OrderStatusEnum.REFUND_PROCESSING);
            
            case REFUND_PROCESSING:
                return new OrderAction("Confirm refund "
                        + "completed", ACCOUNTANT, OrderStatusEnum.REFUND_COMPLETED);

            case REFUND_COMPLETED:
                return new OrderAction("None", "None");
            
            case SHIPPED:
                return new OrderAction("If customer requests a return, decide to approve or reject it", MANAGER, OrderStatusEnum.RETURN_APPROVED, OrderStatusEnum.ORDER_CLOSED);
            
            case RETURN_APPROVED:
                return new OrderAction("Confirm that return was"
                        + " received", WAREHOUSE, OrderStatusEnum.RETURN_RECEIVED);


            case RETURN_RECEIVED:
                return new OrderAction("Start processing refund",
                        ACCOUNTANT, OrderStatusEnum.REFUND_PROCESSING);

            case ORDER_CLOSED:
                return new OrderAction("None", "None");
            
            default:
                return null;
        }
    }

    /**
     * Determines the current status of an order by looking at the most recent
     * entry in its status history. 
     * @param order
     * @return 
     */
    private static OrderStatusEnum getCurrentStatus(OrderEntity order) {
        List<OrderStatusEntity> history = order.getStatusHistory();
        //size must be at least 1;
        return history.get(history.size() - 1).getStatus();
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

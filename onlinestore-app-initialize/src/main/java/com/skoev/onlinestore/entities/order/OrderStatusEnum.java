package com.skoev.onlinestore.entities.order;

/**
 * This enum represents the processing status of an order in the online store. 
 * When an order is received, it goes through a processing sequence and its 
 * status is updated by an employee at each step. This sequence is not linear 
 * since the path an order takes depends on various factors. The possible 
 * actions that an employee of a given type (ACCOUNTANT, MANAGER, 
 * or WAREHOUSE_WORKER)
 * can take at each step are determined by 
 * {@link OrderAction}. 
 * 
 * <br/><br/>
 * 
 * When an order is received by the online store, the first step is to process
 * the payment (the credit card information that the customer provided with the
 * order). This is done by an employee of type ACCOUNTANT (in a real application, 
 * this will likely be done automatically by a payment gateway but here the 
 * scenario is simplified). If the payment fails, the order processing will stop. 
 * 
 * <br/><br/>
 *   
 * If the payment succeeds, the next processing step will be performed by an 
 * employee of type WAREHOUSE_WORKER. If the product is not available, the 
 * payment will be refunded by employee of type ACCOUNTANT and the order
 * processing will stop. Note that this is an exceptional condition and normally
 * should not occur since the product availability is verified by the application
 * when the customer is placing an order. 
 * 
 * <br/><br/>
 * 
 * If the product is available, it is shipped to the customer by employee of
 * type WAREHOUSE_WORKER. 
 * 
 * <br/><br/>
 * 
 * If the customer is not satisfied with the product and wants to return it, 
 * the customer must contact the online store and speak with employee of type
 * MANGER. This employee can authorize a return. If a return is authorized and
 * the product is received back at the warehouse (as verified by WAREHOUSE_WORKER), 
 * the payment is refunded to the customer (by ACCOUNTANT). 
 * 
 * <br/><br/>
 * If the customer doesn't ask for a return or if MANAGER declines the return, 
 * the order is automatically closed 20 days after the day it was placed. No 
 * further actions can be taken on a closed order. 
 * 
 * <br/><br/>
 * 
 * Note that {@link #PAYMENT_PROCESSING}, {@link #SHIPMENT_PROCESSING}, and
 * {@link #REFUND_PROCESSING} are intermediate statuses which prevent concurrent 
 * update issues. For example, if the status were updated from 
 * {@link #PAYMENT_RECEIVED} directly to {@link #SHIPPED}, two employees
 * could unknowingly ship the same product at the same time because none of them
 * knows the other is doing it. The {@link #SHIPMENT_PROCESSING} status prevents 
 * this if used properly; basically, it acts like a pessimistic database lock. 
 * Prior to shipping an order, an employee must try to update its status to 
 * {@link #SHIPMENT_PROCESSING}. 
 * If the order already is in that status, that employee knows that someone else
 * is already working on the order and will back off. 
 * 
 * @see OrderAction
 * @see OrderStatusEntity
 */
public enum OrderStatusEnum {
    /**
     * Order has been received by the online store and processing will start soon. 
     */
    ORDER_RECEIVED,
    /**
     * Payment processing has begun but may not complete yet.
     */
    PAYMENT_PROCESSING,
    /**
     * Payment processing has completed successfully;  
     */
    PAYMENT_RECEIVED,
    /**
     * Payment processing has completed unsuccessfully, for example due to 
     * wrong credit card number.
     */
    PAYMENT_FAILED,
    /**
     * Shipment processing has begun but may not be complete yet.  
     */
    SHIPMENT_PROCESSING,
    /**
     * Shipment processing has completed successfully. 
     */
    SHIPPED,
    /**
     * Shipment processing has completed unsuccessfully -- it was discovered
     * that the product is actually not available. 
     */
    OUT_OF_STOCK,
    /**
     * The customer wants to return the product, and that return has been 
     * approved by an authorized employee. 
     */
    RETURN_APPROVED, 
    /**
     * Return has been approved by an authorized employee, and the product 
     * has been received back at the warehouse. 
     */
    RETURN_RECEIVED,    
    /**
     * Processing of a refund to the customer has begun but may not be complete yet.
     */
    REFUND_PROCESSING,
    /**
     * Processing of a refund to the customer has been completed successfully. 
     */
    REFUND_COMPLETED,
    /**
     * The order has been closed and no further changes to its status can be made. 
     */
    ORDER_CLOSED;        
}

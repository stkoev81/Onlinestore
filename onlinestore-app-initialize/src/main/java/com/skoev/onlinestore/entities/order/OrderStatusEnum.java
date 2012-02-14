/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.entities.order;

/**
 *
 * @author stephan
 */
public enum OrderStatusEnum {
    ORDER_RECEIVED,
    PAYMENT_PROCESSING,
    PAYMENT_RECEIVED,
    PAYMENT_FAILED,
    SHIPMENT_PROCESSING,
    SHIPPED,
    OUT_OF_STOCK,
    RETURN_APPROVED, 
    RETURN_RECEIVED,    
    REFUND_PROCESSING,
    REFUND_COMPLETED,
    ORDER_CLOSED;        
}

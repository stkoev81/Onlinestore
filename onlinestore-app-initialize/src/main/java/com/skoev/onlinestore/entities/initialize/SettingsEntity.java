package com.skoev.onlinestore.entities.initialize;

import javax.persistence.*;
import java.io.Serializable;

/**
 * This entity contains settings for sending email notifications and alerts by 
 * the web application.  
 */
@Entity @Table(name="SETTINGS")
public class SettingsEntity implements Serializable{
    /**
     * There is only one instance of this entity stored in the database, and the 
     * id is set to 1. 
     */
    @Id
    private Integer id; 
    /**
     * This flag is polled by the application to see if email notifications 
     * about an order should be sent. These notification are sent to the customer
     * placing the order when the order is marked as ORDER_RECEIVED, SHIPPED, 
     * PAYMENT_FAILED, or OUT_OF_STOCK (see {@link com.skoev.onlinestore.entities.order.OrderStatusEnum})
     *
     */
    private boolean  notificationsOn; 
    /**
     * This flag is polled by the application to see if alerts 
     * should be sent. These alerts are sent to the alertEmailAddress when a 
     * new order is submitted, when a new product is created, or when an 
     * existing product is modified. 
     */
    private boolean alertsOn;     
    /**
     * Email address for sending alerts.
     */
    private String alertEmailAddress;

    public String getAlertEmailAddress() {
        return alertEmailAddress;
    }

    public boolean isAlertsOn() {
        return alertsOn;
    }

    public Integer getId() {
        return id;
    }

    public boolean isNotificationsOn() {
        return notificationsOn;
    }

    public void setAlertEmailAddress(String alertEmailAddress) {
        this.alertEmailAddress = alertEmailAddress;
    }

    public void setAlertsOn(boolean alertsOn) {
        this.alertsOn = alertsOn;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setNotificationsOn(boolean notificationsOn) {
        this.notificationsOn = notificationsOn;
    }
    
}

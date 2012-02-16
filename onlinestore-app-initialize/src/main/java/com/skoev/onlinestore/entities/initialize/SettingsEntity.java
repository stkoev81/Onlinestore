/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.entities.initialize;


import javax.persistence.*;
import java.io.Serializable;

/**
 *
 * @author stephan
 */
@Entity @Table(name="SETTINGS")
public class SettingsEntity implements Serializable{
    
    @Id
    private Integer id; 
    
    private boolean  notificationsOn; 
    private boolean alertsOn;     
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

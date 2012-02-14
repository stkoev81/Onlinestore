/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.entities.user;
import com.skoev.onlinestore.entities.order.OrderEntity; 

import java.io.Serializable;
import org.apache.commons.codec.digest.DigestUtils;
import java.util.Date;
import java.util.*; 
import javax.persistence.*; 
/**
 *
 * @author stephan
 */
@Entity @Table(name="USERS")
public class UserEntity implements Serializable {    
    @Id     
    @Column(name="USERNAME")
    private String username;
    @Column(name="PASSWD")
    private String passwd; 
    @Temporal(TemporalType.TIMESTAMP)
    private Date acctCreationDate;      
    private String activationString;
    private Boolean activated = false;
    private Boolean disabled = false; 

    // unidirectional
    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name="USERS_GROUPS", joinColumns = @JoinColumn(name="USERNAME"), 
            inverseJoinColumns = @JoinColumn(name="GROUPNAME"))
    private List<GroupEntity> groupMemberships;
    
    //bidirectional
    @OneToMany(fetch=FetchType.EAGER, mappedBy="customer")
    private List<OrderEntity> orderHistory;
       
    //unidirectional
    @OneToOne(fetch=FetchType.EAGER, cascade={CascadeType.PERSIST
            , CascadeType.MERGE}) 
    @JoinColumn
    private UserInfoEntity ui = new UserInfoEntity();
    
    
    {
        ui.setHasUser(true);
    }
    
   
    public void setPasswd(String passwd) {
        if (passwd!=null)
            this.passwd = DigestUtils.sha256Hex(passwd);
        else
            this.passwd = passwd;
    }

    public Date getAcctCreationDate() {
        return acctCreationDate;
    }

    public Boolean getActivated() {
        return activated;
    }

    public String getActivationString() {
        return activationString;
    }

    public List<GroupEntity> getGroupMemberships() {
        return groupMemberships;
    }

    public List<OrderEntity> getOrderHistory() {
        return orderHistory;
    }

    public String getPasswd() {
        return passwd;
    }

    public UserInfoEntity getUi() {
        return ui;
    }

    public String getUsername() {
        return username;
    }

    public void setAcctCreationDate(Date acctCreationDate) {
        this.acctCreationDate = acctCreationDate;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    public void setActivationString(String activationString) {
        this.activationString = activationString;
    }

    public void setGroupMemberships(List<GroupEntity> groupMemberships) {
        this.groupMemberships = groupMemberships;
    }

    public void setOrderHistory(List<OrderEntity> orderHistory) {
        this.orderHistory = orderHistory;
    }

    public void setUi(UserInfoEntity ui) {
        this.ui = ui;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

   
    

}

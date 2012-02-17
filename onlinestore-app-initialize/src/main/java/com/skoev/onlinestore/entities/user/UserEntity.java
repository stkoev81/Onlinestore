package com.skoev.onlinestore.entities.user;

import com.skoev.onlinestore.entities.order.OrderEntity;
import java.io.Serializable;
import org.apache.commons.codec.digest.DigestUtils;
import java.util.*;
import javax.persistence.*;

/**
 * This class represents a registered user in the online store.   
 * For more information on user login credentials and access roles, see 
 * {@link GroupEntity}. 
 * 
 * @see UserInfoEntity
 * 
 */
@Entity
@Table(name = "USERS")
public class UserEntity implements Serializable {
    /**
     * The username is the unique ID for this entity.
     */
    @Id
    @Column(name = "USERNAME")
    private String username;
    /**
     * The password field is hashed by its setter using a SHA256Hex algorithm; 
     * this ensures that the user's password remains secret even if someone has
     * access to the database.
     */
    @Column(name = "PASSWD")    
    private String passwd;
    /**
     * The date when the account was created.
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date acctCreationDate;
    /**
     * A randomly generated activation string that will be sent to the user in 
     * an email once the account is created; the user must provide this activation
     * string in order to activate his account. 
     */
    private String activationString;
    /**
     * Flag indicating if this account has been activated.
     */
    private Boolean activated = false;
    /**
     * Flag indicating if this account has been disabled, in which case the user
     * should not be able to log in. The reason for this is that an administrator may 
     * need to temporarily block access to some account without deleting it. 
     */
    private Boolean disabled = false;
    // unidirectional
    /**
     * Groups of which this user is a a member (see {@link GroupEntity})
     */ 
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "USERS_GROUPS", joinColumns =
    @JoinColumn(name = "USERNAME"),
    inverseJoinColumns =
    @JoinColumn(name = "GROUPNAME"))
    private List<GroupEntity> groupMemberships;
    //bidirectional
    /**
     * The orders which this user has placed previously
     */
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "customer")
    private List<OrderEntity> orderHistory;
    //unidirectional
    /**
     * The contact and payment information that this user has saved in his account.
     */
    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn
    private UserInfoEntity ui = new UserInfoEntity();

    {
        ui.setHasUser(true);
    }

    public void setPasswd(String passwd) {
        if (passwd != null) {
            this.passwd = DigestUtils.sha256Hex(passwd);
        } else {
            this.passwd = passwd;
        }
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

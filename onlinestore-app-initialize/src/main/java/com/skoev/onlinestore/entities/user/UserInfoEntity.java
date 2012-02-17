package com.skoev.onlinestore.entities.user;
import com.skoev.onlinestore.entities.order.OrderEntity; 

import java.io.Serializable;
import org.apache.commons.codec.digest.DigestUtils;
import java.util.Date;
import java.util.*; 
import javax.persistence.*; 
/**
 * Class representing a user's contact and payment information. An instance of 
 * this class can belong to a registered user (see {@link UserEntity}), but it
 * doesn't have to. A UserInfoEntity object can exist without an enclosing 
 * UserEntity. This allows unregistered users to place orders. Also, it allows
 * orders placed by registered users to continue to be associated with the
 * user's information provided at the time of ordering (for example, if a 
 * registered user
 * changes his address soon after ordering, the order should be shipped to the
 * address provided at the time of ordering and not to the new one).  
 * 
 */
@Entity @Table(name="USERINFO")
public class UserInfoEntity implements Serializable {    
    /**
     * Autogenerated ID value
     */
    @GeneratedValue
    @Id
    private Long id;
    private String email; 
    private String firstName; 
    private String lastName;
    private String cardType; 
    private String cardNumber; 
    @Temporal(TemporalType.DATE)
    private Date cardExpirationDate;
    /**
     * If true, the billing address is assumed to be same as the shipping address.
     */
    private boolean sameAsShipping = false; 
    
    
    //unidirectional
    @OneToOne(cascade = CascadeType.ALL) @JoinColumn
    private AddressEntity shippingAddress = new AddressEntity();
    
    //unidirectional
    @OneToOne(cascade = CascadeType.ALL) @JoinColumn
    private AddressEntity billingAddress = new AddressEntity(); 
    
    /**
     * Flag indicating if this UserInfoEntity belongs to a UserEntity; also see
     * {@link #hasOrder}
     */
    private boolean hasUser = false; 
    
    /**
     * Flag indicating if this UserInfoEntity belongs to an OrderEntity. 
     * UserInfoEntity objects that do not belong to any OrderEntity or UserEntity
     * are periodically deleted from the database. Such orphaned objects could 
     * be created for example if a customer who has not placed any orders 
     * deletes his account. Also see {@link #hasUser}
     */
    private boolean hasOrder = false;

    public UserInfoEntity() {
    }
    
    /**
     * Creates a deep copy of the argument 
     * @param ui 
     */
    public UserInfoEntity(UserInfoEntity ui) {
        this.email = ui.email;
        this.firstName = ui.firstName;
        this.lastName = ui.lastName;
        this.cardType = ui.cardType;
        this.cardNumber = ui.cardNumber;
        this.cardExpirationDate = ui.cardExpirationDate;
        this.shippingAddress = new AddressEntity(ui.shippingAddress);
        this.billingAddress = new AddressEntity(ui.billingAddress); 
        this.sameAsShipping = ui.sameAsShipping; 
    }

    public AddressEntity getBillingAddress() {     
        
        return billingAddress;
    }

    public Date getCardExpirationDate() {
        return cardExpirationDate;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardType() {
        return cardType;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public Long getId() {
        return id;
    }

    public String getLastName() {
        return lastName;
    }


    public AddressEntity getShippingAddress() {
        return shippingAddress;
    }



    public void setBillingAddress(AddressEntity billingAddress) {
        this.billingAddress = billingAddress;
    }

    public void setCardExpirationDate(Date cardExpirationDate) {
        this.cardExpirationDate = cardExpirationDate;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }



    public void setShippingAddress(AddressEntity shippingAddress) {
        this.shippingAddress = shippingAddress;
    }


    public boolean isSameAsShipping() {
        return sameAsShipping;
    }

    public void setSameAsShipping(boolean sameAsShipping) {
        this.sameAsShipping = sameAsShipping;
    }
    
    /**
     * Returns a string representation of the credit card expiration month
     * @return 
     */
    public String getCardMonth(){
        Calendar cal = Calendar.getInstance();
         if (cardExpirationDate!=null){
            cal.setTime(cardExpirationDate);
            return Integer.toString(cal.get(Calendar.MONTH)+ 1);
         } 
         else
              return null; 
            
        
   
    }
    /**
     * Returns a string representation of the credit card expiration year
     * @return 
     */
    public String getCardYear(){
        Calendar cal = Calendar.getInstance();
     if (cardExpirationDate!=null){
         cal.setTime(cardExpirationDate);
         return Integer.toString(cal.get(Calendar.YEAR)); 
     }
     else
         return null; 
    }

    public boolean isHasOrder() {
        return hasOrder;
    }

    public boolean isHasUser() {
        return hasUser;
    }

    public void setHasOrder(boolean hasOrder) {
        this.hasOrder = hasOrder;
    }

    public void setHasUser(boolean hasUser) {
        this.hasUser = hasUser;
    }

   

    
    
    
    
   
}

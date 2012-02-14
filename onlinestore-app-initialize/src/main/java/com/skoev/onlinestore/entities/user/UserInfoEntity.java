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
@Entity @Table(name="USERINFO")
public class UserInfoEntity implements Serializable {    
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
    private boolean sameAsShipping = false; 
    
    
    //unidirectional
    @OneToOne(cascade = CascadeType.ALL) @JoinColumn
    private AddressEntity shippingAddress = new AddressEntity();
    
    //unidirectional
    @OneToOne(cascade = CascadeType.ALL) @JoinColumn
    private AddressEntity billingAddress = new AddressEntity(); 
    
    private boolean hasUser = false; 
    private boolean hasOrder = false;

    public UserInfoEntity() {
    }

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
    
    public String getCardMonth(){
        Calendar cal = Calendar.getInstance();
         if (cardExpirationDate!=null){
            cal.setTime(cardExpirationDate);
            return Integer.toString(cal.get(Calendar.MONTH)+ 1);
         } 
         else
              return null; 
            
        
   
    }
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

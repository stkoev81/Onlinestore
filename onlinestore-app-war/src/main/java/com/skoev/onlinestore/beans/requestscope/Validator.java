package com.skoev.onlinestore.beans.requestscope;

import java.util.*;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext; 
import javax.faces.component.UIComponent; 
import javax.faces.validator.ValidatorException; 
import javax.faces.application.FacesMessage; 
import javax.faces.component.UIInput; 
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Contains methods to validate user input in various JSF pages. This is a 
 * request scoped CDI managed bean.
 * All methods throw a ValidatorException with a 
 * customized message if something is wrong with the input. 
 * 
 */
@Named
@RequestScoped
public class Validator {

    @Inject private AcctCreation acctCreation;
    
    public Validator() {
    }    
    
    /**
     * Checks if a username is already taken.  This method internally calls  
     * {@link AccountCreation#usernameAvailable(java.lang.String) }
     * @param context
     * @param component
     * @param value 
     */
    public void usernameAvailable(FacesContext context, UIComponent component
            , Object value){
    String username = (String) value; 
    // Here is an alternative method to get a managed bean from another managed
    // bean (instead of using CDI injection): 
    //ELContext elContext = context.getELContext();
    //AccountCreation acct = (AccountCreation) elContext.getELResolver()
    //      .getValue(elContext,null,"acct");
    
    if (!acctCreation.usernameAvailable(username)){
        FacesMessage message = new FacesMessage("Error! This username is "
                + "already taken.");  
        message.setSeverity(FacesMessage.SEVERITY_ERROR);      
        throw new ValidatorException(message); }
    }          
    
    /**
     * Checks if the passwords entered match when passwords are entered (in the
     * account creation use case passwords are required, but in the the account
     * modification use case they are not). 
     * 
     * @param context
     * @param component
     * @param value 
     */
    public void passwordValidate(FacesContext context, UIComponent component
            , Object value){
        UIInput passwordField = (UIInput) component.findComponent("password");           
        String password = (String) passwordField.getLocalValue();
        UIInput passwordReenterField = (UIInput) component.findComponent(
                "password2");           
        String passwordReenter = (String) passwordReenterField.getLocalValue(); 
        
        boolean fail1 = passwordField.isValid() && password != null 
                && !password.equals(passwordReenter); 
        boolean fail2 = passwordReenterField.isValid() && passwordReenter != null 
                && !passwordReenter.equals(password); 
        
        if (fail1 || fail2){
            FacesMessage message = new FacesMessage("Error! Passwords don't "
                    + "match.");  
            message.setSeverity(FacesMessage.SEVERITY_ERROR);      
            throw new ValidatorException(message); }       
        
    }
    /**
     * Checks if this is valid syntax for an email address. 
     * @param context
     * @param component
     * @param value 
     */
    public void emailValidate(FacesContext context, UIComponent component
            , Object value){
        String email = (String) value; 
        if(!email.matches("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$")){
            FacesMessage message = new FacesMessage("Error! Invalid e-mail "
                    + "format");  
            message.setSeverity(FacesMessage.SEVERITY_ERROR);      
            throw new ValidatorException(message); }        
    } 
    
    /**
     * Checks if the credit card number entered is really a number. Since this is 
     * a mock application, it doesn't check if it follows the correct credit 
     * card number format, but that would be easy to add (e.g. Luhn algorithm). 
     * @param context
     * @param component
     * @param value 
     */
    public void numberValidate(FacesContext context, UIComponent component
            , Object value){
      String number = (String) value; 
            if (number!=null && !number.matches("[0-9]+")){
                FacesMessage message = new FacesMessage("Error! The value "
                        + "entered as credit card number is not a number.");  
                message.setSeverity(FacesMessage.SEVERITY_ERROR);      
                throw new ValidatorException(message); }        
    }
    
    /**
     * Checks if the credit card expiration date entered has already passed
     * 
     * @param context
     * @param component
     * @param value 
     */
    public void expirationValidate(FacesContext context, UIComponent component
            , Object value){
        UIInput yearField = (UIInput) component.findComponent("cardYear");           
        String yearString = (String) yearField.getLocalValue(); 
        UIInput monthField = (UIInput) component.findComponent("cardMonth");           
        String monthString = (String) monthField.getLocalValue(); 
        UIInput numberField = (UIInput) component.findComponent("cardNumber");           
        String numberString =(String) numberField.getLocalValue(); 
                        
        int year = Integer.parseInt(yearString); 
        int month = Integer.parseInt(monthString); 
        Calendar cardExp = Calendar.getInstance(); 
        cardExp.clear();
        cardExp.set(Calendar.YEAR, year);
        cardExp.set(Calendar.MONTH, month - 1);
        cardExp.set(Calendar.DATE, cardExp.getActualMaximum(Calendar.DATE));
        Calendar now = Calendar.getInstance();
        
        if (numberString!=null && cardExp.before(now)){
            FacesMessage message = new FacesMessage("Error! This credit  "
                + "card has already expired.");  
            message.setSeverity(FacesMessage.SEVERITY_ERROR);      
            throw new ValidatorException(message); 
        }        
    }
    
    /**
     * Checks if a billing address has been provided. This method can be used
     * to validate customer address in the store checkout page. 
     * When checking out, the customer must provide both shipping and billing
     * address; if they are the same, he must provide just shipping address
     * and select the "same as shipping" checkbox for the billing address. 
     * 
     * @param context
     * @param component
     * @param value 
     */
    public void billingAddrValidate(FacesContext context, UIComponent component
            , Object value){
      
        String[] billingAddrArray = {"billingStreet","billingCity",
            "billingState","billingCountry", "billingZIP", "billingPhone"};
        boolean sameAsShipping = (Boolean) ((UIInput) component.findComponent(
                "sameAsShipping")).getLocalValue(); 
        boolean billingProvided = true; 
        for (String s: billingAddrArray){
           if (((UIInput) component.findComponent(
                   "billingPhone")).getLocalValue()==null){
               billingProvided = false; 
           }
        }

        if (!sameAsShipping && !billingProvided){
        FacesMessage message = new FacesMessage("Error! No billing address"
                + " provided. If same as "
                + "shipping, select checkbox. If different, enter below.");  
        message.setSeverity(FacesMessage.SEVERITY_ERROR);      
        throw new ValidatorException(message); 
        }
    }
    
    /**
     * Checks if a string entered as a book ISBN number is really a number
     * @param context
     * @param component
     * @param value 
     */
    public void isbnValidate(FacesContext context, UIComponent component
        , Object value){      
        if (value == null || !value.toString().matches("[0-9]+")){
            FacesMessage message = new FacesMessage("Error! ISBN must be an"
                    + " integer");    
            message.setSeverity(FacesMessage.SEVERITY_ERROR);      
            throw new ValidatorException(message); }        
    }
}

package com.skoev.onlinestore.beans.requestscope;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.skoev.onlinestore.ejb.*;
import com.skoev.onlinestore.entities.user.*; 

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*; 
import javax.servlet.http.HttpServletRequest; 

import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext; 
import javax.faces.event.*;

/**
 * Contains methods and fields relevant to account creation and modification. 
 * 
 */
@Named(value="acctCreation")
@RequestScoped
public class AcctCreation {
    @EJB
    private MailSenderStateless mailSender;
    @EJB
    private EntityAccessorStateless entityAccessor;
    
    
    /**
     * Most of the user information is entered directly in the user entity; some 
     * of the information is entered in local fields such as cardMonth, cardYear
     *cardNumber, carType, new Password because they need additional processing before being set in 
     * the user entity. 
     */
    private UserEntity user;
    
    
    private String cardMonth; 
    private String cardYear; 
    private String cardNumber; 
    private String cardType; 
    private String newPassword; 
     

      /**
     * PostAddToView listener that gets called when an account is about to be 
     * created; creates a new UserEntity
     * @param event 
     */  
       public void newUser(ComponentSystemEvent event){      
        user= new UserEntity();    
       }
             
       /**
        * PostAddToView listener that gets called when an existing account is 
        * about to modified; retrieves an existing user entity with the username; 
        * the username is available because the user is authenticated at this point. 
        * 
        * @param event 
        */
        public void existingUser(ComponentSystemEvent event){
         HttpServletRequest request = (HttpServletRequest) FacesContext
               .getCurrentInstance().getExternalContext().getRequest();
         String username=request.getUserPrincipal().getName();
         
         user=entityAccessor.findEntity(UserEntity.class, username); 
         Calendar cal = Calendar.getInstance();
         Date date = user.getUi().getCardExpirationDate();
         if (date!=null){
            cal.setTime(date); 
            cardMonth = Integer.toString(cal.get(Calendar.MONTH)+ 1);
            cardYear = Integer.toString(cal.get(Calendar.YEAR));                        
         }
                 
         
         cardNumber=user.getUi().getCardNumber(); 
         cardType=user.getUi().getCardType(); 
         
    }
    
    /**
         * Checks if the username is already taken by searching for it in the 
         * database
         * @param username
         * @return 
         */
    public boolean usernameAvailable(String username){       
       if (entityAccessor.findEntity(UserEntity.class, username) == null ) 
          return true; 
       else
          return false;
   }

    /**
     * Returns the 12 months; will be used in the credit card expiration field.
     * @return 
     */
   public String[] months(){
       String[] months = {"1","2","3","4","5","6","7","8","9","10","11","12"}; 
       return months; 
       
   }
   /**
    * Returns an array of years for the credit card expiration year
    */ 
   public String[] years(){
       String[] years = new String[20]; 
       Calendar today = Calendar.getInstance(); 
       int currentYear = today.get(Calendar.YEAR); 
       for (int i =0; i<years.length; i++){
           years[i]=String.valueOf(currentYear+i-1);
       }       
       return years; 
   
   }
/**
    * Tries to create a new account by setting various fields in the user
    * entity and then persisting it; sends an email if the account creation is 
    * successful. 
    * @return 
    */        
   public String createNewAccount() {
       //set information related to group memberships and account activation
       user.setAcctCreationDate(new Date()); 
       user.setActivated(false);
       user.setActivationString(generateActivationString()); 
       doCommonTasks();       
       try {
            mailSender.sendActivationEmail(user, generateActivationURL()); 
       }
       catch (EmailException ee){
            return "/Errors/EmailError.xhtml";
       }
       entityAccessor.persistEntity(user);       
       return "/AccountCreation/Created.xhtml"; 
       
   }
   /**
    * Tries to update an existing account by setting various fields in the 
    * UserEntity and then persisting it. 
    * @return 
    */
   public String changeExistingAccount(){      
       doCommonTasks(); 
       if (newPassword!=null){
           user.setPasswd(newPassword);
       }
       
       entityAccessor.mergeEntity(user);                  
       return "/InsideAccount/Changed.xhtml";
       
   }
   
   /**
    * Convenience method used by other methods in this class to set credit
    * card information and expiration date and billing address.
    */
   private void doCommonTasks (){
       //set credit card information if it has been provided by the user
       //convert the month and date to java.util.Date object using a 
       //java.util.Calendar object
       
       if(cardNumber!=null){
           Calendar cardExp = Calendar.getInstance();
           cardExp.clear();
           cardExp.set(Calendar.YEAR, Integer.parseInt(cardYear));
           cardExp.set(Calendar.MONTH, Integer.parseInt(cardMonth)-1);
           cardExp.set(Calendar.DATE, cardExp.getActualMaximum(Calendar.DATE)); 
                      
           user.getUi().setCardNumber(cardNumber);
           user.getUi().setCardType(cardType);
           user.getUi().setCardExpirationDate(cardExp.getTime());        
       }
   }
   
   /**
    * Generates a random activation string that is 8 characters long. 
    * @return 
    */
   private String generateActivationString(){
        int base = 32; 
        int length = 8; 
        long randomNumber = (long) (Math.pow(base,length)*Math.random());
        return  Long.toString(randomNumber, base);   
              
   }
   
   /**
    * Creates an activation URL that this user must visit to activate his 
    * account; it is generated by using the server name, context path, the
    * name of the AccountActivation page, and a randomly generated activation 
    * string.
    * @return 
    */
   private String generateActivationURL(){
       HttpServletRequest request = (HttpServletRequest) FacesContext
               .getCurrentInstance().getExternalContext().getRequest(); 
       int portNum = request.getLocalPort();
       String port = (portNum==80)?"":(":" + portNum);
       String activationURL = "http://" + request.getServerName() + port  
               + request.getContextPath() + "/AccountCreation"
               + "/ActivateAccount.xhtml?u=" + user.getUsername() + "&as=" 
               + user.getActivationString();
       return activationURL; 
   }
   

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }


    public String getCardMonth() {
        return cardMonth;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardType() {
        return cardType;
    }

    public String getCardYear() {
        return cardYear;
    }

    public void setCardMonth(String cardMonth) {
        this.cardMonth = cardMonth;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public void setCardYear(String cardYear) {
        this.cardYear = cardYear;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }


 
   
}

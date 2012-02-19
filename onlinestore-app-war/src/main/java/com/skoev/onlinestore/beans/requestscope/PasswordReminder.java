
package com.skoev.onlinestore.beans.requestscope;

import javax.inject.Named;
import java.util.*;
import com.skoev.onlinestore.entities.user.*;
import com.skoev.onlinestore.ejb.*;


import javax.enterprise.context.RequestScoped;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage; 
import javax.faces.context.FacesContext;
/**
 * Contains methods to generate a password reminder and send it in an email 
 * to the user. 
 * 
 */
@Named
@RequestScoped
public class PasswordReminder {
@EJB
    private MailSenderStateless mailSender;
    @EJB
    private EntityAccessorStateless entityAccessor;
    /** Creates a new instance of PasswordReminder */
    public PasswordReminder() {
    }
    private String email;
    
    private FacesContext context = FacesContext.getCurrentInstance(); 
    
    
    /**
     * Looks up user entity, sets a new randomly generated password, sends
     * a reminder email; also, 
     * generates a message for the user depending on the success or failure of 
     * this operation. 
     * @return 
     */
    public String lookupAndSend() {
        if("email@example.com".equals(email)){
            FacesMessage message = new FacesMessage("Error! This is not a real "
                    + "email address. This is"
                    + " a mock email address that is associated with the demo "
                    + "accounts. "); 
            message.setSeverity(FacesMessage.SEVERITY_ERROR); 
            context.addMessage(null, message);
            return null; 
        }
        
        
        String query = "SELECT u FROM UserEntity u WHERE u.ui.email=?1 "
                + "AND u.activated=true";
        List<UserEntity> accounts = entityAccessor.doQuery(UserEntity.class
                , query, email); 
        FacesMessage message; 
        if (accounts==null || accounts.isEmpty()){
            message = new FacesMessage("Error! No accounts were"
                    + " found using this email address");         
            message.setSeverity(FacesMessage.SEVERITY_ERROR);             
        }
        else {
            String password = generatePassword(); 
            
            for (UserEntity a:accounts){
                a.setPasswd(password);
                entityAccessor.mergeEntity(a); 
            }
            
            try {
                mailSender.sendPasswordReminder(accounts,email, password); 
            }
            
            catch (EmailException ee){
                return "/Errors/EmailError.xhtml?faces-redirect=true";
            }
                        
            message = new FacesMessage("Your account was found. "
                    + "You will receive an email with the password.");         
            message.setSeverity(FacesMessage.SEVERITY_INFO); 
            
        } 
        context.addMessage(null,message);
        
        return null; 
    
    }
  
    /**
     * Generates a random password, 8 characters long. 
     * @return 
     */
    private String generatePassword(){
        int base = 32; 
        int length = 8; 
        long randomNumber = (long) (Math.pow(base,length)*Math.random());
        return Long.toString(randomNumber, base); 
    }
    
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    
    
    
}

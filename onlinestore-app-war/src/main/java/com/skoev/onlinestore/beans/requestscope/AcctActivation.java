/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.beans.requestscope;

import com.skoev.onlinestore.ejb.*; 
import com.skoev.onlinestore.entities.user.*; 
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.ejb.EJB;
import java.util.*; 




/**
 *
 * @author stephan
 */
@Named
@RequestScoped
public class AcctActivation {
    @EJB
    private EntityAccessorStateless entityAccessor; 
    
    private String activationString; 
    private String username; 

    /** Creates a new instance of AccountVerification */
    public AcctActivation() {
    }
    
    /**
     * Tries to activate the account by looking at the activationString and 
     * userName, which are set from query string parameters; if successful, an 
     * entry is made in the database that this account is activated and that it 
     * is now in the CUSTOMER group. 
     * @return appropriate message to the user, depending on outcome of account 
     * activation attempt
     */
    public String activate(){
       String failure = "Acount could not be activated. Please make sure"
               + "you correctly copy the entire link from the activation email.";
       String expired = "Account could not be found. Accounts not "
               + "activated within 48 hrs of creation are deleted. Please "
               + "create your account again."; 
       String success = "Account has been activated. You can now log in."; 
       String alreadyActivated = "Account is already activated. You don't need "
               + "to activate it again."; 
       
       if (username == null || activationString == null){
           return failure; 
       }
       
       UserEntity user = entityAccessor.findEntity(UserEntity.class,username); 
       
       if(user==null){
           return expired; 
       }
       
       else if (!user.getActivated() && activationString.equals(user
               .getActivationString())){
           user.setActivated(true); 
           user.setActivationString(null);            
           List<GroupEntity> groups = new LinkedList<GroupEntity>(); 
           groups.add(new GroupEntity("CUSTOMER")); 
           user.setGroupMemberships(groups);
           
           entityAccessor.mergeEntity(user);
           
           
           return success; 
       }
       
       else if (user.getActivated()){
           return alreadyActivated; 
       }
               
       else {
           return failure; 
       
       }
        
    }

    public String getActivationString() {
        return activationString;
    }

    public String getUsername() {
        return username;
    }

    public void setActivationString(String activationString) {
        this.activationString = activationString;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}

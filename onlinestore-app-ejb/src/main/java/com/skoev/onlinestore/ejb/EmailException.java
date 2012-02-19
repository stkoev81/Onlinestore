/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.ejb;
import javax.ejb.ApplicationException; 
/**
 *
 * @author stephan
 */
@ApplicationException(rollback=true)
public class EmailException extends Exception implements UserFriendly{
    @Override
    public String userMessage(){
        return "There was a problem with the email server, and the application"
                + "couldn't send an email."; 
    }
}

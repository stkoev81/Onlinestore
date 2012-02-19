/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.ejb;

/**
 *
 * @author stephan
 */
public class ProductNotAvailableException extends Exception implements 
        UserFriendly{
    
    @Override
    public String userMessage(){
        return "Error! This product is no longer available. "; 
    }
}

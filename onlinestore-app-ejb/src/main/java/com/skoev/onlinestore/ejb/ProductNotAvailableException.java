package com.skoev.onlinestore.ejb;

/**
 * This exception is thrown to report that a product in the online store is 
 * not available to be ordered. 
 * 
 */
public class ProductNotAvailableException extends Exception implements 
        UserFriendly{
    /**
     * Returns a message stating that the there was an error because the product
     * is no longer available. 
     * @return 
     */
    @Override
    public String userMessage(){
        return "Error! This product is no longer available. "; 
    }
}

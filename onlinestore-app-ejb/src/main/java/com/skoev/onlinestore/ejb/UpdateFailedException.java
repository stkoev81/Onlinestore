package com.skoev.onlinestore.ejb;

/**
 * This exception is thrown to report that an update operation to the database
 * failed due to a javax.persistence.OptimisticLockException. Clients catching 
 * this exception should notify the user that his operation failed because the 
 * item he was trying to update was updated concurrently by another user. 
 * 
 */
public class UpdateFailedException extends Exception implements UserFriendly{
   /**
     * Returns a basic explanation of the failure stating that the item could 
     * not be updated as requested. 
     */
    @Override
    public String userMessage(){
        return "Error! Item could not be updated as you requested."          
               + "It seems someone else already updated the item while you were "
                + "viewing it. Refresh the page to see the latest version. ";
    }
}

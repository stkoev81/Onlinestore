package com.skoev.onlinestore.ejb;

/**
 * This interface can be implemented by exceptions to equip them with a 
 * user-friendly message about the failure. This message is meant for 
 * non-technical users of the application, who would not be interested in a 
 * stack trace. The same message will be shared by all instances of the 
 * exception. 
 * 
 */
public interface UserFriendly {
    /**
     *  Returns a message about the cause of the exception to be displayed to 
     *  the user.
     * @return 
     */
    public String userMessage();
}

package com.skoev.onlinestore.beans.sessionscope;

import java.io.Serializable;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.ejb.EJB;
import com.skoev.onlinestore.ejb.*;

/**
 * This session scoped CDI managed bean is simply used to keep a 
 * {@link com.skoev.onlinestore.ejb.ManageOrdersStateful} object. 
 * Request scoped beans 
 * obtain the ManageOrdersStateful object from here; this way, multiple 
 * requests by the 
 * same user will have access to the same order management object. 
 * 
 * @see com.skoev.onlinestore.ejb.ManageOrdersStateful
 * @see com.skoev.onlinestore.beans.requestscope.ViewStoreOrders
 */
@Named
@SessionScoped
public class ManageOrdersSession implements Serializable {
    @EJB
    ManageOrdersStateful manageOrdersStateful;

    public ManageOrdersStateful getManageOrdersStateful() {
        return manageOrdersStateful;
    }
}

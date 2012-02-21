package com.skoev.onlinestore.beans.sessionscope;

import java.io.Serializable;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.ejb.EJB;
import com.skoev.onlinestore.ejb.*;

/**
 * This session scoped CDI managed bean is simply used to keep a 
 * {@link com.skoev.onlinestore.ejb.ManageProductsStateful} object. 
 * Request scoped beans 
 * obtain the ManageProductsStateful object from here; this way, multiple 
 * requests by the 
 * same user will have access to the same product management object. 
 * 
 * @see com.skoev.onlinestore.ejb.ManageProductsStateful
 * @see com.skoev.onlinestore.beans.requestscope.ViewStoreProducts
 */
@Named
@SessionScoped
public class ManageProductsSession implements Serializable {
   @EJB ManageProductsStateful manageProductsStateful;
    
    public ManageProductsStateful getManageProductsStateful() {
        return manageProductsStateful;
    }
}

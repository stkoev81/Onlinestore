package com.skoev.onlinestore.ejb;

import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.ejb.Startup; 
import javax.annotation.PostConstruct; 
import javax.persistence.*;
import java.util.*; 
import com.skoev.onlinestore.entities.product.*; 

/**
 * This singleton session EJB is responsible for initializing the application. 
 * As explained in {@link ProductAvailabilityEntity}, when a product is placed
 * in cart, the numberAvailable is decremented. If the application crashes or 
 * the server is restarted, the carts won't be emptied properly, i.e. the 
 * numberAvailable won't be incremented back up. This singleton takes care of 
 * that problem. The {@link #initialize} method runs when the application is
 * first started. It looks at the numberInCarts field of 
 * {@link ProductAvailabilityEntity} for each product and increments the 
 * numberAvailable field by that number. 
 * 
 * 
 */
@Singleton
@LocalBean
@Startup
public class InitializerSingleton {
    
    /**
     * Injected entity manager
     */
    @PersistenceContext(unitName = "EJB_PU") 
    private EntityManager em; 

    /**
     * Initializes the application by making sure that any shopping carts 
     * that weren't empty when the application shut down are returned to the 
     * store's inventory.
     */
    @PostConstruct
    public void initialize() {        
        String queryString = "SELECT p FROM ProductAvailabilityEntity p";
        TypedQuery<ProductAvailabilityEntity> query = em.createQuery(
                queryString, ProductAvailabilityEntity.class);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        // get a list of all products (from a query) and lock it pessimistically
        List<ProductAvailabilityEntity> numbersList = query.getResultList(); 
        // iterate over this list and "return" any products that were in carts
        for(ProductAvailabilityEntity numbers: numbersList){
            numbers.setNumberInCarts(0);
            numbers.calculateNumberAvailable();
        }
    }
}

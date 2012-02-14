/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.ejb;

import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.ejb.Startup; 
import javax.annotation.PostConstruct; 
import javax.persistence.*;
import java.util.*; 
import com.skoev.onlinestore.entities.product.*; 

/**
 *
 * @author stephan
 */
@Singleton
@LocalBean
@Startup
public class InitializerSingleton {
    
    @PersistenceContext(unitName = "EJB_PU") 
    private EntityManager em; 

    @PostConstruct
    public void initialize() {
        // get a list of all products (from a query). Lock it.
        // iterate over this list; for each one, set the number in cart to be equal to 0 and call calculate number available
        // TODO: handle exception somehow (who will see the error? will it be just in server log? )
        // TODO: add some sort of method call to clear all sessions 
        String queryString = "SELECT p FROM ProductAvailabilityEntity p";
        TypedQuery<ProductAvailabilityEntity> query = em.createQuery(queryString, ProductAvailabilityEntity.class);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        List<ProductAvailabilityEntity> numbersList = query.getResultList(); 
        for(ProductAvailabilityEntity numbers: numbersList){
            numbers.setNumberInCarts(0);
            numbers.calculateNumberAvailable();
        }
    }

 
    
}

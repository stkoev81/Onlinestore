/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.ejb;


import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.persistence.*;
import com.skoev.onlinestore.entities.user.*; 
import com.skoev.onlinestore.entities.order.*; 
import java.util.*; 
/**
 *
 * @author stephan
 */
@Stateless
@LocalBean
public class EntityAccessorStateless {
    //inject entity manager
    //instantiate a new entity
    //set its fields
    
    @PersistenceContext(unitName = "EJB_PU") 
    private EntityManager em; 

    /**
     * Creates a new entity in the database. 
     * 
     * @param user  an UserEntity object which has all the fields set to create a new user account
     * @return      true if the operation succeed, otherwise false
     */
    public void persistEntity(Object entity){    
        em.persist(entity);
    } 
    public void refreshEntity(Object entity){
        em.refresh(entity);
    }

    public void mergeEntity(Object entity){
        em.merge(entity);
    }

    public <T> T getEntity(Class<T> entityClass, Object primaryKey){
        T entity = em.find(entityClass, primaryKey); 
        return entity; 
    }

    public <T> void deleteEntity(Class<T> entityClass, Object primaryKey){
        T entity = em.find(entityClass, primaryKey); 
        if (entity!=null)
            em.remove(entity);                
    }

    public void removeEntity(Object entity){
        em.remove(em.merge(entity));
    }
    
    public <T> List<T> doQuery(Class<T> resultsClass, String queryString, Object...params){
        TypedQuery<T> query = em.createQuery(queryString,resultsClass);
        for( int i=0;i<params.length; i++){
            query.setParameter(i+1,params[i]);
        }
        return query.getResultList();
    }
    
    //TODO: add lock
    public void deleteUserAccout(UserEntity user){       
       String query = "SELECT p from OrderEntity p WHERE p.customer=?1";
       List<OrderEntity> orders = doQuery(OrderEntity.class, query, user); 
       for ( OrderEntity o:orders){
           o.setCustomer(null);           
           em.merge(o); 
       } 
       user.getUi().setHasUser(false);
       removeEntity(user); 
    }    
}

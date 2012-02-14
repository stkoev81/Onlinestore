/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.entities.initialize;
import javax.persistence.*; 
import java.util.logging.*;

/**
 *
 * @author stephan
 */
public class EntityAccessor {
    private EntityManagerFactory emf; 
    private EntityManager em; 
    
    public EntityAccessor(String puName){
        emf = Persistence.createEntityManagerFactory(puName);
        em = emf.createEntityManager();
    }
    public void refreshEntity(Object o){
    try {     
          EntityTransaction tx = em.getTransaction(); 
           boolean committed = false; 
            try {                
              tx.begin(); 
              em.refresh(o);
              tx.commit();              
              committed = true; 
              } finally {
                 if (!committed) tx.rollback();
            }
      }
       catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception "
                    + "caught", e);            
            throw new RuntimeException();
        }     
        
    }
    
    public void persistEntity(Object o){
      try {     
          EntityTransaction tx = em.getTransaction(); 
           boolean committed = false; 
            try {                
              tx.begin(); 
              em.persist(o);                  
              tx.commit();              
              committed = true; 
              } finally {
                 if (!committed) tx.rollback();
                 
            }
      }
       catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception "
                    + "caught", e);            
            throw new RuntimeException();
        }     
    }
    
    public <T> T getEntity(Class<T> entityClass, Object primaryKey){
        return em.find(entityClass, primaryKey);
    }
    
    
    public <T> void deleteEntity(Class<T> entityClass, Object primaryKey){
        T entity = em.find(entityClass, primaryKey); 
        if (entity==null)
            return; 
        
        
        try {     
          EntityTransaction tx = em.getTransaction(); 
           boolean committed = false; 
            try {                
              tx.begin(); 
              em.remove(entity);                  
              tx.commit();              
              committed = true; 
              } finally {
                 if (!committed) tx.rollback();
            }
      }
       catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception "
                    + "caught", e);            
            throw new RuntimeException();
        }     
    }
   
   public void removeEntity(Object entity){
                
         try {     
          EntityTransaction tx = em.getTransaction(); 
           boolean committed = false; 
            try {                
              tx.begin(); 
              em.merge(entity);
              em.remove(entity);                  
              tx.commit();              
              committed = true; 
              } finally {
                 if (!committed) tx.rollback();
            }
      }
       catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception "
                    + "caught", e);            
            throw new RuntimeException();
        }            
    }
   
   public void mergeEntity(Object entity){
                
         try {     
          EntityTransaction tx = em.getTransaction(); 
           boolean committed = false; 
            try {                
              tx.begin(); 
              em.merge(entity);
              
              tx.commit();              
              committed = true; 
              } finally {
                 if (!committed) tx.rollback();
            }
      }
       catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception "
                    + "caught", e);            
            throw new RuntimeException();
        }            
    }
   
   
   
    
    
}

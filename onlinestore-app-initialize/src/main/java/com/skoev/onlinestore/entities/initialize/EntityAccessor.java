package com.skoev.onlinestore.entities.initialize;
import javax.persistence.*; 
import java.util.logging.*;
import java.util.List;

/**
 * Class responsible for persisting, retrieving, and deleting entities from 
 * the database. Other classes in the package delegate to this class when they 
 * need to access the database. Exceptions thrown inside the methods of 
 * this class are caught and logged, and then rethrown as runtime exceptions. 
 * The log output depends on the settings in the 
 * logging.properties file in the JRE/lib directory; by default, the logging is 
 * directed to standard out. <br/><br/>
 * The methods in this class are not thread-safe because they use the same 
 * entity manager instance. The idea is that this class is used only to 
 * initialize and administer the database in simple script-like programs (
 * {@link TablesInitializer } and {@link com.skoev.onlinestore.admin.AdminDatabase}).
 * 
 * 
 */
public class EntityAccessor {
    /**
     * Entity manager factory instance
     */
    private EntityManagerFactory emf; 
    /**
     * Entity manager instance
     */
    private EntityManager em; 
    /**
     * Logger object used for logging exceptions. 
     */
    private static final Logger logger = Logger.getLogger(EntityAccessor.class.getName()); 
    
    /**
     * Constructs an instance of this class. 
     * @param puName Name of the persistence unit for this entity accessor. The 
     * pu is defined in a persistence.xml file that should be provided in the 
     * directory src/main/resources/META-INF  
     */
    public EntityAccessor(String puName){
        emf = Persistence.createEntityManagerFactory(puName);
        em = emf.createEntityManager();        
    }
    /**
     * Attempts to persist an entity to the database; attempts to roll back 
     * if there are problems. 
     * @param o Entity to be persisted
     */
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
            logger.log(Level.SEVERE, "exception caught", e); 
            throw new RuntimeException(e); 
        }     
    }
    
    /**
     * Attempts to refresh an entity from the database; attempts to roll back if 
     * there are problems. 
     * @param o Entity to be refreshed
     */
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
            logger.log(Level.SEVERE, "exception caught", e); 
            throw new RuntimeException(e); 
        }     

    }
    
    /**
     * Looks up an entity in the database
     * @param entityClass The class of the entity
     * @param primaryKey The primary key of the entity
     * @return the entity that was found; null if nothing found
     */
    public <T> T findEntity(Class<T> entityClass, Object primaryKey){
        return em.find(entityClass, primaryKey);
    }
    
    /**
     * Looks up and entity in the database and then attempts to delete it; 
     * attempts to roll back if there are problems
     *
     * @param entityClass
     * @param primaryKey 
     */
    public <T> void findAndDeleteEntity(Class<T> entityClass, Object primaryKey){
        T entity = em.find(entityClass, primaryKey); 
        if (entity==null){
            return; 
        }
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
            logger.log(Level.SEVERE, "exception caught", e); 
            throw new RuntimeException(e); 
        }   
    }
   
    /**
     * Attempts to delete an entity from the database; attempts to roll back if 
     * there are problems. 
     * @param entity 
     */
    public void deleteEntity(Object entity){
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
            logger.log(Level.SEVERE, "exception caught", e);  
            throw new RuntimeException(e); 
        }     
    }

    /**
     * Attempts to merge an entity to the database; attempts to roll back if 
     * there are problems. 
     * @param entity 
     */
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
            logger.log(Level.SEVERE, "exception caught", e);   
            throw new RuntimeException(e); 
        }  
    }
    
    /**
     * Executes a query
     * @param resultsClass The class of the entity resulting from the query
     * @param queryString The JPQL query 
     * @param params JPQL query parameters
     * @return The results from the query
     */
    public <T> List<T> doQuery(Class<T> resultsClass, String queryString, Object...params){
        TypedQuery<T> query = em.createQuery(queryString,resultsClass);
        for( int i=0;i<params.length; i++){
            query.setParameter(i+1,params[i]);
        }
        // no transaction needed for a query unless it has a lock set.
        return query.getResultList();
    }
    
    
    
    
    
}

package com.skoev.onlinestore.ejb;

import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.persistence.*;
import com.skoev.onlinestore.entities.user.*; 
import com.skoev.onlinestore.entities.order.*; 
import java.util.*; 
/**
 * This stateless session EJB is responsible for persisting, retrieving, and  
 * deleting entities from the database using container managed persistence and
 * container managed transactions. Its role is somewhat similar to that of 
 * {@link com.skoev.onlinestore.entities.initialize.EntityAccessor}; however
 * that class is a POJO and uses resource local transactions and is intended to 
 * run in an SE environment rather than the EJB container. 
 *  
 * <br/><br/>
 * This class is used by managed beans in the web container for simple 
 * persistence tasks (tasks that do not
 * require locking and that are not likely to throw exceptions). It cannot be
 * used for operations that require pessimistic locking because once a 
 * method exits, the entity bean that it returns is detached and the lock is 
 * lost. Also, it should not be used for operations that are likely to throw 
 * exceptions, such as trying to persist an entity that has an optimistic lock, 
 * because the exception is wrapped in an EJBException by the container and 
 * becomes more difficult to handle by the client. 
 * 
 * <br/><br/>
 * More complicated persistence tasks like the ones described above need to be
 * performed by customized EJB's (such as {@link CartStateful},
 * {@link ManageOrdersStateful}, {@link ManageProductsStateful}) instead of 
 * managed beans calling this general purpose EJB.
 * 
 */
@Stateless
@LocalBean
public class EntityAccessorStateless {
    /**
     * Injected EntityManager instance
     */
    @PersistenceContext(unitName = "EJB_PU") 
    private EntityManager em; 
    

    /**
     * Persists and entity in the database
     * @param entity 
     */
    public void persistEntity(Object entity) throws MarkerException{    
        em.persist(entity);
    } 
    
    /**
     * Refreshes and entity from the database
     * @param entity 
     */
    public void refreshEntity(Object entity) throws MarkerException{
        em.refresh(entity);
    }

    /**
     * Merges and entity to the database
     * @param entity 
     */
    public void mergeEntity(Object entity) throws MarkerException {
        em.merge(entity);
    }

    /**
     * Finds an entity in the database by its primary key
     * @param entityClass
     * @param primaryKey
     * @return 
     */
    public <T> T findEntity(Class<T> entityClass, Object primaryKey) throws MarkerException {
        T entity = em.find(entityClass, primaryKey); 
        return entity; 
    }

    /**
     * Finds an entity by its primary key and then deletes it
     * @param entityClass
     * @param primaryKey 
     */
    public <T> void findAndDeleteEntity(Class<T> entityClass
            , Object primaryKey) throws MarkerException {
        T entity = em.find(entityClass, primaryKey); 
        if (entity!=null)
            em.remove(entity);    
    }

    /**
     * Deletes and entity from the database.
     * @param entity 
     */
    public void deleteEntity(Object entity) throws MarkerException {
        em.remove(em.merge(entity));
    }
    
    /**
     * Executes a JPQL query
     * @param resultsClass Class object of the entity resulting from the query
     * @param queryString JPQL query string
     * @param params Query parameters
     * @return 
     */
    public <T> List<T> doQuery(Class<T> resultsClass, String queryString
            , Object...params) throws MarkerException {
        TypedQuery<T> query = em.createQuery(queryString,resultsClass);
        query.setLockMode(LockModeType.NONE);
        for( int i=0;i<params.length; i++){
            query.setParameter(i+1,params[i]);
        }
        return query.getResultList();
    }
    
    /**
     * Deletes a user account, which is represented by a UserEntity. Before 
     * deleting the entity, this method nulls out any references to the
     * entity that exist in orders that the user has placed (otherwise
     * data integrity constraints would be violated)
     * @param user 
     */
    public void deleteUserAccout(UserEntity user) throws MarkerException {       
       String query = "SELECT p from OrderEntity p WHERE p.customer=?1";       
       List<OrderEntity> orders = doQuery(OrderEntity.class, query, user); 
       for ( OrderEntity o:orders){
           o.setCustomer(null);           
           em.merge(o); 
       } 
       user.getUi().setHasUser(false);
       deleteEntity(user); 
    }    
}

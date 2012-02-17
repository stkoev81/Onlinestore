/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.ejb;
import com.skoev.onlinestore.entities.order.*; 
import com.skoev.onlinestore.entities.user.*; 

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import java.util.*; 
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.ejb.Startup; 
import javax.persistence.*; 

/**
 *  Test these when you have enough entries in the database to be testable; 
 */
@Singleton
@LocalBean
@Startup
public class PeriodicCleanUpSingleton {
    
    @PersistenceContext(unitName = "EJB_PU") 
    private EntityManager em; 
    
    private static long MS_IN_DAY = 24*3600*1000; 


    
    
   @Schedule(dayOfMonth="*") 
    public void closeOldOrders(){
        /*-get all the orders such that they are SHIPPED and have been so for 
         * more than 30 days.
         */    
        Date today = new Date(); 
        
        Date twentyDaysAgo = new Date(today.getTime() - 20*MS_IN_DAY); 
        //Date twentyDaysAgo = new Date(today.getTime() - 10); 
        String shippedStatus = OrderStatusEnum.SHIPPED.toString();
                
        String queryString = "SELECT p FROM OrderEntity p WHERE p.currentStatus = ?1 AND p.lastModified < ?2"; 
        TypedQuery query = em.createQuery(queryString, OrderEntity.class);
        query.setParameter(1, shippedStatus);
        query.setParameter(2, twentyDaysAgo);
        
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE); 
        List<OrderEntity> shippedOrders = query.getResultList();
        System.err.println("old orders found: " + shippedOrders.size());
        for (OrderEntity order: shippedOrders){
            closeOrder(order); 
        }
        
        System.err.println("-------closeOldOrders was executed");
    }
    
    
    private void closeOrder(OrderEntity order){
        OrderStatusEntity orderStatusEntity = new OrderStatusEntity(); 
        orderStatusEntity.setStatus(OrderStatusEnum.ORDER_CLOSED); 
        orderStatusEntity.setStatusBegan(new Date());
        orderStatusEntity.setActor("sytem");
        order.getStatusHistory().add(orderStatusEntity);  
        order.setCurrentStatus(orderStatusEntity.getStatus().toString());
        order.setLastModified(orderStatusEntity.getStatusBegan());
        order.setLastActor(orderStatusEntity.getActor());
    }
    
    
    @Schedule(dayOfMonth="*")
    public void deleteUnverifiedAccounts(){
        /* - get all the users that are in the not verified group and have been
         * there for more than 30 days
         * - delete them
         */
        Date today = new Date(); 
        Date thirtyDaysAgo = new Date(today.getTime() - 30*MS_IN_DAY);         
        
        String queryString = "SELECT p FROM UserEntity p WHERE p.activated = ?1 AND p.acctCreationDate < ?2"; 
        
        TypedQuery query = em.createQuery(queryString, UserEntity.class);
        query.setParameter(1, false);
        query.setParameter(2, thirtyDaysAgo);
        
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE); 
        List<UserEntity> unactivatedAccts = query.getResultList();
        System.err.println("unactivated accts found: " + unactivatedAccts.size());
        for (UserEntity acct: unactivatedAccts){
            em.remove(acct.getUi()); 
            em.remove(acct); 
        }
        
        System.err.println("-------deleteUnverifiedAccounts was executed");
    
    }
      
    
    // find orderInfo entities which don't point to any particular users or orders
    // e.g. user accounts that have been deleted and have not placed any orders.
    
    @Schedule(dayOfMonth="*")
    public void deleteUnusedUserInfo(){
        String queryString = "SELECT p FROM UserInfoEntity p WHERE p.hasUser=?1 AND p.hasOrder= ?2"; 
        TypedQuery query = em.createQuery(queryString, UserInfoEntity.class);
        query.setParameter(1, false);
        query.setParameter(2, false); 
        List<UserInfoEntity> unusedUserInfo = query.getResultList();
        System.err.println("unusedUserInfo found: " + unusedUserInfo.size());
        for (UserInfoEntity ui: unusedUserInfo){
            em.remove(ui);
        }
        System.err.println("-------deleteUnusedUserInfo was executed");
    }
}

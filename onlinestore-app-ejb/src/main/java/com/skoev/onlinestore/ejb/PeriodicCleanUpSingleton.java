package com.skoev.onlinestore.ejb;

import com.skoev.onlinestore.entities.order.*;
import com.skoev.onlinestore.entities.user.*;
import javax.ejb.Schedule;
import java.util.*;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.ejb.Startup;
import javax.persistence.*;

//TODO: remove all println statements

/**
 *  The public methods in this singleton session EJB are executed periodically
 *  to clean up the database. The execution is triggered automatically by the 
 *  EJB container since the method are marked with a {@link javax.ejb.Schedule}
 *  annotation. 
 */
@Singleton
@LocalBean
@Startup
public class PeriodicCleanUpSingleton {
    /**
     * Injected entity manager.
     */
    @PersistenceContext(unitName = "EJB_PU")
    private EntityManager em;
    /**
     * Constant equal to number of milliseconds in a day. 
     */
    private static long MS_IN_DAY = 24 * 3600 * 1000;

    /**
     * This method closes out all the old orders. It finds all orders that are
     * in status 
     * {@link com.skoev.onlinestore.entities.order.OrderStatusEnum#SHIPPED} and
     * have not been modified for 20 or more days. Then, it changes their status
     * to 
     * {@link com.skoev.onlinestore.entities.order.OrderStatusEnum#ORDER_CLOSED}.
     * This method is executed automatically every day by the container. 
     */
    @Schedule(dayOfMonth = "*")
    public void closeOldOrders() {
        /*-get all the orders such that they are SHIPPED and have been so for 
         * more than 30 days.
         */
        Date today = new Date();

        Date twentyDaysAgo = new Date(today.getTime() - 20 * MS_IN_DAY);
        //Date twentyDaysAgo = new Date(today.getTime() - 10); 
        String shippedStatus = OrderStatusEnum.SHIPPED.toString();

        String queryString = "SELECT p FROM OrderEntity p WHERE p.currentStatus "
                + "= ?1 AND p.lastModified < ?2";
        TypedQuery query = em.createQuery(queryString, OrderEntity.class);
        query.setParameter(1, shippedStatus);
        query.setParameter(2, twentyDaysAgo);

        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        List<OrderEntity> shippedOrders = query.getResultList();
        for (OrderEntity order : shippedOrders) {
            closeOrder(order);
        }
    }

    /**
     * This method is called by {@link #closeOldOrders()} to change the status
     * of an order. 
     * @param order 
     */
    private void closeOrder(OrderEntity order) {
        OrderStatusEntity orderStatusEntity = new OrderStatusEntity();
        orderStatusEntity.setStatus(OrderStatusEnum.ORDER_CLOSED);
        orderStatusEntity.setStatusBegan(new Date());
        orderStatusEntity.setActor("sytem");
        order.getStatusHistory().add(orderStatusEntity);
        order.setCurrentStatus(orderStatusEntity.getStatus().toString());
        order.setLastModified(orderStatusEntity.getStatusBegan());
        order.setLastActor(orderStatusEntity.getActor());
    }

    
    /**
     * This method deletes unverified (unactivated) accounts. It find all 
     * {@link com.skoev.onlinestore.entities.user.UserEntity} objects in the 
     * database that are not activated and more than 30 days old. Then, it 
     * deletes them. This method is executed automatically every day by the 
     * container. 
     */
    @Schedule(dayOfMonth = "*")
    public void deleteUnverifiedAccounts() {
        /* - get all the users that are in the not verified group and have been
         * there for more than 30 days
         * - delete them
         */
        Date today = new Date();
        Date thirtyDaysAgo = new Date(today.getTime() - 30 * MS_IN_DAY);

        String queryString = "SELECT p FROM UserEntity p WHERE p.activated = ?1"
                + " AND p.acctCreationDate < ?2";

        TypedQuery query = em.createQuery(queryString, UserEntity.class);
        query.setParameter(1, false);
        query.setParameter(2, thirtyDaysAgo);

        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        List<UserEntity> unactivatedAccts = query.getResultList();
        for (UserEntity acct : unactivatedAccts) {
            em.remove(acct.getUi());
            em.remove(acct);
        }
    }

    /**
     * This method deletes unused user information. It finds all 
     * {@link com.skoev.onlinestore.entities.user.UserInfoEntity} objects in the
     * database that are not associated with a any order or any user account 
     * and deletes them. Such orphaned objects could 
     * be created for example if a customer who has not placed any orders 
     * deletes his account. This method is executed automatically every day by 
     * the container. 
     * 
     */
    @Schedule(dayOfMonth = "*")
    public void deleteUnusedUserInfo() {
        String queryString = "SELECT p FROM UserInfoEntity p WHERE p.hasUser=?1"
                + " AND p.hasOrder= ?2";
        TypedQuery query = em.createQuery(queryString, UserInfoEntity.class);
        query.setParameter(1, false);
        query.setParameter(2, false);
        List<UserInfoEntity> unusedUserInfo = query.getResultList();
        for (UserInfoEntity ui : unusedUserInfo) {
            em.remove(ui);
        }
    }
}

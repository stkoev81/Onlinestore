package com.skoev.onlinestore.ejb;

import com.skoev.onlinestore.entities.order.*;
import com.skoev.onlinestore.entities.product.*;
import javax.ejb.*;
import javax.ejb.Stateful;
import javax.ejb.LocalBean;
import javax.ejb.EJB;
import javax.persistence.*;
import javax.annotation.Resource;
import javax.transaction.*;
import java.util.logging.*;

/**
 * This stateful session EJB contains methods for manipulating an order in the
 * online 
 * bookstore after it has been placed. <br/><br/>
 * 
 * The proper usage of this class is: <br/>
 * <ul>
 * <li> Call {@link #setOrderID} to set the order ID for the 
 * order that will be updated 
 * </li> 
 * <li> Call {@link #lookupOrder} to find that order
 * </li>
 * <li> If necessary, use {@link #getOrder} to view details about that order
 * </li> 
 * <li> Call {@link #updateOrder} to updated the status of the order.
 * </li>
 * </ul>
 *  
 * The methods in this class use 
 * bean managed transactions instead of container
 * managed transactions (CMT is the default behavior). The reasons for this is 
 * that optimistic locking is used for some of the operations, and optimistic 
 * locking is likely to result in an exception upon committing. With CMT, this 
 * exception cannot be handled properly and it would
 * cause the stateful bean to be destroyed. With BMT, this 
 * exception can be handled better, the session bean can be preserved, 
 * and a more mild error message can be displayed to the user. 
 * 
 * @see com.skoev.onlinestore.entities.order.OrderEntity
 * 
 */
@Stateful
@LocalBean
@TransactionManagement(TransactionManagementType.BEAN)
public class ManageOrdersStateful {
    /**
     * Order that is being manipulated
     */
    private OrderEntity order;
    /**
     * ID of the order that is being manipulated
     */
    private Long orderID;
    
    /**
     * Injected EntityManager
     */
    @PersistenceContext(unitName = "EJB_PU")
    private EntityManager em;
    
    /**
     * Injected MailSenderStateless instance.
     */
    @EJB
    private MailSenderStateless mailSender;
    
    /**
     * Injected transaction for bean managed transactions.
     */
    @Resource
    private UserTransaction ut;
    
    /**
     * Logger used to log transaction rollback failure
     */
    private static final Logger logger = Logger.getLogger(
            ManageOrdersStateful.class.getName());

    /**
     * This method finds in the database the order whose ID has been set 
     * previously with {@link #setOrderID(java.lang.Long)}. 
     * After completing this method, 
     * the order can be obtained with {@link #getOrder()} 
     * 
     */
    public void lookupOrder() {        
        order = em.find(OrderEntity.class, orderID, LockModeType.OPTIMISTIC);
    }
    
    /**
     * This method updates the status of the order which has previously been 
     * looked up with {@link #lookupOrder()}. If there  are any exceptions upon
     * commit the transaction, a rollback is attempted. If the exception is
     * OptimisticLockException, it is caught and
     * an {@link UpdateFailedException} is thrown; if it is an 
     * {@link EmailException}, it is rethrown as itself. 
     * Any other exceptions are wrapped and 
     * rethrown as a RuntimeException. 
     * <br/><br/>
     * The update operation has several side effects. 
     * <ul>
     * <li> If the new order status is {@link OrderStatusEnum#PAYMENT_FAILED} or
     * {@link OrderStatusEnum#REFUND_COMPLETED}, the status is automatically 
     *  advanced to 
     * {@link OrderStatusEnum#ORDER_CLOSED}.  
     * </li>
     * <li> If the new order status is {@link OrderStatusEnum#PAYMENT_FAILED} or
     * {@link OrderStatusEnum#OUT_OF_STOCK} or {@link OrderStatusEnum#SHIPPED}, 
     * the availability numbers in {@link ProductAvailabilityEntity} are updated 
     * to reflect that. Pessimistic locking is used when updating those numbers 
     * to ensure that they are updated consistently. 
     * </li>
     * <li> This method will call {@link MailSenderStateless#sendStatusEmail} 
     * to send a status email to the customer who placed the order 
     * </li>
     * </ul>
     * 
     * 
     * 
     * @param ose The new status entity to be added to the status history of that
     * order
     * @throws UpdateFailedException If the there was an OptimisticLockException
     * while trying to commit
     * @throws EmailException If there was a problem while trying to send an 
     * email 
     */
    public void updateOrder(OrderStatusEntity ose) throws UpdateFailedException,
            EmailException {
        try {
            ut.begin();
            em.joinTransaction();
            updateOrderStatus(ose);
            em.merge(order);
            mailSender.sendStatusEmail(ose.getStatus(), order);
            ut.commit();
        } catch (Exception e) {
            try {
                ut.rollback();
            } catch (Exception e1) {
                //rollback failed, a potentially serious error that could
                //violate data integrity.
                //will log and rethrow as unchecked exception
                //EJB container will wrap it in an EJBException and pass to 
                //client
                logger.log(Level.SEVERE, "exception caught while trying to roll"
                        + " back transaction", e);
                throw new RuntimeException(e1);
            }
            if (e instanceof OptimisticLockException) {
                // mild errror, will just need to show message to user
                // rethrow as checked exception so client can deal with it
                throw new UpdateFailedException();  
            } else if (e instanceof EmailException){
                // this is a chekced exception
                // will rethrow so client can deal with it
                throw (EmailException) e; 
            }
            else {
                // unknown error, but at least rollback was succesful
                // will retrhow as unchecked exception 
                // EJB container will wrap it in an EJBException and pass to 
                // client. 
                throw new RuntimeException(e);
            }
        }
    }
    /**
     * This method is called internally by {@link #updateOrder}
     * @param ose
     */
    private void updateOrderStatus(OrderStatusEntity ose) {
        order.setCurrentStatus(ose.getStatus().toString());
        order.setLastActor(ose.getActor());
        order.setLastModified(ose.getStatusBegan());
        em.persist(ose);
        order.getStatusHistory().add(ose);

        // if the order is "finished", put it in the closed state
        if (ose.getStatus() == OrderStatusEnum.PAYMENT_FAILED
                || ose.getStatus() == OrderStatusEnum.REFUND_COMPLETED) {
            OrderStatusEntity closedStatus = new OrderStatusEntity();
            closedStatus.setActor("system");
            closedStatus.setStatus(OrderStatusEnum.ORDER_CLOSED);
            closedStatus.setStatusBegan(ose.getStatusBegan());
            em.persist(closedStatus);
            order.getStatusHistory().add(closedStatus);
            order.setLastActor(closedStatus.getActor());
            order.setCurrentStatus(closedStatus.getStatus().toString());
        }

        // if the order fails, update the number of product units in 
        // unprocessed orders
        if (ose.getStatus() == OrderStatusEnum.OUT_OF_STOCK
                || ose.getStatus() == OrderStatusEnum.PAYMENT_FAILED) {
            cleanUpFailedOrders();
        }
        // if the order is shipped, update the nubmer of products in the 
        // warehouse. 
        if (ose.getStatus() == OrderStatusEnum.SHIPPED) {
            updateWarehouseNumbers();
        }
    }

    
    /**
     * This method is called internally by {@link #updateOrderStatus}
     */
    private void cleanUpFailedOrders() {
        for (OrderLineEntity ole : order.getOrderLines()) {
            ProductAvailabilityEntity productNumbers = ole.getProduct()
                    .getNumbers();
            productNumbers = em.find(ProductAvailabilityEntity.class
                    , productNumbers.getId(), LockModeType.PESSIMISTIC_WRITE);
            Integer quantity = ole.getNumber();
            productNumbers.setNumberInUnprocessedOrders(
                    productNumbers.getNumberInUnprocessedOrders() - quantity);
            productNumbers.calculateNumberAvailable();
            em.merge(productNumbers);
        }
    }

    /**
     * This method is called internally by {@link #updateOrderStatus}
     */
    private void updateWarehouseNumbers() {
        for (OrderLineEntity ole : order.getOrderLines()) {
            ProductAvailabilityEntity productNumbers = ole.getProduct()
                    .getNumbers();
            productNumbers = em.find(ProductAvailabilityEntity.class
                    , productNumbers.getId(), LockModeType.PESSIMISTIC_WRITE);
            Integer quantity = ole.getNumber();
            productNumbers.setNumberInUnprocessedOrders(
                    productNumbers.getNumberInUnprocessedOrders() - quantity);
            productNumbers.setNumberInWarehouse(
                    productNumbers.getNumberInWarehouse() - quantity);
            em.merge(productNumbers);
        }

    }

    public OrderEntity getOrder() {
        return order;
    }

    public Long getOrderID() {
        return orderID;
    }

    public void setOrder(OrderEntity order) {
        this.order = order;
    }

    public void setOrderID(Long orderID) {
        this.orderID = orderID;
    }
}

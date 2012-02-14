/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.ejb;
import com.skoev.onlinestore.entities.order.*; 
import com.skoev.onlinestore.entities.product.*; 


import java.math.BigDecimal;
import javax.ejb.*; 
import javax.ejb.Stateful;
import javax.ejb.LocalBean;
import javax.ejb.EJB;
import javax.persistence.*;
import java.util.*; 
import javax.annotation.PostConstruct; 
import java.security.Principal; 
import java.util.concurrent.TimeUnit;
import javax.ejb.StatefulTimeout;
import javax.ejb.Remove;
import javax.annotation.PreDestroy;
import javax.ejb.PrePassivate; 
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.transaction.*; 
/*


/**
 *
 * @author stephan
 */
@Stateful
@LocalBean
@TransactionManagement(TransactionManagementType.BEAN)
public class ManageOrdersStateful {    
    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
   private OrderEntity order;
   private Long orderID; 
   private OrderStatusEntity orderStatus;
   
    
    @PersistenceContext(unitName = "EJB_PU") 
    private EntityManager em; 
    
     
    @EJB
    private MailSenderStateless mailSender;
    
    @Resource
    private UserTransaction ut; 
    

    
    @PostConstruct
    public void initialize(){
        
    }
    
    public void lookupOrder(){    
        // add lock
        order = em.find(OrderEntity.class, orderID, LockModeType.OPTIMISTIC); 
    }
    
    
    public void updateOrderStatus(OrderStatusEntity ose) throws Exception {
        System.err.println("updateOrderStatusCalled"); 
        System.err.println("new status: " + ose.getStatus().toString());
       
        order.setCurrentStatus(ose.getStatus().toString());
        order.setLastActor(ose.getActor());
        order.setLastModified(ose.getStatusBegan());        
        em.persist(ose);
        order.getStatusHistory().add(ose);    
        
        System.err.println("transaction status2: " + ut.getStatus()); 
        // active
        
        // if the order is "finished", put it in the closed state
        if(ose.getStatus()==OrderStatusEnum.PAYMENT_FAILED ||
            ose.getStatus()==OrderStatusEnum.REFUND_COMPLETED){
            OrderStatusEntity closedStatus = new OrderStatusEntity();
            closedStatus.setActor("system"); 
            closedStatus.setStatus(OrderStatusEnum.ORDER_CLOSED);
            closedStatus.setStatusBegan(ose.getStatusBegan());
            em.persist(closedStatus);
            order.getStatusHistory().add(closedStatus); 
            order.setLastActor(closedStatus.getActor());
            order.setCurrentStatus(closedStatus.getStatus().toString());             
        }
        
        if(ose.getStatus()==OrderStatusEnum.OUT_OF_STOCK || 
                ose.getStatus()==OrderStatusEnum.PAYMENT_FAILED){
           cleanUpFailedOrders();       
        }
        if(ose.getStatus()==OrderStatusEnum.SHIPPED){
            updateWarehouseNumbers(); 
        }
    }
    
    public void updateOrder(OrderStatusEntity ose) throws UpdateFailedException{
          System.err.println("----updateORderCalled"); 
        
            try{
                System.err.println("statuses: "); 
                System.err.println(Status.STATUS_ACTIVE); //0
                System.err.println(Status.STATUS_COMMITTED); //3 
                System.err.println(Status.STATUS_COMMITTING); //8
                System.err.println(Status.STATUS_MARKED_ROLLBACK); //1
                System.err.println(Status.STATUS_NO_TRANSACTION); //6
                System.err.println(Status.STATUS_PREPARED); //2
                System.err.println(Status.STATUS_PREPARING); //7
                System.err.println(Status.STATUS_ROLLEDBACK); //4
                System.err.println(Status.STATUS_ROLLING_BACK); //9
                System.err.println(Status.STATUS_UNKNOWN); //5
                
                System.err.println("trans stat 0:" + ut.getStatus()); 
                // no transaction                
                ut.begin();                
                em.joinTransaction();
                
                // active
                
                System.err.println("trans stat 1: " + ut.getStatus()); 
                updateOrderStatus(ose);
               
                //active
                System.err.println("trans stat 3: " + ut.getStatus()); 
                
                em.merge(order);
                ut.commit();
                // no transaction
                System.err.println("trans stat 4: " + ut.getStatus()); 
                System.err.println("---Committed"); 
                mailSender.sendStatusEmail(ose.getStatus(), order);    
            }
            catch(Exception e){
                try {
                    ut.rollback();
                    System.err.println("rolling back"); 
                
                }
                catch (Exception e1) {
                    System.err.println("roll back failed");
                    throw new RuntimeException(e1);
                    
                }
                
                //rollback succeeded
                if(e instanceof OptimisticLockException){
                    throw new UpdateFailedException(); 
                //mild error, will jsut display message to user;
                }
                else{
                    throw new RuntimeException(e);
                //unknown error but at leasst rollback succeeded, log it. 
                }
              }
               
         
    }  
    
    
    
//    public void updateOrder(OrderStatusEntity ose) throws UpdateFailedException{
//        order.setCurrentStatus(ose.getStatus().toString());
//        order.setLastActor(ose.getActor());
//        order.setLastModified(ose.getStatusBegan());        
//        em.persist(ose);
//        order.getStatusHistory().add(ose); 
//        
//        
//        // if the order is "finished", put it in the closed state
//        if(ose.getStatus()==OrderStatusEnum.PAYMENT_FAILED ||
//            ose.getStatus()==OrderStatusEnum.REFUND_COMPLETED){
//            OrderStatusEntity closedStatus = new OrderStatusEntity();
//            closedStatus.setActor("system"); 
//            closedStatus.setStatus(OrderStatusEnum.ORDER_CLOSED);
//            closedStatus.setStatusBegan(ose.getStatusBegan());
//            em.persist(closedStatus);
//            order.getStatusHistory().add(closedStatus); 
//            order.setLastActor(closedStatus.getActor());
//            order.setCurrentStatus(closedStatus.getStatus().toString());             
//        }
//        
//        if(ose.getStatus()==OrderStatusEnum.OUT_OF_STOCK || 
//                ose.getStatus()==OrderStatusEnum.PAYMENT_FAILED){
//           cleanUpFailedOrders();       
//        }
//        if(ose.getStatus()==OrderStatusEnum.SHIPPED){
//            updateWarehouseNumbers(); 
//        }
//        
//        
//        
//        try {
//            em.merge(order);
//            em.flush();
//        }
//        catch (OptimisticLockException e) {
//            ctx.setRollbackOnly();
//            throw new UpdateFailedException();
//        }
//        
//        mailSender.sendStatusEmail(ose.getStatus(), order);
//    }   
    
    

    public void cleanUpFailedOrders(){
        for(OrderLineEntity ole  : order.getOrderLines()){
           
           ProductAvailabilityEntity productNumbers = ole.getProduct().getNumbers();
           
           productNumbers = em.find(ProductAvailabilityEntity.class, productNumbers.getId(), LockModeType.PESSIMISTIC_WRITE);
           Integer quantity = ole.getNumber();           
           productNumbers.setNumberInUnprocessedOrders(productNumbers.getNumberInUnprocessedOrders() - quantity); 
           productNumbers.calculateNumberAvailable();
           em.merge(productNumbers); 
        }  
    }

    public void updateWarehouseNumbers(){
        for(OrderLineEntity ole  : order.getOrderLines()){
           
            ProductAvailabilityEntity productNumbers = ole.getProduct().getNumbers();
           productNumbers = em.find(ProductAvailabilityEntity.class, productNumbers.getId(), LockModeType.PESSIMISTIC_WRITE);
           
           
           Integer quantity = ole.getNumber();           
           productNumbers.setNumberInUnprocessedOrders(productNumbers.getNumberInUnprocessedOrders() - quantity); 
           productNumbers.setNumberInWarehouse(productNumbers.getNumberInWarehouse() - quantity);
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.ejb;

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
public class ManageProductsStateful {    
    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
   private ProductEntity product;
   private Long productID; 
   
   @Resource
   private UserTransaction ut; 
   
    
    @PersistenceContext(unitName = "EJB_PU") 
    private EntityManager em; 
    
    @EJB
    private MailSenderStateless mailSender;
   
    
    @PostConstruct
    public void initialize(){
        
    }
    
    public void lookupProduct(){ 
        
        product = em.find(ProductEntity.class, productID, LockModeType.OPTIMISTIC); 
    }
    
    // TODO: handle exception in flushing this
      public void updateProduct(ProductEntity pe, boolean updateNumber) throws UpdateFailedException{
        product = pe;
            try{
                ut.setTransactionTimeout(30);
                
                ut.begin();
                em.joinTransaction();
                if(updateNumber){                                    
                    ProductAvailabilityEntity numbers = pe.getNumbers(); 
                    Integer numberInWarehouse = numbers.getNumberInWarehouse(); 
                    numbers = em.find(ProductAvailabilityEntity.class, numbers.getId(), LockModeType.PESSIMISTIC_WRITE); 
                    numbers.setNumberInWarehouse(numberInWarehouse);
                    numbers.calculateNumberAvailable();
                    em.merge(numbers); 
                }
                
                em.merge(product);
                ut.commit();
                mailSender.sendAlert("Product id: " + pe.getProductID(), "Product modified");
            
            }
            catch(Exception e){
                try{
                    ut.rollback();
                }
                catch(Exception e1){
                    throw new RuntimeException(e1);
                    //could not roll back
                }
                
                if (e instanceof OptimisticLockException)
                    throw new UpdateFailedException(); 
                else{
                    throw new RuntimeException(e);
                }
       
            
        }       
        
              
        
        
    }
    
    
    
//    public void updateProduct(ProductEntity pe) throws UpdateFailedException{
//        product = pe;
//                
//        try {          
//            em.merge(product);
//            em.flush();
//        }
//        // this version works fine if there is a lock exception  => transaction is rolled back
//        //what if: flush works fine but commit fails (e.g. another user modifies it while sending mail?)
//        // then, you ejb will throw a runtime exception and will be destroyed. Need to commit and catch the 
//        // possible exception. 
//        catch (OptimisticLockException e) {
//            ctx.setRollbackOnly();
//            throw new UpdateFailedException();
//        }
//        mailSender.sendAlert("Product id: " + pe.getProductID(), "Product modified");
//        
//    }   
    

    public ProductEntity getProduct() {
        return product;
    }

    public Long getProductID() {
        return productID;
    }

    public void setProduct(ProductEntity product) {
        this.product = product;
    }

    public void setProductID(Long productID) {
        this.productID = productID;
    }
      
}

package com.skoev.onlinestore.ejb;

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
 * This stateful session EJB is used for modifying existing products in the 
 * online store. <br/><br/>
 * 
 * The proper usage of this class is as follows: 
 * <ul>
 * <li> Call {@link #setProductID} to set the product ID for the product which
 * will be updated
 * </li>
 * <li> Call {@link #lookupProduct} to find that product
 * </li>
 * <li> Use {@link #getProduct} to view details about that product and modify it
 * </li>
 * <li> Call {@link #updateProduct} to merge the modified product to the 
 * database.
 * </li>
 * </ul>
 * 
 * <br/><br/>
 * Similarly to {@link ManageOrdersStateful}, 
 * the methods in this class use bean managed transactions instead of 
 * container managed transactions because they are likely to throw 
 * javax.Persistence.OptimisticLockException. See {@link ManageOrdersStateful} 
 * for an explanation why this choice is made.
 * 
 * @see com.skoev.onlinestore.entities.product.ProductEntity
 * 
 */
@Stateful
@LocalBean
@TransactionManagement(TransactionManagementType.BEAN)
public class ManageProductsStateful {

    /**
     * Product that is being updated
     */
    private ProductEntity product;
    /**
     * ID of the product that is being updated
     */
    private Long productID;
    /**
     * Injected user transaction for bean managed transactions
     */
    @Resource
    private UserTransaction ut;
    /**
     * Injected EntityManager
     */
    @PersistenceContext(unitName = "EJB_PU")
    private EntityManager em;
    /**
     * Injected MailSenderStateless instance
     */
    @EJB
    private MailSenderStateless mailSender;
    
    /**
     * Logger used to log transaction rollback failure
     */
    private static final Logger logger = Logger.getLogger(
            ManageProductsStateful.class.getName());

    /**
     * This method finds in the database the product whose ID has been set 
     * previously with {@link #setProductID(java.lang.Long) }. 
     * After completing this method, the 
     * product can be obtained with {@link #getProduct()} 
     * 
     */
    public void lookupProduct() {
        product = em.find(ProductEntity.class, productID, LockModeType.OPTIMISTIC);
    }

    /**
     * Merges its ProductEntity argument to the database. Normally, this is  
     * a product that was previously looked up and updated by the client of 
     * this method. The operation uses optimistic locking. 
     * 
     * <br/> <br/>
     * If there are any exceptions upon
     * committing the transaction, a rollback is attempted. If the exception is
     * OptimisticLockException, it is caught and
     * an {@link UpdateFailedException} is thrown; if it is an 
     * {@link EmailException}, it is rethrown as itself. 
     * Any other exceptions are wrapped and 
     * rethrown as a RuntimeException. 
     * <br/><br/>
     * 
     * The caller of this method can choose not to update the availability 
     * numbers (see {@link 
     * com.skoev.onlinestore.entities.product.ProductAvailabilityEntity}) for 
     * the product by passing updateNumbers=false. In that case, the
     * numbers ProductAvailabilityEntity associated with the argument pe is 
     * ignored and not merged to the database. This option allows the client
     * to modify aspects of the product without worrying about its current 
     * inventory. 
     *      
     * <br/><br/>
     * If the caller passes updateNumbers=true, the availability numbers will be
     * only partially updated. More precisely, only the numberInWarehouse will be 
     * copied from the argument pe and updated in the database. The reason for
     * this is that the other 
     * availability numbers (numberAvailable, numberInCarts, 
     * and numberInUnprocessedOrders) depend on other variables in the 
     * application and the user could cause inconsistencies by setting them 
     * directly. 
     *
     * <br/><br/>
     * 
     * This method calls {@link MailSenderStateless#sendAlert} to send an 
     * email alert that a product was modified. 
     *     
     *  
     * @param pe The updated product entity that will be merged to the 
     * database by this method
     * @param updateNumber Flag indicating whether the product availability 
     * number should be updated or not.
     * @throws UpdateFailedException If the there was an OptimisticLockException
     * while trying to commit transaction to the database
     * @throws EmailException If there was a problem while trying to send an 
     * alert email 
     *  
     */
    public void updateProduct(ProductEntity pe, boolean updateNumber) 
            throws UpdateFailedException, EmailException {
        product = pe;
        try {
            ut.begin();
            em.joinTransaction();
            if (updateNumber) {
                ProductAvailabilityEntity numbers = pe.getNumbers();
                Integer numberInWarehouse = numbers.getNumberInWarehouse();
                // get the latest inventory for this product
                // use a pessimistic lock to make sure the next calculation 
                // will be consistent, i.e. no one will change the numbers 
                // before this transaction commits
                numbers = em.find(ProductAvailabilityEntity.class
                        , numbers.getId(), LockModeType.PESSIMISTIC_WRITE);
                numbers.setNumberInWarehouse(numberInWarehouse);
                // calculate the availability numbers based on the newly set 
                // numberInWarehouse
                numbers.calculateNumberAvailable();
                em.merge(numbers);
            }
            em.merge(product);
            mailSender.sendAlert("Product id: " + pe.getProductID()
                    , "Product modified");
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

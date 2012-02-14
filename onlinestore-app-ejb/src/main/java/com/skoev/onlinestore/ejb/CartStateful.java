/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.ejb;
import com.skoev.onlinestore.entities.order.*; 
import com.skoev.onlinestore.entities.product.*; 
import com.skoev.onlinestore.entities.user.*; 

import java.math.BigDecimal;
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


/**
 *
 * @author stephan
 */
@Stateful
@LocalBean
public class CartStateful {    
    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    OrderEntity order;
    UserInfoEntity ui; 
    Map<Long, OrderLineEntity> cartContents = new LinkedHashMap<Long, OrderLineEntity>();
    Map<Long, Integer> cartQuantities = new LinkedHashMap<Long, Integer>(); 
    @PersistenceContext(unitName = "EJB_PU") 
    private EntityManager em; 
    
      @EJB
    private MailSenderStateless mailSender;
    
    @PostConstruct
    public void initialize(){
    }
    
    public void initUserInfo(Principal userPrincipal){
        order = new OrderEntity();
        if (userPrincipal!=null){
            String username = userPrincipal.getName();
            UserEntity user = em.find(UserEntity.class, username);
            ui= new UserInfoEntity(user.getUi());
            order.setCustomer(user); 
        }
        else {
            ui = new UserInfoEntity();
        }
    }
    
    public void placeOrder(){
        updateOrderNumbers(); 
        ui.setHasOrder(true);
        order.setUi(ui); 
        order.setOrderLines(new LinkedList<OrderLineEntity>(cartContents.values()));
        order.setTotalCost(calcTotalCost());
        order.setOrderDate(new Date());
        order.setLastModified(order.getOrderDate());
        
        OrderStatusEntity ose = new OrderStatusEntity();
        ose.setStatus(OrderStatusEnum.ORDER_RECEIVED); 
        order.setCurrentStatus(OrderStatusEnum.ORDER_RECEIVED.toString());
        ose.setStatusBegan(new Date());
        ose.setActor("customer"); 
        order.setLastActor("customer");
        List<OrderStatusEntity> statuses = new LinkedList<OrderStatusEntity>(); 
        statuses.add(ose);
        order.setStatusHistory(statuses);
        
        em.persist(ui);
        em.persist(order);
        cartContents.clear();
        cartQuantities.clear();
        mailSender.sendStatusEmail(ose.getStatus(),order);    
    }
    
    // lock each product availability entity
    // change the number in carts, the number in orders, the number available. 
    public void updateOrderNumbers(){
        for(OrderLineEntity ole  : cartContents.values()){
           ProductAvailabilityEntity productNumbers = ole.getProduct().getNumbers();
           //look up the latest productNumbers
           productNumbers = em.find(ProductAvailabilityEntity.class, productNumbers.getId(), LockModeType.PESSIMISTIC_WRITE);
           Integer quantity = ole.getNumber(); 
           productNumbers.setNumberInCarts(productNumbers.getNumberInCarts() - quantity); 
           productNumbers.setNumberInUnprocessedOrders(productNumbers.getNumberInUnprocessedOrders() + quantity); 
        }
    }
      
    
    public List<OrderLineEntity> getCartProductList (){
        List<OrderLineEntity> list = new ArrayList<OrderLineEntity>(cartContents.values()) ;        
        
        return list; 
    }
    
    public boolean addToCart(long productID, int quantity) throws ProductNotAvailableException{        
        //look up product, throw Exception if it does not exist 
        //place lock on product
        //check its availability; if OK, add to cart and reduce #available, otherwise exception
        //release lock on product       
       
             
//       if (product==null){ return false;}
//       if (product.getNumberAvailable() < quantity) {
//           em.persist(product);
//           return false;
//       }     
     
        ProductEntity product = em.find(ProductEntity.class, productID);   
     ProductAvailabilityEntity productNumbers = product.getNumbers(); 
     productNumbers = em.find(ProductAvailabilityEntity.class, productNumbers.getId(), LockModeType.PESSIMISTIC_WRITE);
     
     
     if (productNumbers.getNumberAvailable() <= 0 || !product.getDisplayProductInStore()){
         throw new ProductNotAvailableException(); 
     }
     
     if (cartContents.containsKey(productID)){
         OrderLineEntity orderLine = cartContents.get(productID);
         orderLine.setNumber(orderLine.getNumber() + quantity);         
         cartQuantities.put(productID ,  orderLine.getNumber());
     }
     else {         
         OrderLineEntity orderLine = new OrderLineEntity();
         orderLine.setProduct(product);
         orderLine.setNumber(quantity);
         cartContents.put(productID, orderLine);
         cartQuantities.put(productID, quantity);
     }
              
         productNumbers.setNumberInCarts(productNumbers.getNumberInCarts() + quantity);         
         productNumbers.calculateNumberAvailable();
            
       return true;
    }
    
    public void updateCart(){
        for (Long l: cartContents.keySet()){
            int newQuantity = cartContents.get(l).getNumber();
            int oldQuantity = cartQuantities.get(l);
            if (oldQuantity != newQuantity){
                updateProductQuantity(l,oldQuantity,newQuantity);
            }
        }
    }
    
    public void updateProductQuantity(Long productID, int oldQuantity, int newQuantity){        
       ProductEntity product = em.find(ProductEntity.class, productID);   
       ProductAvailabilityEntity productNumbers = product.getNumbers(); 
       productNumbers = em.find(ProductAvailabilityEntity.class, productNumbers.getId(), LockModeType.PESSIMISTIC_WRITE);
               
       int actualQuantity;
       productNumbers.setNumberInCarts(productNumbers.getNumberInCarts() - oldQuantity);
       productNumbers.calculateNumberAvailable();
        
        if(newQuantity==0){
            cartContents.remove(productID);
            cartQuantities.remove(productID); 
        }
                
        else if (productNumbers.getNumberAvailable() < newQuantity){
            actualQuantity=productNumbers.getNumberAvailable();            
            productNumbers.setNumberInCarts(productNumbers.getNumberInCarts() + actualQuantity); 
            productNumbers.calculateNumberAvailable();
            
            cartQuantities.put(productID, actualQuantity);
            cartContents.get(productID).setNumber(actualQuantity);
        }
        else { 
            cartQuantities.put(productID,newQuantity);
            productNumbers.setNumberInCarts(productNumbers.getNumberInCarts() + newQuantity); 
            productNumbers.calculateNumberAvailable();
        }          
       
    }
        
    public void emptyCart(){
      for (Long l: cartContents.keySet()){
          cartContents.get(l).setNumber(0);
      }
      updateCart(); 
    }
    
 
    
    public BigDecimal calcTotalCost(){
        BigDecimal sum = BigDecimal.ZERO; 
           for (Long l: cartContents.keySet()){
               OrderLineEntity ol = cartContents.get(l);
               BigDecimal linePrice = ol.getProduct().getPrice().multiply(BigDecimal.valueOf(ol.getNumber()));
               sum=sum.add(linePrice);
        }
    return sum; 
    
    }
    

    public Map<Long, OrderLineEntity> getCartContents() {
        return cartContents;
    }

    public UserInfoEntity getUi() {
        return ui;
    }

    public void setUi(UserInfoEntity ui) {
        this.ui = ui;
    }

    public OrderEntity getOrder() {
        return order;
    }

    public void setOrder(OrderEntity order) {
        this.order = order;
    }

    
}

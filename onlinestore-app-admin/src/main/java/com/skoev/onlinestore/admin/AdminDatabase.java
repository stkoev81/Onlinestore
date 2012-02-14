/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.admin;

import com.skoev.onlinestore.entities.initialize.*; 
import java.io.File; 
import org.w3c.dom.*;
import com.skoev.onlinestore.entities.user.*; 
import com.skoev.onlinestore.entities.order.*; 
import java.util.*; 
import java.math.BigDecimal; 

/**
 *
 * @author stephan
 */
public class AdminDatabase {
    private EntityAccessor entityAccessor = new EntityAccessor("PU_Admin"); 
    
    
     
    public static void main(String[] args) {
            new AdminDatabase().run(); 
    
    }
    
    public void run(){
//        ProductEntity pe1 = new ProductEntity(); 
//        ProductEntity pe2 = new ProductEntity(); 
//        pe1.setName("product 1"); 
//        pe2.setName("product 2"); 
//        entityAccessor.deleteEntity(ProductEntity.class, 101l); 
        //entityAccessor.deleteEntity(ProductEntity.class, 151l); 
      //  OrderEntity order = entityAccessor.getEntity(OrderEntity.class, 110l);
       // UserInfoEntity ui = order.getUi(); 
       // ui.setFirstName("Newman");
       // entityAccessor.mergeEntity(ui);
        
        UserInfoEntity ui = entityAccessor.getEntity(UserInfoEntity.class, 85l); 
        ui.setFirstName("Newman"); 
        entityAccessor.mergeEntity(ui);
        
        
        
        
        
        
    }
}

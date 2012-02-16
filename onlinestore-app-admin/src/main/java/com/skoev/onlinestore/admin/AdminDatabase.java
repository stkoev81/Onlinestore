/**
 * This class is meant to serve as an administration script for the online store
 * database. If some changes need to be made to the database that cannot be made
 * through the web application, they can be made programmatically by modifying 
 * this class, compiling it, and executing it from a Java SE environment. 
 * 
 * <br/><br/>
 * Examples of administrative changes to the database include deleting old products 
 * or orders, changing user privileges, executing arbitrary queries. When making
 * administrative changes, care must be taken to not lose or overwrite data. 
 * Generally, an exception will be thrown and the operation will be rolled back 
 * if it would result in violating data integrity constraints (such as deleting
 * a product which is referred to by existing orders). Nevertheless, a lot of 
 * damage can be done by careless administrative changes.  
 * 
 * <br/><br/>
 * This class is placed in its own package and jar file so that it can be 
 * recompiled without
 * having to recompile other classes. 
 * 
 */
package com.skoev.onlinestore.admin;

import com.skoev.onlinestore.entities.initialize.*; 
import java.io.File; 
import org.w3c.dom.*;
import com.skoev.onlinestore.entities.user.*; 
import com.skoev.onlinestore.entities.order.*; 
import com.skoev.onlinestore.entities.product.*; 
import java.util.*; 
import java.math.BigDecimal; 

/**
 *
 * @author stephan
 */
public class AdminDatabase {
    private EntityAccessor entityAccessor = new EntityAccessor("PU_Admin"); 
  
    public static void main(String[] args) {
        AdminDatabase admin = new AdminDatabase();    
        admin.scriptExample1(); 
    
    }
    
    //example script: changing user privileges
    private void scriptExample1(){
        UserEntity user = entityAccessor.findEntity(UserEntity.class, "Stefan"); 
        //adding a privilege
        user.getGroupMemberships().add(new GroupEntity("MANAGER"));       
        //removing a privilege
        user.getGroupMemberships().remove(new GroupEntity("MANAGER"));       
        entityAccessor.mergeEntity(user);   
    }
    
    //example script: deleting a product with productID=1000
    private void scriptExample2(){
        entityAccessor.findAndDeleteEntity(ProductEntity.class, 1000L);     
    }
    
    //example script: deleting of an order with orderID=1000
    private void scriptExample3(){
        entityAccessor.findAndDeleteEntity(OrderEntity.class, 1000L);
    }
    
    //TODO: write a useful query.
    //example script: querying the number of 
    private void scriptExample4(){
      //  String query = "SELECT p from Product"
    }
    
    
}

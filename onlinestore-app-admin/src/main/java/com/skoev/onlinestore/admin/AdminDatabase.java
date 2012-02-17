package com.skoev.onlinestore.admin;
import com.skoev.onlinestore.entities.initialize.*;
import com.skoev.onlinestore.entities.user.*;
import com.skoev.onlinestore.entities.order.*;
import com.skoev.onlinestore.entities.product.*;
import java.util.*;

/**
 * This class is meant to serve as an administration script for the online store
 * database. If some changes need to be made to the database that cannot be made
 * through the web application, they can be made programmatically by modifying 
 * this class, compiling it, and executing it from a Java SE environment. 
 * 
 * <br/><br/>
 * Examples of administrative changes to the database include deleting old 
 * products 
 * or orders, changing user privileges, executing arbitrary queries, etc. When 
 * making
 * administrative changes, care must be taken to not lose or overwrite data. 
 * Generally, an exception will be thrown and the operation will be rolled back 
 * if it tries to violate data integrity constraints (such as deleting
 * a product which is referred to by existing orders). Nevertheless, 
 * data can still be lost or corrupted by improper administrative changes.  
 * 
 * <br/><br/>
 * This class is placed in its own package and jar file so that it can be 
 * recompiled without
 * having to recompile other classes. 
 * 
 */
public class AdminDatabase {

    private EntityAccessor entityAccessor = new EntityAccessor("PU_Admin");

    /**
     * Modify this method, recompile package, and execute from Java SE 
     * environment to make changes to the database
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        AdminDatabase admin = new AdminDatabase();
      //  admin.codeExample1();

    }

    /**
     * Example code for changing user privileges
     */
    private void codeExample1() {
        UserEntity user = entityAccessor.findEntity(UserEntity.class, "Stefan");
        //adding a privilege
        user.getGroupMemberships().add(new GroupEntity("MANAGER"));
        //removing a privilege
        user.getGroupMemberships().remove(new GroupEntity("MANAGER"));
        entityAccessor.mergeEntity(user);
    }

    /**
     * Example code for deleting a product with productID=1000 
     */
    private void codeExample2() {
        entityAccessor.findAndDeleteEntity(ProductEntity.class, 1000L);
    }
    
    /**
     * Example code for deleting an order with orderID=1000
     */
    private void codeExample3() {
        entityAccessor.findAndDeleteEntity(OrderEntity.class, 1000L);
    }

    /**
     * Example code for querying the number of times a product with
     * productID=2 has been ordered.
     */
    private void codeExample4() {
        Long productID = 2L;
        String query = "SELECT p from OrderLineEntity p WHERE "
                + "p.product.productID=?1";
        List<OrderLineEntity> results = entityAccessor
                .doQuery(OrderLineEntity.class, query, productID);
        int count = 0;
        for (OrderLineEntity ole : results) {
            count = ole.getNumber() + count;
        }
        System.out.println("Product with ID=" + productID + " has been ordered "
                + count + " times.");
    }
}

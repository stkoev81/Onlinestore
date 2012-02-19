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
import java.security.Principal;

/**
 * This stateful session EJB represents a shipping cart in the online store. It 
 * has methods for adding and removing products from the cart, and also for
 * placing
 * an order based on  the cart contents. 
 * The proper usage of this class is as follows.
 * <br/>
 * <ul>
 * <li> First, call {@link #addToCart} to add products.
 * </li> 
  * <li> To update product quantities, get the cartContents by calling 
 * {@link #getCartContents()} and update the numbers in the respective
 * OrderLineEntity. Then, call {@link #updateCart()}
 * </li> 
 * <li> If necessary to empty the cart, call {@link #emptyCart()}
 * </li>
 * <li> To place an order for the cart contents, first call {@link #initOrder}.
 * Then, get the UserInfoEntity by calling {@link #getUi()} and 
 * update its contents. Finally, call {@link #placeOrder() }
 * </li>
 * </ul>
 * 
 * 
 */
@Stateful
@LocalBean
public class CartStateful {

    /**
     * The order that will be placed when the {@link #placeOrder()} method is 
     * called.
     */
    private OrderEntity order;
    /**
     * The user information associated with the {@link #order}; 
     */
    private UserInfoEntity ui;
    /**
     * This is a mapping of productID's to order line items
     */
    private Map<Long, OrderLineEntity> cartContents = new LinkedHashMap<Long
            , OrderLineEntity>();
    /**
     * A mapping of productID's to number of units of product
     */
    private Map<Long, Integer> cartQuantities = new LinkedHashMap<Long
            , Integer>();
    
    /**
     * Injected entity manager used for accessing database
     */
    @PersistenceContext(unitName = "EJB_PU")
    private EntityManager em;
    
    /**
     * Injected ejb for sending e-mail notifications
     */
    @EJB
    private MailSenderStateless mailSender;

    /**
     * Prepares this object for placing an order. First, creates a new 
     * OrderEntity object. Then, checks if the user is logged in; 
     * if so, copies the UserInfoEntity from the UserEnitity object. If not
     * logged
     * in, a new blank UserInforEntity object is created. 
     * 
     * @param userPrincipal The Principal object for the current user. If a 
     * user is not logged in, this value is null. 
     */
    public void initOrder(Principal userPrincipal) throws MarkerException {
        order = new OrderEntity();
        if (userPrincipal != null) {
            String username = userPrincipal.getName();
            UserEntity user = em.find(UserEntity.class, username);
            ui = new UserInfoEntity(user.getUi());
            order.setCustomer(user);
        } else {
            ui = new UserInfoEntity();
        }
    }

    /**
     * Submits the {@link #order}. First, copies the OrderLineEntity objects
     * form {@link #cartContents} and the UserInfo from {@link #ui} to 
     * {@link #order}. 
     * It then sets various fields in the order, such as totalCost, 
     * lastModified, currentStatus, statusHistory, etc. Then it persists the
     * order 
     * to the database, 
     * sends a status email, and clears the cartContents and cartQuantities. 
     * It also updates the product availability by calling 
     * {@link #updateOrderNumbers() }
     * 
     * @throws EmailException If there is problem sending out a status email.
     */
    public void placeOrder() throws EmailException, MarkerException{
        updateOrderNumbers();
        ui.setHasOrder(true);
        order.setUi(ui);
        order.setOrderLines(new LinkedList<OrderLineEntity>(
                cartContents.values()));
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
        mailSender.sendStatusEmail(ose.getStatus(), order);
    }

    /**
     * Called by {@link #placeOrder()}; changes the numbers in the 
     * ProductAvailabilityEntity by decrementing the numberInCarts and 
     * incrementing numberInUnprocessedOrders. It uses pessimistic locking to
     * ensure that these numbers are changed consistently. 
     * 
     * @see com.skoev.onlinestore.entities.product.ProductAvailabilityEntity
     */
    private void updateOrderNumbers() throws MarkerException {
        for (OrderLineEntity ole : cartContents.values()) {
            ProductAvailabilityEntity productNumbers = ole.getProduct()
                    .getNumbers();
            //look up the latest productNumbers
            productNumbers = em.find(ProductAvailabilityEntity.class
                    , productNumbers.getId(), LockModeType.PESSIMISTIC_WRITE);
            Integer quantity = ole.getNumber();
            productNumbers.setNumberInCarts(
                    productNumbers.getNumberInCarts() - quantity);
            productNumbers.setNumberInUnprocessedOrders(
                    productNumbers.getNumberInUnprocessedOrders() + quantity);
        }
    }

    /**
     * Returns the values of {@link #cartContents}
     * @return 
     */
    public List<OrderLineEntity> getCartProductList() throws MarkerException {
        List<OrderLineEntity> list = new ArrayList<OrderLineEntity>(
                cartContents.values());
        return list;
    }
    
    /**
     * Adds a single unit of the product to the shopping cart, {@literal i.e.}
     * it updates the {@link #cartContents} and {@link #cartQuantities}. It also
     * updates the numbers in ProductAvailabilityEntity by incrementing the
     * numberInCarts and decrementing the numberAvailable. It uses pessimistic 
     * locking to ensure that these numbers are changed consistently. 
     * 
     * @param productID
     * @throws ProductNotAvailableException If the numberAvailable for this 
     * product is 0 or the displayProductInStore flag of this product is set to
     * false or this productID is not found.
     * 
     * @see com.skoev.onlinestore.entities.product.ProductAvailabilityEntity
     * @see com.skoev.onlinestore.entities.product.ProductEntity
     */
    public void addToCart(long productID) throws ProductNotAvailableException, MarkerException {
        ProductEntity product = em.find(ProductEntity.class, productID);
        if (product == null){
            throw new ProductNotAvailableException();
        }
        ProductAvailabilityEntity productNumbers = product.getNumbers();
        productNumbers = em.find(ProductAvailabilityEntity.class
                , productNumbers.getId(), LockModeType.PESSIMISTIC_WRITE);

        if (productNumbers.getNumberAvailable() <= 0
                || !product.getDisplayProductInStore()) {
            throw new ProductNotAvailableException();
        }

        final int quantity = 1; 
        if (cartContents.containsKey(productID)) {
            OrderLineEntity orderLine = cartContents.get(productID);
            orderLine.setNumber(orderLine.getNumber() + quantity);
            cartQuantities.put(productID, orderLine.getNumber());
        } else {
            OrderLineEntity orderLine = new OrderLineEntity();
            orderLine.setProduct(product);
            orderLine.setNumber(quantity);
            cartContents.put(productID, orderLine);
            cartQuantities.put(productID, quantity);
        }

        productNumbers.setNumberInCarts(productNumbers.getNumberInCarts()
                + quantity);
        productNumbers.calculateNumberAvailable();
        
    }
    
    /**
     * Updates {@link #cartQuantities} to reflect the product quantities in 
     * {@link #cartContents}. It internally calls 
     * {@link #updateProductQuantity(java.lang.Long, int, int)} to achieve this.
     * Therefore, updating the cart quantities consists of
     * two steps: <br/>
     * 1) Change the numbers in each OrderLineEntity in {@link #cartContents}
     * <br/>
     * 2) Call this method <br/>
     * 
     * @see #updateProductQuantity(java.lang.Long, int, int) 
     */
    public void updateCart() throws MarkerException {
        for (Long l : cartContents.keySet()) {
            int newQuantity = cartContents.get(l).getNumber();
            int oldQuantity = cartQuantities.get(l);
            if (oldQuantity != newQuantity) {
                updateProductQuantity(l, oldQuantity, newQuantity);
            }
        }
    }

    /**
     * Updates {@link #cartQuantities} according to the arguments. This method
     * is called by {@link #updateCart()}. If the product's numberAvailable is
     * less than the newQuantity argument, the quantity is updated to 
     * number Available; otherwise, it is updated to to newQuantity. <br/><br/> 
     * 
     * If newQuantity == 0, the product is removed from both 
     * {@link #cartQuantities} and {@link #cartContents} <br/> <br/>
     * 
     * This method also updates the properties numberInCarts and numberAvailable
     * in ProductAvailabilityEntity. It uses pessimistic 
     * locking to ensure that these numbers are changed consistently. 
     * 
     * @param productID
     * @param oldQuantity
     * @param newQuantity 
     * 
     * @see com.skoev.onlinestore.entities.product.ProductAvailabilityEntity
     */
    private void updateProductQuantity(Long productID, int oldQuantity
            , int newQuantity) throws MarkerException {
        ProductEntity product = em.find(ProductEntity.class, productID);
        ProductAvailabilityEntity productNumbers = product.getNumbers();
        productNumbers = em.find(ProductAvailabilityEntity.class
                , productNumbers.getId(), LockModeType.PESSIMISTIC_WRITE);

        int actualQuantity;
        productNumbers.setNumberInCarts(
                productNumbers.getNumberInCarts() - oldQuantity);
        productNumbers.calculateNumberAvailable();
        if (newQuantity == 0) {
            cartContents.remove(productID);
            cartQuantities.remove(productID);
        } else if (productNumbers.getNumberAvailable() < newQuantity) {
            actualQuantity = productNumbers.getNumberAvailable();
            productNumbers.setNumberInCarts(
                    productNumbers.getNumberInCarts() + actualQuantity);
            productNumbers.calculateNumberAvailable();
            cartQuantities.put(productID, actualQuantity);
            cartContents.get(productID).setNumber(actualQuantity);
        } else {
            cartQuantities.put(productID, newQuantity);
            productNumbers.setNumberInCarts(
                    productNumbers.getNumberInCarts() + newQuantity);
            productNumbers.calculateNumberAvailable();
        }

    }

    /**
     * Empties the cart; it simply uses the {@link #updateCart()} method to set 
     * all 
     * product quantities to 0. 
     */
    public void emptyCart() throws MarkerException {
        for (Long l : cartContents.keySet()) {
            cartContents.get(l).setNumber(0);
        }
        updateCart();
    }

    /**
     * Iterates over {@link #cartContents} to figure out the total cost of 
     * products in the cart. 
     * @return 
     */
    private BigDecimal calcTotalCost() {
        BigDecimal sum = BigDecimal.ZERO;
        for (Long l : cartContents.keySet()) {
            OrderLineEntity ol = cartContents.get(l);
            BigDecimal linePrice = ol.getProduct().getPrice().multiply(
                    BigDecimal.valueOf(ol.getNumber()));
            sum = sum.add(linePrice);
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

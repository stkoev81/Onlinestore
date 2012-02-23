package com.skoev.onlinestore.beans.requestscope;

import java.util.*; 
import javax.ejb.EJB;
import javax.inject.Named;
import javax.inject.Inject;
import javax.enterprise.context.RequestScoped;
import javax.annotation.PostConstruct; 
import javax.faces.event.ComponentSystemEvent; 
import javax.faces.context.FacesContext; 
import javax.faces.event.ActionEvent; 
import javax.faces.application.FacesMessage; 
import com.skoev.onlinestore.ejb.*;
import com.skoev.onlinestore.entities.order.*; 
import com.skoev.onlinestore.beans.sessionscope.*; 
import com.skoev.onlinestore.web.*;

/**
 * This class can be used to view orders in the online store and update their 
 * status. It is a request
 * scoped CDI managed bean. It is meant to be used only for the "employee" 
 * user roles; a store "customer" should not be able to view/update store orders. 
 * This restriction can be enforced by using this class only in JSF pages 
 * that are accessible to the "employee" roles. <br/><br/>
 * 
 * Internally, this class obtains an instance of 
 * {@link com.skoev.onlinestore.ejb.ManageOrdersStateful} from 
 * {@link com.skoev.onlinestore.beans.sessionscope.ManageOrdersSession} and 
 * delegates to it to update order status. For simply viewing the orders, it
 * uses an injected instance of 
 * {@link com.skoev.onlinestore.ejb.EntityAccessorStateless} to query them from
 * the database. 
 * 
 */
@Named
@RequestScoped
public class ViewStoreOrders {
    @EJB
    private EntityAccessorStateless entityAccessor;
    @Inject
    private RequestInfo requestInfo; 
    @Inject
    private ManageOrdersSession manageOrdersSession; 
    
    private ManageOrdersStateful manageOrdersStateful; 
    /**
     * A Pager object which aids in presenting only a part of the results to the 
     * user at a time. 
     */    
    private Pager pager;
    /**
     * Property by which the results should be sorted. Value must be one of the 
     * properties of the order. 
     */
    private String sortBy = "orderDate"; 
    /**
     * Sorting direction for querying the orders in the database.
     * Value must be "ASC" or "DESC" (default). 
     */
    private String sortDir = "DESC";
    /**
     * All orders that results from the query. 
     */
    private List<OrderEntity> allOrders = new ArrayList<OrderEntity>();
    /**
     * Sublists of {@link #allOrders} resulting from applying the endpoints 
     * contained in {@link #pager}
     */
    private List<OrderEntity> orders ;
    /**
     * A single order looked up in the database. 
     */
    private OrderEntity singleOrder; 
    /**
     * The ID of a single order to be looked up in the database. 
     */
    private Long singleOrderID; 
    /**
     * Search string used for querying the orders in the database
     */
    private String searchString; 
    
    /**
     * Allowed {@link #newStatus} entries; these can be displayed to the user in 
     * a dropdown list to choose from. 
     */
    private String[] possibleStatuses;
    
    /**
     * New status to which the order will be updated after calling 
     * {@link #updateOrderStatus()}
     */
    private String newStatus; 
    
    /**
     * Initializes the {@link com.skoev.onlinestore.web.Pager} associated
     * with this object. This method is called automatically by the container
     * right after the bean is constructed.
     */
    @PostConstruct
    public void initialize(){
        pager = new Pager(); 
        pager.setIpp(5);
        pager.setFirstItem(1); 
        manageOrdersStateful=manageOrdersSession.getManageOrdersStateful();
    }
   /**
     * Returns a navigation string which causes the JSF controller to redirect
     * to the same page and include the view parameters. This functionality 
     * is used to implement the POST-redirect-GET pattern for obtaining 
     * RESTful URL's with JSF. 
     * 
     * @return 
     */  
   public String redirectToSelf(){
    
    String path = requestInfo.getRequest().getServletPath() +
            "?faces-redirect=true&amp;includeViewParams=true"; 
    return path; 
   } 
   
   /**
     * Action listener that clears the search string. It can be used to clear 
     * the search terms when a button on the JSF page is pressed. 
     * 
     */
   public void clearSearch(ActionEvent event){
       searchString = null;    
   }
   /**
     * Action listener that sets the first page in the Pager object to 1. 
     * It can be used to reset the pager when settings such as "items per page"
     * or "sort by" are updated.  
     * @param event 
     */
   public void resetFirstItem(ActionEvent event){
       pager.setFirstItem(1);    
   }
   
   /**
    * Determines if the current user should be allowed to update the order status.
    * This can be used to hide or show JSF components depending on the user role.
    * It calls 
    * {@link com.skoev.onlinestore.entities.order.OrderAction#getActionRequired}
    * to determine the employee type required to take action next and compares 
    * that to the current user's role. 
    * 
    * @return true if the current user should be a able to update status; false
    * otherwise
    */
   public boolean showActions(){
       boolean show=false; 
       if (requestInfo.getRequest().isUserInRole(
               manageOrdersStateful.getOrder().getNextActorTypeRequired())){
           show=true; 
       }
       if(getPossibleStatuses().length == 0){
           show = false; 
       }
       return show;
   }
   
   /**
    * Determines if store order query results are more than 0. This can be used
    * to hide JSF components, such a an empty table, and show other JSF 
    * components, such as a notification that there are no orders.    
    * 
    * @return true if there are more than 0 results; false otherwise.
    */
   public boolean showOrders(){
       return allOrders != null && !allOrders.isEmpty();
   }
   
   /**
    * This method delegates to 
    * {@link com.skoev.onlinestore.ejb.ManageOrdersStateful} to update order
    * status with the status previously set by {@link #setNewStatus}. If the
    * update fails due to UpdateFailedException},
    * returns null (to stay on the same JSF page) and prints 
    * a FacesMessage about the failure. If the update fails due to an 
    * EmailException, returns the name of the "Email error" page. Otherwise, 
    * it returns the result of {@link #redirectToSelf() }. 
    * 
    * @return null if update fails ()
    */
   public String updateOrderStatus(){
       OrderStatusEntity ose = new OrderStatusEntity();
       ose.setStatus(OrderStatusEnum.valueOf(newStatus));
       ose.setStatusBegan(new Date());
       ose.setActor(requestInfo.getRequest().getUserPrincipal().getName());
       
       try {
        manageOrdersStateful.updateOrder(ose);
       }
       catch ( UpdateFailedException e){
        FacesMessage message = new FacesMessage(e.userMessage());         
            message.setSeverity(FacesMessage.SEVERITY_ERROR); 
            FacesContext context = FacesContext.getCurrentInstance();   
            context.addMessage(null,message);
         return null;
       }
       catch (EmailException ee){
           return "/Errors/EmailError.xhtml";
       }
       return redirectToSelf(); 
   }
   
   /**
     * preRenderVew listener that finds a single order from the database. 
     * It can be used in pages that display a single order. 
     * Before the call, the order ID must be set using 
     * {@link #setSingleOrderID(java.lang.Long)  }
     * After the call, the
     * order object is available through {@link #getSingleOrder() }. <br/><br/>
     * 
     */ 
   public void querySingle(ComponentSystemEvent event){
       if(singleOrderID!=null){
        manageOrdersStateful.setOrderID(singleOrderID);
       }
       manageOrdersStateful.lookupOrder();       
       singleOrder = manageOrdersStateful.getOrder();
       
    }  
   
   
    /**
    * preRenderView listener that looks up multiple orders in the database.  
    * It can be used in pages that display multiple orders. <br/><br/>
    * 
    * This method performs a JPQL query in the database for orders.
    * If a search string has 
    * been previously set with {@link #setSearchString(java.lang.String)}, the
    * search string is incorporated in the query. Otherwise, all of the orders
    * are found. 
    * Values previously set 
    * with {@link #setSortBy(java.lang.String)} and 
    * {@link #setSortDir(java.lang.String) } are used for the sorting and 
    * ordering clauses of the query. <br/><br/>
    * 
    * After this method completes, the results of the query are available 
    * through {@link #getOrders()  }
    * 
    * 
    * @param event 
    */
    public void queryAll(ComponentSystemEvent event){
        sortBy = sortBy.replaceAll("[^0-9a-zA-Z]", "");      
        sortDir = sortDir.replaceAll("[^0-9a-zA-Z]", ""); 
        String query = null;
        if (searchString==null){
             query = "SELECT p FROM OrderEntity p"
                 + " ORDER BY p." + sortBy + " " + sortDir;
             allOrders=entityAccessor.doQuery(OrderEntity.class, query); 
        }
        else {           
            String searchStringLowerCase = searchString.toLowerCase(); 
            Long searchStringAsLong = -1l; 
            try {searchStringAsLong = Long.valueOf(searchString); }
            catch (NumberFormatException e){}            
                   query = "SELECT p FROM OrderEntity p WHERE"
                           + " (p.id=?1"
                           + " OR lower(p.lastActor)=?2"
                           + " OR lower(p.currentStatus)=?2)"
                 + " ORDER BY p." + sortBy + " " + sortDir;
                   allOrders=entityAccessor.doQuery(OrderEntity.class, query
                           , searchStringAsLong, searchStringLowerCase); 
               }              
        pager.setNumItems(allOrders.size());       
    }

    /**
     * Returns a sublist of the orders resulting from the lookup operation 
     * {@link #queryAll(javax.faces.event.ComponentSystemEvent) }. The sublist
     * endpoints are determined by calling 
     * {@link com.skoev.onlinestore.web.Pager#getFirstItem()} and 
     * {@link com.skoev.onlinestore.web.Pager#getLastItem()} on the Pager 
     * associated with this object.
     * 
     */
    public List<OrderEntity> getOrders() {
        if(allOrders.isEmpty())
            return allOrders;         
        return allOrders.subList(pager.getFirstItem()-1, pager.getLastItem());
    }

    public void setOrders(List<OrderEntity> orders) {
        this.orders = orders;
    }

    public Pager getPager() {
        return pager;
    }

    public void setPager(Pager pager) {
        this.pager = pager;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public OrderEntity getSingleOrder() {
        return singleOrder;
    }

    public Long getSingleOrderID() {
        return singleOrderID;
    }

    public void setSingleOrder(OrderEntity singleOrder) {
        this.singleOrder = singleOrder;
    }

    public void setSingleOrderID(Long singleOrderID) {
        this.singleOrderID = singleOrderID;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public String getNewStatus() {
        return newStatus;
    }

    /**
     * Returns the allowed {@link #newStatus} entries. These can be displayed 
     * to the user in a dropdown list to choose from if that user has the 
     * required role to update the current status. This method calls
     * {@link com.skoev.onlinestore.entities.order.OrderAction#nextAction} to 
     * determine the allowed new status entries. 
     *  
     */
    public String[] getPossibleStatuses() {
        if(manageOrdersStateful.getOrder()!=null){
        List<OrderStatusEnum> possibleStatusesEnum = OrderAction.nextAction(
                manageOrdersStateful.getOrder()).getPossibleStatuses();
        possibleStatuses = new String[possibleStatusesEnum.size()];       
        int i = 0; 
        for(OrderStatusEnum p:possibleStatusesEnum){
            possibleStatuses[i] = p.name();
            i++; 
        }
    }
        
        return possibleStatuses;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public void setPossibleStatuses(String[] possibleStatuses) {
        this.possibleStatuses = possibleStatuses;
    }

    public String getSortDir() {
        return sortDir;
    }

    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }
}

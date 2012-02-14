
package com.skoev.onlinestore.beans.requestscope;


import com.skoev.onlinestore.ejb.*;
import com.skoev.onlinestore.entities.order.*; 
import com.skoev.onlinestore.beans.sessionscope.*; 
import com.skoev.onlinestore.web.*;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import java.util.*; 
import javax.annotation.PostConstruct; 
import javax.faces.event.ComponentSystemEvent; 
import javax.servlet.http.HttpServletRequest; 
import javax.faces.context.FacesContext; 
import javax.inject.Inject;
import javax.faces.event.ActionEvent; 
import javax.faces.application.FacesMessage; 
import java.text.*; 
import java.util.*; 
import javax.faces.component.UIComponent; 
import javax.faces.component.UIInput; 

/**
 *
 * @author stephan
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
        
    private Pager pager;
    private String sortBy = "orderDate"; 
    private String sortDir = "DESC";
    
    private List<OrderEntity> allOrders = new ArrayList<OrderEntity>(); ; 
    
    private List<OrderEntity> orders ;
    
    private OrderEntity singleOrder; 
    
    private Long singleOrderID; 
    
    private String searchString; 
    
    private String[] possibleStatuses;
    private String newStatus; 
    
    @PostConstruct
    public void initialize(){
        pager = new Pager(); 
        pager.setIpp(5);
        pager.setFirstItem(1); 
        manageOrdersStateful=manageOrdersSession.getManageOrdersStateful();
    }
     
   public String redirectToSelf(){
    
    String path = requestInfo.getRequest().getServletPath() +"?faces-redirect=true&amp;includeViewParams=true"; 
    return path; 
   } 
   
   //action listener that clears the search string 
   public void clearSearch(ActionEvent event){
       searchString = null;    
   }
   //action listener that sets the first page to 1 again when items per page or 
   // sorty by is updated
   public void resetFirstItem(ActionEvent event){
       pager.setFirstItem(1);    
   }
   
   public boolean showActions(){
       boolean show=false; 
       
       if (requestInfo.getRequest().isUserInRole(manageOrdersStateful.getOrder().getNextActorTypeRequired())){
           show=true; 
       }
       if(getPossibleStatuses().length == 0){
           show = false; 
       }
       
       return show;
   }
   
   public boolean showOrders(){
       return allOrders != null && !allOrders.isEmpty();
   }
   
   public String updateOrderStatus(){
       OrderStatusEntity ose = new OrderStatusEntity();
       ose.setStatus(OrderStatusEnum.valueOf(newStatus));
       ose.setStatusBegan(new Date());
       ose.setActor(requestInfo.getRequest().getUserPrincipal().getName());
       
       try {
        manageOrdersStateful.updateOrder(ose);
       }
       catch ( UpdateFailedException e){
        FacesMessage message = new FacesMessage("Error! Status could not be updated. It seems someone else already "
                +"updated the status while you were viewing it. Press 'page refresh' to "
                + "see the latest status.");         
            message.setSeverity(FacesMessage.SEVERITY_ERROR); 
            FacesContext context = FacesContext.getCurrentInstance();   
            context.addMessage(null,message);
         return null;
       }
       
       return redirectToSelf(); 
       
   }
   
   //preRenderVew listner for the loading a single component from the database 
   public void querySingle(ComponentSystemEvent event){
       if(singleOrderID!=null){
        manageOrdersStateful.setOrderID(singleOrderID);
       }
       manageOrdersStateful.lookupOrder();       
       singleOrder = manageOrdersStateful.getOrder();
       
    }  
   
   
   //preRenderView listener
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
                   allOrders=entityAccessor.doQuery(OrderEntity.class, query, searchStringAsLong, searchStringLowerCase); 
               }              
        pager.setNumItems(allOrders.size());       
    }

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

    public String[] getPossibleStatuses() {
       
          if(manageOrdersStateful.getOrder()!=null){
         List<OrderStatusEnum> possibleStatusesEnum = OrderAction.nextAction(manageOrdersStateful.getOrder()).getPossibleStatuses();
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

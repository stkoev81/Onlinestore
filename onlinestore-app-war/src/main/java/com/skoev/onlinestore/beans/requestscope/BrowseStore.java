
package com.skoev.onlinestore.beans.requestscope;

import com.skoev.onlinestore.ejb.EntityAccessorStateless;
import com.skoev.onlinestore.entities.product.*; 
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
import javax.faces.application.ConfigurableNavigationHandler;

/**
 *
 * @author stephan
 */
@Named(value="browseStore")
@RequestScoped
public class BrowseStore {
    @EJB
    private EntityAccessorStateless entityAccessor;
    @Inject
    private RequestInfo requestInfo;   
        
    private Pager pager;
    private String sortBy = "title";
    private String sortDir = "ASC";
    
    private List<ProductEntity> allProducts = new ArrayList<ProductEntity>(); ; 
    
    private List<ProductEntity> products ;
    
    private ProductEntity singleProduct; 
    
    private Long singleProductID; 
    
    private String searchString; 
    
    /** Creates a new instance of Browse */
    public BrowseStore() {
    }
    
     @PostConstruct
    public void initialize(){
        pager = new Pager(); 
        
        pager.setIpp(5);
        pager.setFirstItem(1);        
        
        
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
   
   
   //preRenderVew listner for the loading a single component from the database 
   public void querySingle(ComponentSystemEvent event){
        singleProduct = entityAccessor.findEntity(ProductEntity.class, singleProductID); 
        if (!singleProduct.getDisplayProductInStore()){
            FacesContext context = FacesContext.getCurrentInstance();
            ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler)
            context.getApplication().getNavigationHandler();
            handler.performNavigation("/Errors/NotAvailable.xhtml");
        }
        
    }
         
      
   
   //preRenderView listener
    public void queryAll(ComponentSystemEvent event){
        
        String productType = (String) event.getComponent().getAttributes().get("productType");
        String entityName = productType + "Entity";        
        sortBy = sortBy.replaceAll("[^0-9a-zA-Z]", ""); 
        sortDir = sortDir.replaceAll("[^0-9a-zA-Z]", ""); 
        
        
        String query = null; 
        
        if (searchString==null){
//            query = "SELECT p FROM " + entityName + " p WHERE p.productType='" + productType +"'"
//                 + " ORDER BY p." + sortBy + " ASC"; 
             query = "SELECT p FROM " + entityName + " p WHERE p.productType=?1 AND p.displayProductInStore=?2"
                 + " ORDER BY p." + sortBy + " " + sortDir;
             
             allProducts=entityAccessor.doQuery(ProductEntity.class, query, productType, true); 
             
        }
        else {           
            String searchStringPadded = "%" + searchString.toLowerCase() + "%"; 
            
               if("Book".equals(productType)){             
                   query = "SELECT p FROM " + entityName + " p WHERE p.productType=?1 AND p.displayProductInStore=?2"
                           + " AND (lower(p.author) LIKE ?3"
                           + " OR lower(p.title) LIKE ?3"
                           + " OR p.isbn=?4)"
                 + " ORDER BY p." + sortBy + " " + sortDir;
                   allProducts=entityAccessor.doQuery(ProductEntity.class, query, productType, true, searchStringPadded, searchString); 
               }
               else if("CD".equals(productType)){             
                   query = "SELECT p FROM " + entityName + " p WHERE p.productType=?1 AND p.displayProductInStore=?2"
                           + " AND (lower(p.artist) LIKE ?3"
                           + " OR lower(p.title) LIKE ?3)"                           
                 + " ORDER BY p." + sortBy + " " + sortDir;
                   allProducts=entityAccessor.doQuery(ProductEntity.class, query, productType, true, searchStringPadded); 
               }              
               
                        
            
            
//                query = "SELECT p FROM "+ entityName + " WHERE p.productType"
//                 + "lower(p.title) LIKE '%" + searchString.toLowerCase() +"%'"
//                    + " ORDER BY p." + sortBy + " ASC"; 
////       
//            query = "SELECT p FROM " + entityName + " WHERE p.productType=" + productType
//                 + " ORDER BY p." + sortBy + " ASC"; 
            
//            query = "SELECT p FROM "+ entityName + " p WHERE p.productType='" + productType + "'"
//                 + " AND p.title LIKE '%" + searchString +"%'"
//                    + " ORDER BY p." + sortBy + " ASC"; 
//       
        
       
        
        }
        
       
//        String query = "SELECT p FROM "+ entityName 
//                 + " ORDER BY p." + pager.getSortBy() + " ASC";
//         String query = "SELECT p FROM ProductEntity p WHERE p.productType='Book'";
        
   
         
        
        pager.setNumItems(allProducts.size());       
    }

    public List<ProductEntity> getProducts() {
        if(allProducts.isEmpty())
            return allProducts;         
        return allProducts.subList(pager.getFirstItem()-1, pager.getLastItem());
    }

    public void setProducts(List<ProductEntity> products) {
        this.products = products;
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

    public ProductEntity getSingleProduct() {
        return singleProduct;
    }

    public Long getSingleProductID() {
        return singleProductID;
    }

    public void setSingleProduct(ProductEntity singleProduct) {
        this.singleProduct = singleProduct;
    }

    public void setSingleProductID(Long singleProductID) {
        this.singleProductID = singleProductID;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public String getSortDir() {
        return sortDir;
    }

    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }
   
}

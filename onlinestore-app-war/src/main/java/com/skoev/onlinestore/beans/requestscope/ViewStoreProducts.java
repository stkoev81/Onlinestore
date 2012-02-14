
package com.skoev.onlinestore.beans.requestscope;


import com.skoev.onlinestore.ejb.*;
import com.skoev.onlinestore.entities.product.*; 
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
import java.io.*; 
import org.apache.commons.io.FilenameUtils;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import javax.faces.context.FacesContext; 
import javax.faces.component.UIComponent; 
import javax.faces.component.UIInput; 
import javax.faces.validator.ValidatorException; 




/**
 *
 * @author stephan
 */
@Named
@RequestScoped
public class ViewStoreProducts {
    @EJB
    private EntityAccessorStateless entityAccessor;
    @Inject
    private RequestInfo requestInfo; 
    @Inject
    private ManageProductsSession manageProductsSession; 
    @Inject
    private InsideAccount insideAccount;
    
    private ManageProductsStateful manageProductsStateful; 
    private UploadedFile uploadedFile;
        
    private Pager pager;
    private String sortBy = "productID"; 
    private String sortDir = "ASC";
    
    private List<ProductEntity> allProducts = new ArrayList<ProductEntity>(); ; 
    
    private List<ProductEntity> products ;
    
    private ProductEntity singleProduct; 
    
    private Long singleProductID; 
    
    private String searchString;  
    private String singleProductType = "Book"; 
    private boolean updateNumberInWarehouse = false; 
    
    
     @PostConstruct
    public void initialize(){
        pager = new Pager(); 
        pager.setIpp(5);
        pager.setFirstItem(1); 
        manageProductsStateful=manageProductsSession.getManageProductsStateful();       
             
    }
     
      public void updateNumberValidate(FacesContext context, UIComponent component
            , Object value){
      
            
            Integer numberInWarehouse = (Integer) ((UIInput) component.findComponent("numberInWarehouse")).getLocalValue(); 
            Boolean updateNumber = (Boolean) ((UIInput) component.findComponent("updateNumber")).getLocalValue();  
            
            if (updateNumber && numberInWarehouse == null){
            FacesMessage message = new FacesMessage("Error! If \"update number\" checkbox is selected, the \"number in warehouse\" field cannot be empty");  
            message.setSeverity(FacesMessage.SEVERITY_ERROR);      
            throw new ValidatorException(message); 
            }
         
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
   
  
   
   public String updateProductAction(){
       if(uploadedFile!=null){
           ImageEntity image = singleProduct.getImage(); 
           try {          
               image.setContent(uploadedFile.getBytes());
           }
           catch(IOException e){
               FacesMessage message = new FacesMessage("Error! The image file could not be read. Product update failed"); 
                message.setSeverity(FacesMessage.SEVERITY_ERROR); 
                FacesContext context = FacesContext.getCurrentInstance();   
                context.addMessage(null,message);

               return null;           
           } 
           image.setFileLength(uploadedFile.getSize());
           image.setFileName(FilenameUtils.getName(uploadedFile.getName()));      
       }
       singleProduct.setLastModifiedDate(new Date());
       singleProduct.setLastModifiedBy(insideAccount.getUsername());
       
              
       // will only modify latest version, has optimistic locking. 
       try {      
        manageProductsStateful.updateProduct(singleProduct, updateNumberInWarehouse); 
       }
       catch (UpdateFailedException e){
       FacesMessage message = new FacesMessage("Error! Product could not be updated. It seems that "
               + "another user updated it since you opened this page, and you were looking at an old version. "
               + "Press refresh to see the latest version and try updating again.");         
            message.setSeverity(FacesMessage.SEVERITY_ERROR); 
            FacesContext context = FacesContext.getCurrentInstance();   
            context.addMessage(null,message);
         return null;
       }
       FacesMessage message = new FacesMessage("Success! Product was updated.");     
            message.setSeverity(FacesMessage.SEVERITY_INFO); 
            FacesContext context = FacesContext.getCurrentInstance();   
            context.addMessage(null,message);
       return null;
       
        
       
   }
   
   //preRenderVew listner for the loading a single component from the database 
   public void querySingle(ComponentSystemEvent event){ 
       if(singleProductID != null){
        manageProductsStateful.setProductID(singleProductID);
       }
              
       
       manageProductsStateful.lookupProduct();            
       
    }  
   //preValidateEvent listener
   public void saveParameters(ComponentSystemEvent event){       
      UIComponent uic = event.getComponent(); 
      singleProductID= (Long)((UIInput) uic.findComponent("singleProductID")).getLocalValue(); 
      pager.setFirstItem((Integer)((UIInput) uic.findComponent("firstItem")).getLocalValue());
      pager.setIpp((Integer)((UIInput) uic.findComponent("ipp")).getLocalValue());
        sortBy= (String)((UIInput) uic.findComponent("sortBy")).getLocalValue();               
        searchString= (String)((UIInput) uic.findComponent("searchString")).getLocalValue(); 
     sortDir= (String)((UIInput) uic.findComponent("sortDir")).getLocalValue(); 
       singleProductType= (String)((UIInput) uic.findComponent("singleProductType")).getLocalValue(); 
       System.err.println(singleProductType); 
   }
   
         
   //preRenderView listener
    public void queryAll(ComponentSystemEvent event){
         sortBy = sortBy.replaceAll("[^0-9a-zA-Z\\.]", "");      
        sortDir = sortDir.replaceAll("[^0-9a-zA-Z]", ""); 
        String query = null;
        
        if (searchString==null){
             query = "SELECT p FROM ProductEntity p"
                 + " ORDER BY p." + sortBy + " " + sortDir;
             
             allProducts=entityAccessor.doQuery(ProductEntity.class, query); 
             
        }
        else {           
            //id,name, type
            String searchStringPadded = "%" + searchString.toLowerCase() + "%"; 
            String searchStringLowerCase = searchString.toLowerCase();
            Long searchStringAsLong = -1l; 
            try {searchStringAsLong = Long.valueOf(searchString); }
            catch (NumberFormatException e){}            
                   query = "SELECT p FROM ProductEntity p WHERE"
                           + " (lower(p.name) LIKE ?1"
                           + " OR lower(p.lastModifiedBy) = ?2"
                           + " OR lower(p.productType) = ?2"
                           + " OR p.productID = ?3)"
                           
                 + " ORDER BY p." + sortBy + " " + sortDir;
                   allProducts=entityAccessor.doQuery(ProductEntity.class, query, searchStringPadded, searchStringLowerCase, searchStringAsLong); 
               }              
         
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
        singleProduct = manageProductsStateful.getProduct();
        return singleProduct;
    }

    public Long getSingleProductID() {
        return singleProductID;
    }

    public void setSingleProduct(ProductEntity singleProduct) {
        this.singleProduct = singleProduct;
        manageProductsStateful.setProduct(singleProduct);
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


    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public String getSingleProductType() {
        return singleProductType;
    }

    public void setSingleProductType(String singleProductType) {
        this.singleProductType = singleProductType;
    }

    public boolean isUpdateNumberInWarehouse() {
        return updateNumberInWarehouse;
    }

    public void setUpdateNumberInWarehouse(boolean updateNumberInWarehouse) {
        this.updateNumberInWarehouse = updateNumberInWarehouse;
    }
    
    
}

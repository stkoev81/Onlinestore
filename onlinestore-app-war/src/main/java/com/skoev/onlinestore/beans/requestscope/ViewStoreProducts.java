package com.skoev.onlinestore.beans.requestscope;

import java.util.*;
import java.io.*;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.inject.Inject;
import javax.enterprise.context.RequestScoped;
import javax.annotation.PostConstruct;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ActionEvent;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.validator.ValidatorException;
import org.apache.commons.io.FilenameUtils;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import com.skoev.onlinestore.ejb.*;
import com.skoev.onlinestore.entities.product.*;
import com.skoev.onlinestore.beans.sessionscope.*;
import com.skoev.onlinestore.web.*;

/**
 * This class can be used to view products in the online store and update them.
 * It is a request
 * scoped CDI managed bean. It is meant to be used only for the "employee" 
 * user role; a store "customer" should not be able to view/update store 
 * products. Further, it may be desirable to limit the updating capability only
 * to "manager" type employees. These restrictions can be enforced by 
 * calling this class's methods only in JSF pages that are accessible to the 
 * appropriate user roles. 
 * <br/><br/>
 * 
 * Internally, this class obtains an instance of 
 * {@link com.skoev.onlinestore.ejb.ManageProductsStateful} from 
 * {@link com.skoev.onlinestore.beans.sessionscope.ManageProductsSession} and 
 * delegates to it to update the products. For simply viewing the products, it
 * uses an injected instance of 
 * {@link com.skoev.onlinestore.ejb.EntityAccessorStateless} to query them from
 * the database. 
 * 
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
    
    /**
     * The new image file uploaded by the user that will be used to replace
     * {@link #image} the existing image if {@link #updateProductAction()} is 
     * called
     */
    private UploadedFile uploadedFile;
    /**
     * A Pager object which aids in presenting only a part of the results to the 
     * user at a time. 
     */   
    private Pager pager;
    /**
     * Property by which the results should be sorted. Value must be one of the 
     * properties of the product. Default is productID. 
     */
    private String sortBy = "productID";
    /**
     * Sorting direction for querying the orders in the database.
     * Value must be "ASC" (default) or "DESC". 
     */
    private String sortDir = "ASC";
    /**
     * All products that results from the query. 
     */
    private List<ProductEntity> allProducts = new ArrayList<ProductEntity>();
    /**
     * Sublists of {@link #allProducts} resulting from applying the endpoints 
     * contained in {@link #pager}
     */
    private List<ProductEntity> products;
    /**
     * A single product looked up in the database. 
     */
    private ProductEntity singleProduct;
    /**
     * The ID of a single product to be looked up in the database. 
     */
    private Long singleProductID;
    /**
     * Search string used for querying the products in the database
     */
    private String searchString;
    
    /**
     * The type of the product to be looked up in the database.   
     * Default is "Book".
     */
    private String singleProductType = "Book";
    
    /**
     * Flag indicating whether the product availability numbers should be updated
     * when calling {@link #updateProductAction()}. 
     * @see com.skoev.onlinestore.ejb.ManageProductsStateful#updateProduct 
     */
    private boolean updateNumberInWarehouse = false;
    
    /**
     * Initializes the {@link com.skoev.onlinestore.web.Pager} associated
     * with this object. This method is called automatically by the container
     * right after the bean is constructed.
     */
    @PostConstruct
    public void initialize() {
        pager = new Pager();
        pager.setIpp(5);
        pager.setFirstItem(1);
        manageProductsStateful = manageProductsSession
                .getManageProductsStateful();
    }

    /**
     * This is a JSF validator used for input fields related to updating the 
     * product availability number. It ensures that if the "update number" 
     * checkbox is selected by the user, a number is entered in the "number in
     * warehouse" field.
     */
    public void updateNumberValidate(FacesContext context, UIComponent component
            , Object value) {
        Integer numberInWarehouse = (Integer) ((UIInput) component
                .findComponent("numberInWarehouse")).getLocalValue();
        Boolean updateNumber = (Boolean) ((UIInput) component
                .findComponent("updateNumber")).getLocalValue();
        if (updateNumber && numberInWarehouse == null) {
            FacesMessage message = new FacesMessage(
                    "Error! If \"update number\" checkbox is selected, the"
                    + " \"number in warehouse\" field cannot be empty");
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }
    }
    /**
     * Returns a navigation string which causes the JSF controller to redirect
     * to the same page and include the view parameters. This functionality 
     * is used to implement the POST-redirect-GET pattern for obtaining 
     * RESTful URL's with JSF. 
     */ 
    public String redirectToSelf() {
        String path = requestInfo.getRequest().getServletPath() +
                "?faces-redirect=true&amp;includeViewParams=true";
        return path;
    }

    /**
     * Action listener that clears the search string. It can be used to clear 
     * the search terms when a button on the JSF page is pressed. 
     * 
     */
    public void clearSearch(ActionEvent event) {
        searchString = null;
    }
    /**
     * Action listener that sets the first page in the Pager object to 1. 
     * It can be used to reset the pager when settings such as "items per page"
     * or "sort by" are updated.  
     * @param event 
     */
    public void resetFirstItem(ActionEvent event) {
        pager.setFirstItem(1);
    }
    
    
    /**
     * Updates the product's details in the database. Before this method is 
     * called, the client should obtain the product object using 
     * {@link #getSingleProduct()}, update it, and set it using
     * {@link #setSingleProduct}. If the client also sets a a new image file
     * using {@link #setUploadedFile()}, the product's image will be updated with
     * it. If the client also calls {@link #setUpdateNumberInWarehouse(boolean)}
     * with an argument of true, the availability number of the product 
     * will be updated (see 
     * {@link com.skoev.onlinestore.ejb.ManageProductsStateful#updateProduct}). 
     * 
     * <br/><br/>
     * 
     * If an EmailException is caught during the product update, this method
     * returns the name of the "Email Error" page. Otherwise, it returns null (
     * the Faces navigation stays on the same page and a Faces message is 
     * displayed about the success or failure of the update).
     * 
     */
    public String updateProductAction() {
        if (uploadedFile != null) {
            ImageEntity image = singleProduct.getImage();
            try {
                image.setContent(uploadedFile.getBytes());
            } catch (IOException e) {
                FacesMessage message = new FacesMessage("Error! The image file "
                        + "could not be read. Product update failed");
                message.setSeverity(FacesMessage.SEVERITY_ERROR);
                FacesContext context = FacesContext.getCurrentInstance();
                context.addMessage(null, message);
                return null;
            }
            image.setFileLength(uploadedFile.getSize());
            image.setFileName(FilenameUtils.getName(uploadedFile.getName()));
        }
        singleProduct.setLastModifiedDate(new Date());
        singleProduct.setLastModifiedBy(insideAccount.getUsername());


        // will only modify latest version, has optimistic locking. 
        try {
            manageProductsStateful.updateProduct(singleProduct
                    , updateNumberInWarehouse);
        } catch (UpdateFailedException e) {
            FacesMessage message = new FacesMessage(e.userMessage());
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, message);
            return null;
        } catch (EmailException ee) {
            return "/Errors/EmailError.xhtml";
        }
        FacesMessage message = new FacesMessage("Success! Product was updated.");
        message.setSeverity(FacesMessage.SEVERITY_INFO);
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, message);
        return null;
    }

    /**
     * preRenderVew listener that finds a single product from the database. 
     * It can be used in pages that display a single product. 
     * Before the call, the product ID must be set using 
     * {@link #setSingleProductID(java.lang.Long) }
     * After the call, the
     * product is available through {@link #getSingleProduct()}. <br/><br/>
     */ 
    public void querySingle(ComponentSystemEvent event) {
        if (singleProductID != null) {
            manageProductsStateful.setProductID(singleProductID);
        }
        manageProductsStateful.lookupProduct();
    }
   
    
    /**
     * preValidateEvent listener that extracts the values from several JSF
     * input components and assigns them to fields of this object. These are 
     * values necessary to perform the database query.
     * <br/><br/>
     * This listener is necessary for the "product update" page. Without this
     * listener, 
     * if the validation of any JSF component fails, the abovementioned values
     * are never set, the database query fails and the page cannot be 
     * populated properly. Therefore this listener helps to present the user
     * with a meaningful 
     * "product update" page if the initial submission of that page fails.           
     * 
     */
    public void saveParameters(ComponentSystemEvent event) {
        UIComponent uic = event.getComponent();
        singleProductID = (Long) ((UIInput) uic
                .findComponent("singleProductID")).getLocalValue();
        pager.setFirstItem((Integer) ((UIInput) uic
                .findComponent("firstItem")).getLocalValue());
        pager.setIpp((Integer) ((UIInput) uic
                .findComponent("ipp")).getLocalValue());
        sortBy = (String) ((UIInput) uic.findComponent("sortBy"))
                .getLocalValue();
        searchString = (String) ((UIInput) uic.findComponent("searchString"))
                .getLocalValue();
        sortDir = (String) ((UIInput) uic.findComponent("sortDir"))
                .getLocalValue();
        singleProductType = (String) ((UIInput) uic
                .findComponent("singleProductType")).getLocalValue();
        System.err.println(singleProductType);
    }

    /**
     * preRenderView listener that looks up multiple products in the database.  
     * It can be used in pages that display multiple products. <br/><br/>
     * 
     * This method performs a JPQL query in the database for products of the
     * type specified with {@link #setSingleProductType(java.lang.String) }. 
     * If a search string has 
     * been previously set with {@link #setSearchString(java.lang.String)}, the
     * search string is incorporated in the query. Otherwise, all products of 
     * that type are found. 
     * Values previously set 
     * with {@link #setSortBy(java.lang.String)} and 
     * {@link #setSortDir(java.lang.String) } are used for the sorting and 
     * ordering clauses of the query. <br/><br/>
     * 
     * After this method completes, the results of the query are available 
     * through {@link #getProducts() }
     * 
     * @param event 
     */
    public void queryAll(ComponentSystemEvent event) {
        sortBy = sortBy.replaceAll("[^0-9a-zA-Z\\.]", "");
        sortDir = sortDir.replaceAll("[^0-9a-zA-Z]", "");
        String query = null;
        if (searchString == null) {
            query = "SELECT p FROM ProductEntity p"
                    + " ORDER BY p." + sortBy + " " + sortDir;

            allProducts = entityAccessor.doQuery(ProductEntity.class, query);
        } else {
            //id,name, type
            String searchStringPadded = "%" + searchString.toLowerCase() + "%";
            String searchStringLowerCase = searchString.toLowerCase();
            Long searchStringAsLong = -1l;
            try {
                searchStringAsLong = Long.valueOf(searchString);
            } catch (NumberFormatException e) {
            }
            query = "SELECT p FROM ProductEntity p WHERE"
                    + " (lower(p.name) LIKE ?1"
                    + " OR lower(p.lastModifiedBy) = ?2"
                    + " OR lower(p.productType) = ?2"
                    + " OR p.productID = ?3)"
                    + " ORDER BY p." + sortBy + " " + sortDir;
            allProducts = entityAccessor.doQuery(ProductEntity.class, query
                , searchStringPadded, searchStringLowerCase, searchStringAsLong);
        }
        pager.setNumItems(allProducts.size());
    }

    /**
     * Returns a sublist of the products resulting from the lookup operation 
     * {@link #queryAll(javax.faces.event.ComponentSystemEvent)}. The sublist
     * endpoints are determined by calling 
     * {@link com.skoev.onlinestore.web.Pager#getFirstItem()} and 
     * {@link com.skoev.onlinestore.web.Pager#getLastItem()} on the Pager 
     * associated with this object.
     * 
     */
    public List<ProductEntity> getProducts() {
        if (allProducts.isEmpty()) {
            return allProducts;
        }
        return allProducts.subList(pager.getFirstItem() - 1, pager.getLastItem());
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

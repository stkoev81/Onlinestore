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
import javax.faces.application.ConfigurableNavigationHandler;
import com.skoev.onlinestore.ejb.EntityAccessorStateless;
import com.skoev.onlinestore.entities.product.*;
import com.skoev.onlinestore.web.*;

/**
 * This class is used for browsing the products in the online store. It is a 
 * request scoped CDI managed bean. The JSF pages that use this bean can
 * be accessible to both logged in users and unregistered visitors; this 
 * class does not access any user-specific information. <br/><br/>
 * 
 * The code of this class must be updated if any new products types are added 
 * to the store. 
 * Currently, it works with Book and CD. 
 * 
 * @see com.skoev.onlinestore.entities.product.ProductEntity
 * @see com.skoev.onlinestore.entities.product.BookEntity
 * @see com.skoev.onlinestore.entities.product.CDEntity
 */
@Named(value = "browseStore")
@RequestScoped
public class BrowseStore {
    @EJB
    private EntityAccessorStateless entityAccessor;
    @Inject
    private RequestInfo requestInfo;
    
    /**
     * A Pager object which aids in presenting only a part of the results to the 
     * user at a time. 
     */
    private Pager pager;
    /**
     * Property by which the results should be sorted. Value must be one of the 
     * properties of the product. 
     */
    private String sortBy = "title";
    /**
     * Sorting direction for querying the products in the database.
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

    public BrowseStore() {
    }

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
    }

    /**
     * Returns a navigation string which causes the JSF controller to redirect
     * to the same page and include the view parameters. This functionality 
     * is used to implement the POST-redirect-GET pattern for obtaining 
     * RESTful URL's with JSF. 
     * 
     * @return 
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
     * preRenderVew listener that finds a single product from the database. 
     * It can be used in pages that display a single product. 
     * Before the call, the product ID must be set using 
     * {@link #setSingleProductID(java.lang.Long) }
     * After the call, the
     * product is available through {@link #getSingleProduct()}. <br/><br/>
     * 
     * If the product with that ID is not available in the database, a 
     * JSF navigation to a "Not available" error page is performed.
     *
     */
    public void querySingle(ComponentSystemEvent event) {
        singleProduct = entityAccessor.findEntity(ProductEntity.class
                , singleProductID);
        if (!singleProduct.getDisplayProductInStore()) {
            FacesContext context = FacesContext.getCurrentInstance();
            ConfigurableNavigationHandler handler = 
                    (ConfigurableNavigationHandler) context.getApplication()
                    .getNavigationHandler();
            handler.performNavigation("/Errors/NotAvailable.xhtml");
        }

    }

    /**
     * preRenderView listener that looks up multiple products in the database.  
     * It can be used in pages that display multiple products. <br/><br/>
     * 
     * The
     * f:event tag to which this listener is assigned must have an f:attribute 
     * tag with a name of "productType" and value equal to whatever the 
     * product type is. <br/><br/>
     * 
     * This method performs a JPQL query in the database for products of the
     * type specified by the f:attribute tag. If a search string has 
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
     * 
     */
    public void queryAll(ComponentSystemEvent event) {
        String productType = (String) event.getComponent().getAttributes()
                .get("productType");
        String entityName = productType + "Entity";
        sortBy = sortBy.replaceAll("[^0-9a-zA-Z]", "");
        sortDir = sortDir.replaceAll("[^0-9a-zA-Z]", "");
        String query = null;
        if (searchString == null) {
            query = "SELECT p FROM " + entityName + " p WHERE p.productType=?1 "
                    + "AND p.displayProductInStore=?2"
                    + " ORDER BY p." + sortBy + " " + sortDir;
            allProducts = entityAccessor.doQuery(ProductEntity.class, query
                    , productType, true);
        } else {
            String searchStringPadded = "%" + searchString.toLowerCase() + "%";
            if ("Book".equals(productType)) {
                query = "SELECT p FROM " + entityName + " p WHERE "
                        + "p.productType=?1 AND p.displayProductInStore=?2"
                        + " AND (lower(p.author) LIKE ?3"
                        + " OR lower(p.title) LIKE ?3"
                        + " OR p.isbn=?4)"
                        + " ORDER BY p." + sortBy + " " + sortDir;
                allProducts = entityAccessor.doQuery(ProductEntity.class, query
                        , productType, true, searchStringPadded, searchString);
            } else if ("CD".equals(productType)) {
                query = "SELECT p FROM " + entityName + " p WHERE "
                        + "p.productType=?1 AND p.displayProductInStore=?2"
                        + " AND (lower(p.artist) LIKE ?3"
                        + " OR lower(p.title) LIKE ?3)"
                        + " ORDER BY p." + sortBy + " " + sortDir;
                allProducts = entityAccessor.doQuery(ProductEntity.class, query
                        , productType, true, searchStringPadded);
            }
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

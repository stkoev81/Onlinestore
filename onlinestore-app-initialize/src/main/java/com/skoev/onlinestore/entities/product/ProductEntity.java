package com.skoev.onlinestore.entities.product;

import java.math.BigDecimal;
import java.util.*;
import java.io.Serializable;
import javax.persistence.*;
import java.text.DateFormat;

/**
 * Class that represents a generic product sold in the online store. It contains
 * properties that are common to all products. This class is subclassed
 * to represent more specific product types. In this particular application, 
 * the only product types are Book and CD, but it can easily be extended to 
 * include more product types by using inheritance. 
 * 
 * @see BookEntity 
 * @see CDEntity
 * 
 */
@Entity
@Table(name = "PRODUCTS")
public class ProductEntity implements Serializable {

    /**
     * Autogenerated ID value
     */
    @GeneratedValue
    @Id
    private Long productID;
    private BigDecimal price;
    /**
     * A generic name of the product
     */
    private String name;
    /**
     * Username of the last employee who modified this product.
     */
    private String lastModifiedBy;
    /**
     * Date when this product was last modified. 
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;
    /**
     * This flag determines whether the product should be displayed to customers
     * browsing the store; if false, the product is hidden from them but is still
     * seen by employees viewing store products. 
     */
    private Boolean displayProductInStore = true;
    /**
     * Weight of this product; this could be used for calculating shipping costs. 
     */
    private Double shippingWeight;
    /**
     * Type (category) of the product; this could be used in web pages displaying 
     * products by type. 
     */
    private String productType;
    /**
     * A generic description of the product. 
     */
    @Column(length = 2000)
    private String description;
    /**
     * A generic mapping of properties for the product; for example some products 
     * might have height, weight, battery life, etc. 
     */
    @ElementCollection
    private Map<String, String> properties;
    // unidirectional
    /**
     * This field represent the current inventory for this product. 
     */
    @OneToOne(fetch = FetchType.EAGER)
    private ProductAvailabilityEntity numbers = new ProductAvailabilityEntity();
    /**
     * Autogenerated value for enforcing optimistic locking. 
     */
    @Version
    private Integer version;
    //unidirectional
    /**
     * Image object for this product.
     */
    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    @JoinColumn
    private ImageEntity image;

    public String getDescription() {
        return description;
    }

    public Boolean getDisplayProductInStore() {
        return displayProductInStore;
    }

    public ImageEntity getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Long getProductID() {
        return productID;
    }

    public String getProductType() {
        return productType;
    }

    public Double getShippingWeight() {
        return shippingWeight;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDisplayProductInStore(Boolean displayProductInStore) {
        this.displayProductInStore = displayProductInStore;
    }

    public void setImage(ImageEntity image) {
        this.image = image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setProductID(Long productID) {
        this.productID = productID;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public void setShippingWeight(Double shippingWeight) {
        this.shippingWeight = shippingWeight;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    /**
     * Returns a formatted string representation of the date 
     * {@link #lastModifiedDate}. 
     * @return 
     */
    public String getLastModifiedDateFormatted() {
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT
                , DateFormat.SHORT, Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        return dateFormat.format(lastModifiedDate);
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public ProductAvailabilityEntity getNumbers() {
        return numbers;
    }

    public void setNumbers(ProductAvailabilityEntity numbers) {
        this.numbers = numbers;
    }
}

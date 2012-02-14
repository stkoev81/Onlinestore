/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.entities.product;

import java.math.BigDecimal;
import java.util.*;
import java.io.Serializable;
import javax.persistence.*; 
import java.text.DateFormat; 
/**
 *
 * @author stephan
 */

@Entity @Table(name="PRODUCTS")
public class ProductEntity implements Serializable {
   @GeneratedValue
   @Id
   private Long productID;
   private BigDecimal price;    
   private String name; 
   private String lastModifiedBy; 
   @Temporal(TemporalType.TIMESTAMP)
   private Date lastModifiedDate; 
   
   
   private Boolean displayProductInStore = true; 
   private Boolean autoHide = false;  
   private Double shippingWeight;   
   private String productType; 
   @Column(length=2000)
   private String description; 
   
   @ElementCollection
   private Map<String,String> properties; 
   
   // unidirectional
   @OneToOne(fetch=FetchType.EAGER)
   private ProductAvailabilityEntity numbers = new ProductAvailabilityEntity();
   
   @Version
   private Integer version;
        
   //unidirectional
   @OneToOne(fetch=FetchType.EAGER,  cascade = {CascadeType.ALL})
   @JoinColumn
   private ImageEntity image;

    public Boolean getAutoHide() {
        return autoHide;
    }

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

    public void setAutoHide(Boolean autoHide) {
        this.autoHide = autoHide;
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
    
    public String getLastModifiedDateFormatted(){
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US);
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
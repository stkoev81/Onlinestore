package com.skoev.onlinestore.entities.order;
import com.skoev.onlinestore.entities.product.ProductEntity;
import java.io.Serializable;
import javax.persistence.*;

/**
 * This class represents a single order line item in an order; an order line
 * corresponds
 * to a unique product in an order.
 * 
 */
@Entity @Table(name="ORDER_LINES")
public class OrderLineEntity implements Serializable{
    /**
     * Autogenerated ID. 
     */
    @GeneratedValue
    @Id 
    private Long id; 
    /**
     * Quantity of the product specified by this order line. 
     */
    private Integer number; 
    
    // unidirectional
    /**
     * Product to which this order line corresponds. 
     */
    @ManyToOne(fetch=FetchType.EAGER) @JoinColumn
    private ProductEntity product;

    public Long getId() {
        return id;
    }

    public Integer getNumber() {
        return number;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public void setProduct(ProductEntity product) {
        this.product = product;
    }
    
    
    
}

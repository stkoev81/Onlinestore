/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.entities.order;
import com.skoev.onlinestore.entities.product.ProductEntity;
import java.io.Serializable;
import javax.persistence.*;
/**
 *
 * @author stephan
 */
@Entity @Table(name="ORDER_LINES")
public class OrderLineEntity implements Serializable{
    @GeneratedValue
    @Id 
    private Long id; 
    private Integer number; 
    
    // unidirectional
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

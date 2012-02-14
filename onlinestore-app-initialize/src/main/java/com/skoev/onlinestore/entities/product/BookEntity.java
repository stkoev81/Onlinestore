/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.entities.product;

import java.math.BigDecimal;
import java.util.*;
import java.io.Serializable;
import javax.persistence.*; 

/**
 *
 * @author stephan
 */

@Entity
public class BookEntity extends ProductEntity {
   
   private String author; 
   private String title; 
   private String isbn;
    @Column(length=2000)
   private String summary;

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getSummary() {
        return summary;
    }

    public String getTitle() {
        return title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}

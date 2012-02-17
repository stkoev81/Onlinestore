/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.entities.product;

import javax.persistence.*;

/**
 * This class represents a CD product in the online store. 
 */
@Entity
public class CDEntity extends ProductEntity {

    private String artist;
    private String title;
    @Column(length = 2000)
    private String biography;

    public String getArtist() {
        return artist;
    }

    public String getBiography() {
        return biography;
    }

    public String getTitle() {
        return title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

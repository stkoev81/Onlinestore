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
@Entity @Table(name="IMAGES")
public class ImageEntity {
    @GeneratedValue
    @Id
    private Long imageID; 
    private String imageName;
    private String fileName; 
    private Long fileLength;
    @Lob @Basic(fetch=FetchType.LAZY)
    private byte[] content;

    public byte[] getContent() {
        return content;
    }

    public Long getFileLength() {
        return fileLength;
    }

    public String getFileName() {
        return fileName;
    }

    public Long getImageID() {
        return imageID;
    }

    public String getImageName() {
        return imageName;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setFileLength(Long fileLength) {
        this.fileLength = fileLength;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setImageID(Long imageID) {
        this.imageID = imageID;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
    
    
}

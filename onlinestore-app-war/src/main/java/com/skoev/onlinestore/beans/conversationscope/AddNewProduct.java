/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.skoev.onlinestore.beans.conversationscope;
import com.skoev.onlinestore.entities.product.*;  

import javax.inject.Named;
import javax.enterprise.context.ConversationScoped;
import java.io.Serializable;
import javax.inject.Inject; 
import javax.enterprise.context.Conversation; 
import javax.faces.application.FacesMessage;

import org.apache.commons.io.FilenameUtils;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import javax.faces.validator.ValidatorException; 
import javax.faces.context.FacesContext; 
import javax.faces.component.UIComponent; 

import com.skoev.onlinestore.ejb.EntityAccessorStateless;
import javax.ejb.EJB;
import java.io.*; 
import java.util.Date; 
import com.skoev.onlinestore.beans.sessionscope.InsideAccount; 
import com.skoev.onlinestore.ejb.MailSenderStateless;

/**
 *
 * @author stephan
 */
@Named
@ConversationScoped
public class AddNewProduct implements Serializable {
    @Inject 
    Conversation conversation;
    @Inject
    private InsideAccount insideAccount; 
      
    private String newProductType = "Book"; 
    private ProductEntity product; 
    private ImageEntity image; 
    private UploadedFile uploadedFile;
    @EJB
    private EntityAccessorStateless entityAccessor;
    
       @EJB
    private MailSenderStateless mailSender;
    
    
    /** Creates a new instance of CartManipulatorConversation */
    public AddNewProduct() {
    }
    
    public void fileSizeValidate(FacesContext context, UIComponent component
            , Object value){
    if(((UploadedFile) value).getSize() > 500000){
         FacesMessage message = new FacesMessage("Error! File is too large!");
                
        message.setSeverity(FacesMessage.SEVERITY_ERROR);      
        throw new ValidatorException(message); }
    }  
     
    public void textLengthValidate(FacesContext context, UIComponent component
            , Object value){
        String text = (String) value; 
   if(text!=null && text.length()>20000){
         FacesMessage message = new FacesMessage("Error! Entered text is too long. "
                    + "Max length is 2000 characters.");
                
        message.setSeverity(FacesMessage.SEVERITY_ERROR);      
        throw new ValidatorException(message); }
    }
     
    
    public String openNewProductForm(){
        conversation.begin(); 
        if("Book".equals(newProductType)){
            product = new BookEntity();             
        }
        else if("CD".equals(newProductType)){
            product = new CDEntity(); 
        }   
        image = new ImageEntity();
        
    
                
        return "/InsideAccount/Manager/AddNewProductForm.xhtml";
    }
    
    
    public String createNewProduct(){
        // add the image to the product entity
        // persist entity
        // remove from the conversation scope  
        // go to success page, where you have links for prodcut creation or products page
        try {
            image.setContent(uploadedFile.getBytes());
        }
        catch(IOException e){
        throw new RuntimeException(e);
        }
        
        image.setFileLength(uploadedFile.getSize());
        image.setFileName( FilenameUtils.getName(uploadedFile.getName()));
        product.setImage(image);
        product.setLastModifiedDate(new Date());
        product.setLastModifiedBy(insideAccount.getUsername());
        product.setProductType(newProductType);
        entityAccessor.persistEntity(product.getNumbers());
        entityAccessor.persistEntity(product);       
        
        mailSender.sendAlert("ProductID: " + product.getProductID(), "New product created");
        conversation.end(); 
        
        
    return "/InsideAccount/Manager/ProductAddSuccess.xhtml?faces-redirect=true";
    }
    public String cancelNewProduct(){
        // remove from the conversation scope
        // go to products page
        conversation.end();
        return "/InsideAccount/Employee/ViewStoreProducts.xhtml?faces-redirect=true";
    }
    public String back(){
        // remove from the conversation scope
        // go to new product page
        conversation.end(); 
        return "/InsideAccount/Manager/AddNewProduct.xhtml?faces-redirect=true";
    }
    

    public String getNewProductType() {
        return newProductType;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public void setNewProductType(String newProductType) {
        this.newProductType = newProductType;
    }

    public void setProduct(ProductEntity product) {
        this.product = product;
    }

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public ImageEntity getImage() {
        return image;
    }

    public void setImage(ImageEntity image) {
        this.image = image;
    }
    
    
    
    
}

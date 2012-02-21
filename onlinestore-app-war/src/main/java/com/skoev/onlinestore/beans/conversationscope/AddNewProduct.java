package com.skoev.onlinestore.beans.conversationscope;

import java.io.*; 
import java.util.Date; 
import javax.inject.Named;
import javax.inject.Inject; 
import javax.enterprise.context.Conversation; 
import javax.enterprise.context.ConversationScoped;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.validator.ValidatorException; 
import javax.faces.context.FacesContext; 
import javax.faces.component.UIComponent; 
import org.apache.commons.io.FilenameUtils;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import com.skoev.onlinestore.ejb.*; 
import com.skoev.onlinestore.beans.sessionscope.InsideAccount; 
import com.skoev.onlinestore.entities.product.*; 

/**
 * This class is used for adding a new product to the online store. It is a 
 * conversation scoped CDI managed bean. 
 * 
 */
@Named
@ConversationScoped
public class AddNewProduct implements Serializable {
    @Inject 
    Conversation conversation;
    @Inject
        
    private InsideAccount insideAccount; 

    /**
     * The new product that is being created
     */
    private ProductEntity product; 
    /**
     * The image that will be used for the new product
     */
    private ImageEntity image; 
    /**
     * The image file uploaded by the user that will be used for {@link #image}
     */
    private UploadedFile uploadedFile;
    
    /**
     * The new product type; the default value is book. 
     */
    private String newProductType = "Book"; 
    
    /**
     * Injected EJB used for persistence
     */
    @EJB
    private EntityAccessorStateless entityAccessor;
    
    /**
     * Injected EJB used for sending emails
     */
    @EJB
    private MailSenderStateless mailSender;
    
    
    /** Creates a new instance of CartManipulatorConversation */
    public AddNewProduct() {
    }
    
    /**
     * JSF validator for the t:inputFileUpload component used to upload the 
     * image file. This validator ensures that that the maximum file size is 
     * 500 kB.  
     * @param context
     * @param component
     * @param value 
     */
    public void fileSizeValidate(FacesContext context, UIComponent component
            , Object value){
    if(((UploadedFile) value).getSize() > 500000){
         FacesMessage message = new FacesMessage("Error! File is too large!");
                
        message.setSeverity(FacesMessage.SEVERITY_ERROR);      
        throw new ValidatorException(message); }
    }  
     
    /**
     * JSF validator for the h:inputTextArea component used to upload text
     * about a product. This validator ensures that the text is at most 2000
     * characters. 
     * @param context
     * @param component
     * @param value 
     */
    public void textLengthValidate(FacesContext context, UIComponent component
            , Object value){
        String text = (String) value; 
        if(text!=null && text.length()>2000){
            FacesMessage message = new FacesMessage("Error! Entered text is too"
                    + " long. Max length is 2000 characters.");
        message.setSeverity(FacesMessage.SEVERITY_ERROR);      
        throw new ValidatorException(message); }
    }
     
    
    /**
     * This method starts a CDI conversation, creates a new product object, 
     * and adds the object to the conversation. The object's class is a 
     * subtype of ProductEntity and is determined by the type of product the
     * user has previously specified with {@link #setNewProductType}
     * @return The the name of the page with a form for adding a new product
     */
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
    
    /**
     * This method commits to the database the product object that has been 
     * created with {@link #openNewProductForm} and updated through 
     * {@link #getProduct}. The product image is populated with the file 
     * that has been set with {@link #setUploadedFile()}. <br/><br/>
     * 
     * Next, this method ends the CDI conversation and attempts to send 
     * an alert email. 
     * 
     * @return The name of the "Email Error" page if there is an EmailException;
     * the name of the "Product addition success" page otherwise.
     * 
     */
    public String createNewProduct() {
        // add the image to the product entity
        // persist entity
        // remove from the conversation scope  
        // go to success page, where you have links for prodcut creation or 
        // products page
        try {
            image.setContent(uploadedFile.getBytes());
        }
        //TODO: handle this exc
        catch(IOException e){
            throw new RuntimeException(e);
        }
        
        image.setFileLength(uploadedFile.getSize());
        image.setFileName( FilenameUtils.getName(uploadedFile.getName()));
        product.setImage(image);
        product.setLastModifiedDate(new Date());
        product.setLastModifiedBy(insideAccount.getUsername());
        product.setProductType(newProductType);
        
        conversation.end(); 
        try {
            mailSender.sendAlert("ProductID: " + product.getProductID(), "New "
                    + "product created");
        }
        catch (EmailException ee){
            return "/Errors/EmailError.xhtml?faces-redirect=true";
        }
        entityAccessor.persistEntity(product.getNumbers());
        entityAccessor.persistEntity(product);       
        return "/InsideAccount/Manager/ProductAddSuccess.xhtml?faces-redirect"
                + "=true";
    }
    
    /**
     * Ends the CDI conversation without committing the product object to the 
     * database
     * @return The name of the page showing a list of store products. 
     */
    public String cancelNewProduct(){
        // remove from the conversation scope
        // go to products page
        conversation.end();
        return "/InsideAccount/Employee/ViewStoreProducts.xhtml?faces-redirect"
                + "=true";
    }
    
    /**
     * Ends the CDI conversation without committing the product object to the
     * database
     * @return The name of the page where user chooses the type of product to 
     * add
     */
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
